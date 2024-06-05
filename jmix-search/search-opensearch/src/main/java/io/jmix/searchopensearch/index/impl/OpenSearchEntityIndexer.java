/*
 * Copyright 2024 Haulmont.
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

package io.jmix.searchopensearch.index.impl;

import io.jmix.core.*;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexResult;
import io.jmix.search.index.RefreshPolicy;
import io.jmix.search.index.impl.BaseEntityIndexer;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Implementation for OpenSearch
 */
public class OpenSearchEntityIndexer extends BaseEntityIndexer {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchEntityIndexer.class);

    protected final OpenSearchClient client;

    public OpenSearchEntityIndexer(UnconstrainedDataManager dataManager,
                                   FetchPlans fetchPlans,
                                   IndexConfigurationManager indexConfigurationManager,
                                   Metadata metadata,
                                   IdSerialization idSerialization,
                                   IndexStateRegistry indexStateRegistry,
                                   MetadataTools metadataTools,
                                   SearchProperties searchProperties,
                                   OpenSearchClient client) {
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
                            return new IndexResult.Failure(id, index, error.reason());
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
}
