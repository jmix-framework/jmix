/*
 * Copyright 2021 Haulmont.
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

/*import io.jmix.core.*;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.impl.ElasticSearchEntityIndexer;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import jakarta.annotation.Nonnull;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RestHighLevelClient;*/

/**
 * Track incoming bulk requests via provided tracker instead of execution via ES client
 */
/*public class TestElasticSearchEntityIndexer extends ElasticSearchEntityIndexer { //todo

    protected final TestBulkRequestsTracker bulkRequestsTracker;


    public TestElasticSearchEntityIndexer(TestBulkRequestsTracker bulkRequestsTracker) {
        this.bulkRequestsTracker = bulkRequestsTracker;
    }


    public TestElasticSearchEntityIndexer(UnconstrainedDataManager dataManager,
                                          FetchPlans fetchPlans,
                                          IndexConfigurationManager indexConfigurationManager,
                                          Metadata metadata,
                                          IdSerialization idSerialization,
                                          IndexStateRegistry indexStateRegistry,
                                          MetadataTools metadataTools,
                                          SearchProperties searchProperties,
                                          RestHighLevelClient client,
                                          TestBulkRequestsTracker bulkRequestsTracker) {
        super(dataManager, fetchPlans, indexConfigurationManager, metadata, idSerialization, indexStateRegistry, metadataTools, searchProperties, client);
        this.bulkRequestsTracker = bulkRequestsTracker;
    }

    @Override
    @Nonnull
    protected BulkResponse executeBulkRequest(@Nonnull BulkRequest request) {
        bulkRequestsTracker.accept(request);
        return new BulkResponse(new BulkItemResponse[]{}, 0);
    }
}*/
