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
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.searching.*;
import io.jmix.search.utils.Constants;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
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
    protected MetadataTools metadataTools;
    @Autowired
    protected DataManager secureDataManager;
    @Autowired
    protected InstanceNameProvider instanceNameProvider;
    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected IdSerialization idSerialization;
    @Autowired
    protected SecureOperations secureOperations;
    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;
    @Autowired
    protected PolicyStore policyStore;
    @Autowired
    protected SearchStrategyManager searchStrategyManager;

    @Override
    public SearchResult search(SearchContext searchContext) {
        return search(searchContext, searchStrategyManager.getDefaultSearchStrategy());
    }

    @Override
    public SearchResult search(SearchContext searchContext, SearchStrategy searchStrategy) {
        log.debug("Perform search by context '{}'", searchContext);
        SearchResultImpl searchResult = initSearchResult(searchContext, searchStrategy);
        List<String> targetIndexes = resolveTargetIndexes(searchContext);
        if (targetIndexes.isEmpty()) {
            return searchResult;
        }

        SearchRequest searchRequest = createSearchRequest(targetIndexes, searchContext, searchStrategy);
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

    protected SearchResultImpl initSearchResult(SearchContext searchContext, SearchStrategy searchStrategy) {
        return new SearchResultImpl(searchContext, searchStrategy);
    }

    protected SearchRequest createSearchRequest(List<String> targetIndexes, SearchContext searchContext, SearchStrategy searchStrategy) {
        SearchRequest searchRequest = createBaseSearchRequest(targetIndexes);
        searchStrategy.configureRequest(searchRequest, searchContext);
        postStrategyConfiguration(searchRequest, searchContext);
        return searchRequest;
    }

    protected SearchRequest createBaseSearchRequest(List<String> targetIndexes) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(targetIndexes.toArray(targetIndexes.toArray(new String[0])));
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        return searchRequest;
    }

    protected void postStrategyConfiguration(SearchRequest searchRequest, SearchContext searchContext) {
        searchRequest.source().size(searchContext.getSize());
        configureHighlight(searchRequest);
    }

    protected List<String> resolveTargetIndexes(SearchContext searchContext) {
        Collection<String> requestedEntities = searchContext.getEntities();
        if (requestedEntities.isEmpty()) {
            requestedEntities = indexConfigurationManager.getAllIndexedEntities();
        }

        return requestedEntities.stream()
                .map(metadata::getClass)
                .filter(metaClass -> secureOperations.isEntityReadPermitted(metaClass, policyStore))
                .map(metaClass -> indexConfigurationManager.getIndexConfigurationByEntityNameOpt(metaClass.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(IndexConfiguration::getIndexName)
                .collect(Collectors.toList());
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

            boolean hasRowLevelPolicies = policyStore.getRowLevelPolicies(metaClass).findAny().isPresent();
            List<SearchHit> entityHits = entry.getValue();
            Set<String> effectiveIds;
            if (hasRowLevelPolicies) {
                List<Object> entityIds = entityHits.stream()
                        .map(SearchHit::getId)
                        .map(idSerialization::stringToId)
                        .map(Id::getValue)
                        .collect(Collectors.toList());
                effectiveIds = reloadIds(metaClass, entityIds);
            } else {
                effectiveIds = entityHits.stream()
                        .map(SearchHit::getId)
                        .collect(Collectors.toSet());
            }

            for (SearchHit searchHit : entityHits) {
                if (searchResultImpl.getSize() >= sizeLimit) {
                    return;
                }

                String entityId = searchHit.getId();
                if (effectiveIds.contains(entityId)) {
                    Map<String, Object> source = searchHit.getSourceAsMap();
                    String displayedName;
                    if (source == null) {
                        displayedName = entityId;
                    } else {
                        String instanceName = (String) source.get(Constants.INSTANCE_NAME_FIELD);
                        displayedName = Strings.isEmpty(instanceName) ? entityId : instanceName;
                    }
                    searchResultImpl.addEntry(createSearchResultEntry(entityId, displayedName, metaClass.getName(), searchHit));
                }
                searchResultImpl.incrementOffset();
            }
        }
    }

    protected SearchResultEntry createSearchResultEntry(String entityId, String instanceName, String entityName, SearchHit searchHit) {
        Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
        List<FieldHit> fieldHits = new ArrayList<>();
        highlightFields.forEach((f, h) -> {
            if (isDisplayedField(f)) {
                String highlight = Arrays.stream(h.getFragments()).map(Text::toString).collect(Collectors.joining("..."));
                fieldHits.add(new FieldHit(formatFieldName(f), highlight));
            }
        });
        return new SearchResultEntry(entityId, instanceName, entityName, fieldHits);
    }

    protected boolean isDisplayedField(String fieldName) {
        return !Constants.INSTANCE_NAME_FIELD.equals(fieldName);
    }

    protected Set<String> reloadIds(MetaClass metaClass, Collection<Object> entityIds) {
        Set<String> result = new HashSet<>();
        String primaryKeyName = metadataTools.getPrimaryKeyName(metaClass);
        for (Collection<Object> idsPartition : Iterables.partition(entityIds, searchProperties.getSearchReloadEntitiesBatchSize())) {
            log.debug("Load instance names for ids: {}", idsPartition);

            List<Object> partitionResult;
            if (metadataTools.hasCompositePrimaryKey(metaClass)) {
                partitionResult = idsPartition.stream()
                        .map(id -> secureDataManager
                                .load(metaClass.getJavaClass())
                                .id(id)
                                .fetchPlanProperties(primaryKeyName)
                                .optional())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
            } else {
                partitionResult = secureDataManager
                        .load(metaClass.getJavaClass())
                        .query("select e from " + metaClass.getName() + " e where e." + primaryKeyName + " in :ids")
                        .parameter("ids", idsPartition)
                        .fetchPlanProperties(primaryKeyName)
                        .list();
            }

            partitionResult.stream()
                    .map(instance -> idSerialization.idToString(Id.of(instance)))
                    .forEach(result::add);
        }
        return result;
    }

    protected String formatFieldName(String fieldName) {
        return StringUtils.removeEnd(fieldName, "." + Constants.INSTANCE_NAME_FIELD);
    }
}
