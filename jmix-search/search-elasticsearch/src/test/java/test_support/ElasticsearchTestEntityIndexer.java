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

package test_support;

import io.jmix.core.*;
import io.jmix.search.SearchProperties;

import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexResult;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.searchelasticsearch.index.impl.ElasticsearchEntityIndexer;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.bulk.OperationType;


import javax.annotation.Nonnull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Track incoming bulk requests via provided tracker instead of execution via ES client
 */
public class ElasticsearchTestEntityIndexer extends ElasticsearchEntityIndexer {

    protected final ElasticsearchTestBulkRequestsTracker bulkRequestsTracker;

    public ElasticsearchTestEntityIndexer(UnconstrainedDataManager dataManager,
                                          FetchPlans fetchPlans,
                                          IndexConfigurationManager indexConfigurationManager,
                                          Metadata metadata,
                                          IdSerialization idSerialization,
                                          IndexStateRegistry indexStateRegistry,
                                          MetadataTools metadataTools,
                                          SearchProperties searchProperties,
                                          ElasticsearchTestBulkRequestsTracker bulkRequestsTracker) {
        super(dataManager, fetchPlans, indexConfigurationManager, metadata, idSerialization, indexStateRegistry, metadataTools, searchProperties, null);
        this.bulkRequestsTracker = bulkRequestsTracker;
    }

    @Nonnull
    protected BulkResponse executeBulkRequest(@Nonnull BulkRequest request) {
        bulkRequestsTracker.accept(request);

        return new BulkResponse.Builder()
                .items(request.operations().stream().map((r) -> {
                    return new BulkResponseItem.Builder()
                            .id(r.index().id())
                            .index(r.index().index())
                            .operationType(OperationType.Index)
                            .result(r.index().document().toString())
                            .status(0)
                            .build();
                }).collect(Collectors.toList()))
                .took(0L)
                .errors(false).build();
    }
    @Override
    public IndexResult index(Object entityInstance) {
        return indexCollection(Collections.singletonList(entityInstance));
    }

    @Override
    protected IndexResult deleteByGroupedDocIds(Map<IndexConfiguration, Collection<String>> groupedDocIds) {
        return deleteByGroupedDocIds(groupedDocIds);
    }
}
