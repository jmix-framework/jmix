/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.search.searching.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.SearchApplicationProperties;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchResult;
import io.jmix.search.searching.SearchStrategy;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("search_EntitySearcher")
public class EntitySearcherImpl implements EntitySearcher {

    private static final Logger log = LoggerFactory.getLogger(EntitySearcherImpl.class);

    @Autowired
    protected RestHighLevelClient esClient;
    @Autowired
    protected Metadata metadata;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    protected DataManager secureDataManager;
    @Autowired
    protected InstanceNameProvider instanceNameProvider;
    @Autowired
    protected SearchApplicationProperties searchApplicationProperties;
    @Autowired
    protected IdSerialization idSerialization;

    @Override
    public SearchResult search(SearchContext searchContext, SearchStrategy searchStrategy) {
        log.debug("Perform search by context '{}'", searchContext);
        SearchResultImpl searchResult = initSearchResult(searchContext, searchStrategy);
        SearchRequest searchRequest = createSearchRequest(searchContext, searchStrategy);
        boolean moreDataAvailable;
        do {
            updateRequestOffset(searchRequest, searchResult);
            SearchResponse searchResponse;
            try {
                log.debug("Search Request: {}", searchRequest);
                searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                throw new RuntimeException("Search failed", e);
            }
            SearchHits searchHits = searchResponse.getHits();
            Map<MetaClass, List<SearchHit>> hitsByEntityName = groupSearchHitsByEntity(searchHits);
            fillSearchResult(searchResult, hitsByEntityName);

            long totalHits = searchResponse.getHits().getTotalHits().value;
            moreDataAvailable = (totalHits - searchResult.getEffectiveOffset()) > 0;
        } while (moreDataAvailable && !isResultFull(searchResult, searchContext));
        searchResult.setMoreDataAvailable(moreDataAvailable);
        return searchResult;
    }

    @Override
    public SearchResult searchNextPage(SearchResult previousSearchResult) {
        return search(previousSearchResult.createNextPageSearchContext(), previousSearchResult.getSearchStrategy());
    }

    @Override
    public Collection<Object> loadEntityInstances(SearchResult searchResult) {
        return loadEntityInstances(searchResult, Collections.emptyMap());
    }

    @Override
    public Collection<Object> loadEntityInstances(SearchResult searchResult, Map<String, FetchPlan> fetchPlans) {
        List<Object> result = new ArrayList<>(searchResult.getSize());
        searchResult.getEntityNames().forEach(entityName -> {
            MetaClass metaClass = metadata.getClass(entityName);
            Collection<SearchResultEntry> entries = searchResult.getEntriesByEntityName(entityName);

            for (Collection<SearchResultEntry> entriesPartition : Iterables.partition(entries, searchApplicationProperties.getSearchReloadEntitiesBatchSize())) {
                List<Object> partitionIds = entriesPartition.stream()
                        .map(entry -> idSerialization.stringToId(entry.getDocId()))
                        .map(Id::getValue)
                        .collect(Collectors.toList());

                FetchPlan fetchPlan = fetchPlans.get(entityName);
                FluentLoader.ByIds<Object> loader = secureDataManager.load(metaClass.getJavaClass()).ids(partitionIds);
                if (fetchPlan == null) {
                    loader.fetchPlan(FetchPlan.LOCAL);
                } else {
                    loader.fetchPlan(fetchPlan);
                }
                List<Object> partitionResult = loader.list();

                result.addAll(partitionResult);
            }
        });
        return result;
    }

    protected SearchResultImpl initSearchResult(SearchContext searchContext, SearchStrategy searchStrategy) {
        return new SearchResultImpl(searchContext, searchStrategy);
    }

    protected SearchRequest createSearchRequest(SearchContext searchContext, SearchStrategy searchStrategy) {
        SearchRequest searchRequest = createBaseSearchRequest(searchContext);
        searchStrategy.configureRequest(searchRequest, searchContext);
        postStrategyConfiguration(searchRequest, searchContext);
        return searchRequest;
    }

    protected SearchRequest createBaseSearchRequest(SearchContext searchContext) {
        SearchRequest searchRequest = new SearchRequest();
        configureTargetIndices(searchRequest, searchContext);
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        return searchRequest;
    }

    protected void postStrategyConfiguration(SearchRequest searchRequest, SearchContext searchContext) {
        searchRequest.source().size(searchContext.getSize());
        configureHighlight(searchRequest);
    }

    protected void configureTargetIndices(SearchRequest searchRequest, SearchContext searchContext) {
        List<String> indices = searchContext.getIndices();
        if (indices.isEmpty()) {
            searchRequest.indices("search_index_*");
        } else {
            searchRequest.indices(indices.toArray(new String[0]));
        }
    }

    protected void configureHighlight(SearchRequest searchRequest) {
        SearchSourceBuilder searchSourceBuilder = searchRequest.source();
        if (searchSourceBuilder.highlighter() == null) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("*").preTags("<b>").postTags("</b>");
            highlightBuilder.requireFieldMatch(true);
            searchRequest.source().highlighter(highlightBuilder);
        }
    }

    protected void updateRequestOffset(SearchRequest searchRequest, SearchResultImpl searchResult) {
        searchRequest.source().from(searchResult.getEffectiveOffset());
    }

    protected boolean isResultFull(SearchResultImpl searchResultImpl, SearchContext searchContext) {
        return searchResultImpl.getSize() >= searchContext.getSize();
    }

    protected Map<MetaClass, List<SearchHit>> groupSearchHitsByEntity(SearchHits searchHits) {
        return Stream.of(searchHits.getHits())
                .collect(Collectors.groupingBy(hit -> {
                    Id<Object> entityId = idSerialization.stringToId(hit.getId());
                    return metadata.getClass(entityId.getEntityClass());
                }));
    }

    protected void fillSearchResult(SearchResultImpl searchResultImpl, Map<MetaClass, List<SearchHit>> hitsByEntityName) {
        int sizeLimit = searchResultImpl.getSearchContext().getSize();
        for (Map.Entry<MetaClass, List<SearchHit>> entry : hitsByEntityName.entrySet()) {
            MetaClass metaClass = entry.getKey();
            List<SearchHit> entityHits = entry.getValue();
            List<Object> entityIds = entityHits.stream()
                    .map(SearchHit::getId)
                    .map(idSerialization::stringToId)
                    .map(Id::getValue)
                    .collect(Collectors.toList());
            Map<String, String> reloadedIdNames = loadEntityInstanceNames(metaClass, entityIds);

            for (SearchHit searchHit : entityHits) {
                if (searchResultImpl.getSize() >= sizeLimit) {
                    return;
                }

                String entityId = searchHit.getId();
                if (reloadedIdNames.containsKey(entityId)) {
                    String instanceName = reloadedIdNames.get(entityId);
                    searchResultImpl.addEntry(createSearchResultEntry(entityId, instanceName, metaClass.getName(), searchHit));
                }
                searchResultImpl.incrementOffset();
            }
        }
    }

    protected SearchResultEntry createSearchResultEntry(String entityId, String instanceName, String entityName, SearchHit searchHit) {
        Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
        List<FieldHit> fieldHits = new ArrayList<>();
        highlightFields.forEach((f, h) -> {
            String highlight = Arrays.stream(h.getFragments()).map(Text::toString).collect(Collectors.joining("..."));
            fieldHits.add(new FieldHit(formatFieldName(f), highlight));
        });
        return new SearchResultEntry(entityId, instanceName, entityName, fieldHits);
    }

    protected Map<String, String> loadEntityInstanceNames(MetaClass metaClass, List<Object> entityIds) {
        Map<String, String> result = new HashMap<>();
        String primaryKeyName = metadataTools.getPrimaryKeyName(metaClass);
        for (List<Object> idsPartition : Lists.partition(entityIds, searchApplicationProperties.getSearchReloadEntitiesBatchSize())) {
            log.debug("Load instance names for ids: {}", idsPartition);

            List<Object> partitionResult;
            if (metadataTools.hasCompositePrimaryKey(metaClass)) {
                partitionResult = idsPartition.stream()
                        .map(id -> secureDataManager
                                .load(metaClass.getJavaClass())
                                .id(id)
                                .fetchPlan(FetchPlan.INSTANCE_NAME)
                                .optional())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
            } else {
                partitionResult = secureDataManager
                        .load(metaClass.getJavaClass())
                        .query("select e from " + metaClass.getName() + " e where e." + primaryKeyName + " in :ids")
                        .parameter("ids", idsPartition)
                        .fetchPlan(FetchPlan.INSTANCE_NAME)
                        .list();
            }

            partitionResult.forEach(entity -> {
                String instanceName = instanceNameProvider.getInstanceName(entity);
                result.put(idSerialization.idToString(Id.of(entity)), instanceName);
            });
        }
        return result;
    }

    protected String formatFieldName(String fieldName) {
        return StringUtils.removeEnd(fieldName, "._instance_name");
    }
}
