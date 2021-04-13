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

import com.google.common.collect.Lists;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.SearchApplicationProperties;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.searching.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("core_SecureDataManager")
    protected DataManager secureDataManager;
    @Autowired
    protected InstanceNameProvider instanceNameProvider;
    @Autowired
    protected SearchApplicationProperties searchApplicationProperties;
    @Autowired
    protected IdSerialization idSerialization;

    @Override
    public SearchResult search(String searchTerm, SearchContext searchContext) {
        //todo Currently it's a simple search over all fields of all search indices without any paging
        log.debug("Perform search by term '{}' and with details: {}", searchTerm, searchContext);
        SearchRequest searchRequest = createSearchRequest(searchTerm, searchContext);

        SearchResultImpl searchResultImpl = new SearchResultImpl(searchTerm, searchContext);
        boolean moreDataAvailable;
        do {
            searchRequest.source().from(searchResultImpl.getEffectiveOffset());

            SearchResponse searchResponse;
            try {
                searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                throw new RuntimeException("Search failed", e);
            }
            SearchHits searchHits = searchResponse.getHits();
            Map<MetaClass, List<SearchHit>> hitsByEntityName = groupSearchHitsByEntity(searchHits);
            fillSearchResult(searchResultImpl, hitsByEntityName);

            long totalHits = searchResponse.getHits().getTotalHits().value;
            moreDataAvailable = (totalHits - searchResultImpl.getEffectiveOffset()) > 0;
        } while (moreDataAvailable && !isResultFull(searchResultImpl, searchContext));
        searchResultImpl.setMoreDataAvailable(moreDataAvailable);
        return searchResultImpl;
    }

    protected SearchRequest createSearchRequest(String searchTerm, SearchContext searchContext) {
        SearchRequest searchRequest = new SearchRequest("search_index_*");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(searchTerm, "*"));
        searchSourceBuilder.size(searchContext.getSize());

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("*");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);

        return searchRequest;
    }

    protected boolean isResultFull(SearchResultImpl searchResultImpl, SearchContext searchSettings) {
        return searchResultImpl.getSize() >= searchSettings.getSize();
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
        for (List<Object> idsPartition : Lists.partition(entityIds, searchApplicationProperties.getSearchReloadEntitiesBatchSize())) {
            log.debug("Load instance names for ids: {}", idsPartition);
            List<Object> partitionResult = secureDataManager
                    .load(metaClass.getJavaClass())
                    .ids(idsPartition)
                    .fetchPlan(FetchPlan.INSTANCE_NAME)
                    .list();
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
