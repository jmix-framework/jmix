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

package io.jmix.searchelasticsearch.index.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import io.jmix.core.*;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexResult;
import io.jmix.search.index.RefreshPolicy;
import io.jmix.search.index.impl.BaseEntityIndexer;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ElasticsearchEntityIndexer extends BaseEntityIndexer {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchEntityIndexer.class);

    protected final ElasticsearchClient client;

    public ElasticsearchEntityIndexer(UnconstrainedDataManager dataManager,
                                      FetchPlans fetchPlans,
                                      IndexConfigurationManager indexConfigurationManager,
                                      Metadata metadata,
                                      IdSerialization idSerialization,
                                      IndexStateRegistry indexStateRegistry,
                                      MetadataTools metadataTools,
                                      SearchProperties searchProperties,
                                      ElasticsearchClient client) {
        super(dataManager, fetchPlans, indexConfigurationManager, metadata,
                idSerialization, indexStateRegistry, metadataTools, searchProperties);
        this.client = client;
    }

    @Override
    protected IndexResult indexDocuments(List<IndexDocumentData> documents) {
        BulkRequest.Builder requestBuilder = new BulkRequest.Builder();
        documents.forEach(doc ->
                requestBuilder.operations(operationsBuilder ->
                        operationsBuilder.index(indexOperationBuilder ->
                                indexOperationBuilder.index(doc.indexName()).id(doc.id()).document(doc.source()))
                ));

        Refresh refresh = resolveRefresh();
        log.debug("Refresh: {}", refresh);
        BulkRequest request = requestBuilder.refresh(refresh).build();

        BulkResponse bulkResponse = executeBulkRequest(request);
        return createIndexResult(bulkResponse);
    }

    @Override
    protected IndexResult deleteByGroupedDocIds(Map<IndexConfiguration, Collection<String>> groupedDocIds) {
        BulkRequest.Builder requestBuilder = new BulkRequest.Builder();
        for (Map.Entry<IndexConfiguration, Collection<String>> entry : groupedDocIds.entrySet()) {
            IndexConfiguration indexConfiguration = entry.getKey();
            String indexName = indexConfiguration.getIndexName();
            Collection<String> docIds = entry.getValue();
            docIds.forEach(docId ->
                    requestBuilder.operations(operationsBuilder ->
                            operationsBuilder.delete(deleteOperationBuilder ->
                                    deleteOperationBuilder.index(indexName).id(docId))
                    )
            );
        }

        BulkResponse response = execute(requestBuilder);
        return createIndexResult(response);
    }

    protected BulkResponse execute(BulkRequest.Builder requestBuilder) {
        Refresh refresh = resolveRefresh();
        log.debug("Execute bulk request with Refresh: {}", refresh);
        BulkRequest request = requestBuilder.refresh(refresh).build();

        return executeBulkRequest(request);
    }

    protected BulkResponse executeBulkRequest(BulkRequest request) {
        if (request.operations().isEmpty()) {
            log.debug("Bulk request has no operations");
            return createNoopBulkResponse();
        }

        try {
            BulkResponse response = client.bulk(request);
            log.debug("Bulk response: took = {}, has failures = {}", response.took(), response.errors());
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Bulk request failed", e);
        }
    }

    protected BulkResponse createNoopBulkResponse() {
        return BulkResponse.of(builder ->
                builder.items(Collections.emptyList())
                        .took(0L)
                        .errors(false)
        );
    }

    protected IndexResult createIndexResult(BulkResponse response) {
        List<IndexResult.Failure> failures;
        if (response.errors()) {
            failures = response.items().stream()
                    .filter(item -> item.error() != null)
                    .map(item -> {
                        String id = item.id();
                        String index = item.index();
                        ErrorCause error = item.error();
                        if (StringUtils.isEmpty(id)) {
                            return null;
                        } else {
                            return new IndexResult.Failure(id, index, error.reason() == null ? "" : error.reason());
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } else {
            failures = Collections.emptyList();
        }

        return new IndexResult(response.items().size(), failures);
    }

    protected Refresh resolveRefresh() {
        RefreshPolicy refreshPolicy = searchProperties.getBulkRequestRefreshPolicy();
        switch (refreshPolicy) {
            case TRUE -> {
                return Refresh.True;
            }
            case WAIT_FOR -> {
                return Refresh.WaitFor;
            }
            default -> {
                return Refresh.False;
            }
        }
    }




    /*@Override
    public IndexResult index(Object entityInstance) {
        return indexCollection(Collections.singletonList(entityInstance));
    }

    @Override
    public IndexResult indexCollection(Collection<Object> entityInstances) {
        Map<IndexConfiguration, Collection<Object>> groupedInstances = prepareInstancesForIndexing(entityInstances);
        return indexGroupedInstances(groupedInstances);
    }

    @Override
    public IndexResult indexByEntityId(Id<?> entityId) {
        return indexCollectionByEntityIds(Collections.singletonList(entityId));
    }

    @Override
    public IndexResult indexCollectionByEntityIds(Collection<Id<?>> entityIds) {
        Map<IndexConfiguration, Collection<Object>> groupedInstances = prepareInstancesForIndexingByIds(entityIds);
        return indexGroupedInstances(groupedInstances);
    }

    @Override
    public IndexResult delete(Object entityInstance) {
        return deleteCollection(Collections.singletonList(entityInstance));
    }

    @Override
    public IndexResult deleteCollection(Collection<Object> entityInstances) {
        Map<IndexConfiguration, Collection<String>> groupedIndexIds = prepareIndexIdsByEntityInstances(entityInstances);
        return deleteByGroupedDocIds(groupedIndexIds);
    }

    @Override
    public IndexResult deleteByEntityId(Id<?> entityId) {
        return deleteCollectionByEntityIds(Collections.singletonList(entityId));
    }

    @Override
    public IndexResult deleteCollectionByEntityIds(Collection<Id<?>> entityIds) {
        Map<IndexConfiguration, Collection<String>> groupedIndexIds = prepareIndexIdsByEntityIds(entityIds);
        return deleteByGroupedDocIds(groupedIndexIds);
    }*/

    /*@Override
    protected IndexResult indexDocuments(List<IndexDocumentData> documents) {
        BulkRequest request = new BulkRequest();
        documents.forEach(doc -> {
            try {
                request.add(new IndexRequest()
                        .index(doc.indexName())
                        .id(doc.id())
                        .source(objectMapper.writeValueAsString(doc.source()), XContentType.JSON));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to create index request: unable to parse source object", e);
            }
        });

        BulkResponse response = executeBulkRequest(request);
        return createIndexResult(response);
    }

    @Override
    protected IndexResult deleteByGroupedDocIds(Map<IndexConfiguration, Collection<String>> groupedDocIds) {
        BulkRequest request = new BulkRequest();
        for (Map.Entry<IndexConfiguration, Collection<String>> entry : groupedDocIds.entrySet()) {
            IndexConfiguration indexConfiguration = entry.getKey();
            String indexName = indexConfiguration.getIndexName();
            Collection<String> docIds = entry.getValue();
            docIds.forEach(docId -> request.add(new DeleteRequest(indexName, docId)));
        }

        BulkResponse response = executeBulkRequest(request);
        return createIndexResult(response);
    }*/

    /*protected RefreshPolicy resolveRefresh() {
        RefreshPolicy unifiedRefresh = searchProperties.getElasticsearchBulkRequestRefreshPolicy();
        switch (unifiedRefresh) {
            case TRUE -> {
                return RefreshPolicy.IMMEDIATE;
            }
            case WAIT_FOR -> {
                return RefreshPolicy.WAIT_UNTIL;
            }
            default -> {
                return RefreshPolicy.NONE;
            }
        }
    }*/

    /*protected BulkResponse executeBulkRequest(BulkRequest request) {
        if (request.requests().isEmpty()) {
            log.debug("Bulk request has no operations");
            return createNoopBulkResponse();
        }

        try {
            RefreshPolicy refresh = resolveRefresh();
            log.debug("Execute bulk request with Refresh: {}", refresh);
            request.setRefreshPolicy(refresh);
            BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
            log.debug("Bulk response: took {}, status = {}, has failures = {}",
                    bulkResponse.getTook(), bulkResponse.status(), bulkResponse.hasFailures());
            return bulkResponse;
        } catch (IOException e) {
            throw new RuntimeException("Bulk request failed", e);
        }
    }

    protected IndexResult createIndexResult(BulkResponse response) {
        List<IndexResult.Failure> failures;
        if (response.hasFailures()) {
            failures = Stream.of(response.getItems())
                    .filter(BulkItemResponse::isFailed)
                    .map(item -> new IndexResult.Failure(item.getId(), item.getIndex(), item.getFailureMessage()))
                    .collect(Collectors.toList());


        } else {
            failures = Collections.emptyList();
        }
        return new IndexResult(response.getItems().length, failures);
    }

    protected BulkResponse createNoopBulkResponse() {
        return new BulkResponse(new BulkItemResponse[]{}, 0L);
    }*/
}
