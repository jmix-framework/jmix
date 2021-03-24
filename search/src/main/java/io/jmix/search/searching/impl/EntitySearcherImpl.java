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
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.SearchProperties;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.utils.PropertyTools;
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

@Component(EntitySearcher.NAME)
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
    protected SearchProperties searchProperties;
    @Autowired
    protected PropertyTools propertyTools;

    @Override
    public SearchResult search(String searchTerm) {
        //todo Currently it's a simple search over all fields of all search indices without any paging
        log.debug("Perform search by term '{}'", searchTerm);
        SearchRequest searchRequest = new SearchRequest("*_search_index");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(searchTerm, "*"));
        searchSourceBuilder.size(searchProperties.getEsSearchSize());

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("*");
        highlightBuilder.fragmentSize(10);
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
            return processHits(searchTerm, searchResponse.getHits());
        } catch (IOException e) {
            throw new RuntimeException("Search failed", e);
        }
    }

    protected SearchResult processHits(String searchTerm, SearchHits searchHits) {
        Map<String, List<SearchHit>> hitsByEntityName = Stream.of(searchHits.getHits())
                .collect(Collectors.groupingBy(hit -> {
                    Object metaObject = hit.getSourceAsMap().get("meta");
                    if (metaObject instanceof Map) {
                        return (String) ((Map<?, ?>) metaObject).get("entityClass");
                    } else {
                        throw new RuntimeException("Entity metadata not found in ES document " + hit.getIndex() + "/" + hit.getId());
                    }
                }));

        SearchResult searchResult = new SearchResult(searchTerm);
        for (Map.Entry<String, List<SearchHit>> entry : hitsByEntityName.entrySet()) {
            String entityName = entry.getKey();
            List<SearchHit> entityHits = entry.getValue();
            List<String> entityIds = entityHits.stream().map(SearchHit::getId).collect(Collectors.toList());
            Map<String, String> reloadedIdNames = loadEntityInstanceNames(metadata.getClass(entityName), entityIds);

            for (SearchHit searchHit : entityHits) {
                String entityId = searchHit.getId();
                if (reloadedIdNames.containsKey(entityId)) {
                    String instanceName = reloadedIdNames.get(entityId);
                    Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                    List<FieldHit> fieldHits = new ArrayList<>();
                    highlightFields.forEach((f, h) -> {
                        String highlight = Arrays.stream(h.getFragments()).map(Text::toString).collect(Collectors.joining("..."));
                        fieldHits.add(new FieldHit(formatFieldName(f), highlight));
                    });
                    searchResult.addEntry(new SearchResultEntry(entityId, instanceName, entityName, fieldHits));
                }
            }
        }

        return searchResult;
    }

    protected Map<String, String> loadEntityInstanceNames(MetaClass metaClass, List<String> entityIds) {
        String primaryKeyProperty = propertyTools.getPrimaryKeyPropertyNameForIndex(metaClass);
        Map<String, String> result = new HashMap<>();
        for (List<String> partition : Lists.partition(entityIds, searchProperties.getSearchReloadEntitiesBatchSize())) {
            log.debug("Load instance names for ids: {}", partition);
            List<Object> partitionResult = secureDataManager
                    .load(metaClass.getJavaClass())
                    .query(String.format("select e from %s e where e.%s in :ids", metaClass.getName(), primaryKeyProperty))
                    .parameter("ids", partition)
                    .fetchPlan(FetchPlan.INSTANCE_NAME)
                    .list();
            partitionResult.forEach(entity -> {
                Object primaryKeyValue = EntityValues.getValue(entity, primaryKeyProperty);
                if (primaryKeyValue == null) {
                    log.error("Primary key value is not set for entity {}", entity);
                } else {
                    String instanceName = instanceNameProvider.getInstanceName(entity);
                    result.put(primaryKeyValue.toString(), instanceName);
                }
            });
        }
        return result;
    }

    protected String formatFieldName(String fieldName) {
        return StringUtils.removeStart(StringUtils.removeEnd(fieldName, "._instance_name"), "content.");
    }

}
