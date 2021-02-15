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

package io.jmix.search;

import com.google.common.collect.Lists;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
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

@Component
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    protected RestHighLevelClient esClient;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private Metadata metadata;
    @Autowired
    @Qualifier("core_SecureDataManager")
    private DataManager dataManager;
    @Autowired
    private InstanceNameProvider instanceNameProvider;

    public SearchResult search(String searchTerm) {
        //todo Currently it's a simple search over all fields of all search indices without any paging
        log.debug("Perform search by term '{}'", searchTerm);
        SearchRequest searchRequest = new SearchRequest("*_search_index");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(searchTerm, "*"));
        searchSourceBuilder.size(100); //todo property

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("*");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
            return processHits(searchTerm, searchResponse.getHits());
        } catch (IOException e) {
            throw new RuntimeException("Search failed", e);
        }
    }

    //todo Use this on indexing step. Move to some utils class?
    public String getPrimaryKeyPropertyNameForSearch(MetaClass metaClass) {
        //todo
        String primaryKeyPropertyName = metadataTools.getPrimaryKeyName(metaClass);
        if(primaryKeyPropertyName == null) {
            if (metadataTools.hasCompositePrimaryKey(metaClass) && metadataTools.hasUuid(metaClass)) {
                primaryKeyPropertyName = metadataTools.getUuidPropertyName(metaClass.getJavaClass());
                if(primaryKeyPropertyName == null) {
                    throw new RuntimeException("Primary key property is null");
                }
            } else {
                throw new RuntimeException("Proper primary key property not found for entity " + metaClass.getName());
            }
        }
        return primaryKeyPropertyName;
    }

    protected SearchResult processHits(String searchTerm, SearchHits searchHits) {
        Map<String, List<SearchHit>> hitsByEntityClass = Stream.of(searchHits.getHits())
                .collect(Collectors.groupingBy(hit -> {
                    Object metaObject = hit.getSourceAsMap().get("meta");
                    if (metaObject instanceof Map) {
                        return (String) ((Map<?, ?>) metaObject).get("entityClass");
                    } else {
                        throw new RuntimeException("Entity metadata not found in ES document " + hit.getIndex() + "/" + hit.getId());
                    }
                }));

        SearchResult searchResult = new SearchResult(searchTerm);
        for(Map.Entry<String, List<SearchHit>> entry : hitsByEntityClass.entrySet()) {
            String entityClass = entry.getKey();
            List<SearchHit> entityHits = entry.getValue();
            List<String> entityIds = entityHits.stream().map(SearchHit::getId).collect(Collectors.toList());
            Map<String, String> idNames = loadEntityInstanceNames(metadata.getClass(entityClass), entityIds);

            for(SearchHit searchHit : entityHits) {
                String entityId = searchHit.getId();
                String instanceName = idNames.get(entityId);
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                List<FieldHit> fieldHits = new ArrayList<>();
                highlightFields.forEach((f, h) -> {
                    String highlight = Arrays.stream(h.getFragments()).map(Text::toString).collect(Collectors.joining("..."));
                    fieldHits.add(new FieldHit(f.substring(8), highlight)); //todo substring - remove leading 'content.'
                });
                searchResult.addEntry(new SearchResultEntry(entityId, instanceName, entityClass, fieldHits));
            }
        }

        return searchResult;
    }

    protected Map<String, String> loadEntityInstanceNames(MetaClass metaClass, List<String> entityIds) {
        String primaryKeyProperty = getPrimaryKeyPropertyNameForSearch(metaClass);
        Map<String, String> result = new HashMap<>();
        int loadEntityInstanceNamesBatchSize = 100; //todo property
        for (List<String> partition : Lists.partition(entityIds, loadEntityInstanceNamesBatchSize)) {
            log.debug("Load instance names for ids: {}", partition);
            List<Object> partitionResult = dataManager
                    .load(metaClass.getJavaClass())
                    .query(String.format("select e from %s e where e.%s in :ids", metaClass.getName(), primaryKeyProperty))
                    .parameter("ids", partition)
                    .fetchPlan(FetchPlan.INSTANCE_NAME)
                    .list();
            partitionResult.forEach(entity -> {
                Object primaryKeyValue = EntityValues.getValue(entity, primaryKeyProperty);
                if(primaryKeyValue == null) {
                    log.error("Primary key value is not set for entity {}", entity);
                } else {
                    String instanceName = instanceNameProvider.getInstanceName(entity);
                    result.put(primaryKeyValue.toString(), instanceName);
                }
            });
        }
        return result;
    }

    public static class SearchResultEntry {
        private final String docId;
        private final String instanceName;
        private final String entityClass;
        private final List<FieldHit> fieldHits;

        public SearchResultEntry(String docId, String instanceName, String entityClass, List<FieldHit> fieldHits) {
            this.docId = docId;
            this.instanceName = instanceName;
            this.entityClass = entityClass;
            this.fieldHits = fieldHits;
        }

        public String getDocId() {
            return docId;
        }

        public String getInstanceName() {
            return instanceName;
        }

        public String getEntityClass() {
            return entityClass;
        }

        public List<FieldHit> getFieldHits() {
            return fieldHits;
        }
    }

    public static class FieldHit {
        private final String fieldName;
        private final String highlights;

        public FieldHit(String fieldName, String highlights) {
            this.fieldName = fieldName;
            this.highlights = highlights;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getHighlights() {
            return highlights;
        }
    }

    public static class SearchResult {
        protected final String searchTerm;
        protected final Map<String, Set<SearchResultEntry>> entriesByEntityClassName = new HashMap<>();

        public SearchResult(String searchTerm) {
            this.searchTerm = searchTerm;
        }

        public void addEntry(SearchResultEntry searchResultEntry) {
            Set<SearchResultEntry> entriesForEntityClassName = entriesByEntityClassName.computeIfAbsent(
                    searchResultEntry.getEntityClass(),
                    s -> new LinkedHashSet<>()
            );
            entriesForEntityClassName.add(searchResultEntry);
        }

        public Set<SearchResultEntry> getEntriesByEntityClassName(String entityClassName) {
            return entriesByEntityClassName.get(entityClassName);
        }

        public boolean isEmpty() {
            return entriesByEntityClassName.isEmpty();
        }

        public Collection<String> getEntityClassNames() {
            return entriesByEntityClassName.keySet();
        }
    }

    protected class EntityMetadata {

    }
}
