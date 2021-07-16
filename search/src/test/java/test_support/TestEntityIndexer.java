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

import io.jmix.search.index.impl.EntityIndexerImpl;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;

import javax.annotation.Nonnull;

/**
 * Track incoming bulk requests via provided tracker instead of execution via ES client
 */
public class TestEntityIndexer extends EntityIndexerImpl {

    protected final TestBulkRequestsTracker bulkRequestsTracker;

    public TestEntityIndexer(TestBulkRequestsTracker bulkRequestsTracker) {
        this.bulkRequestsTracker = bulkRequestsTracker;
    }

    @Override
    @Nonnull
    protected BulkResponse executeBulkRequest(@Nonnull BulkRequest request) {
        bulkRequestsTracker.accept(request);
        return new BulkResponse(new BulkItemResponse[]{}, 0);
    }
}
