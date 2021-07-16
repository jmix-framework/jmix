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

import org.elasticsearch.action.bulk.BulkRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TestBulkRequestsTracker implements Consumer<BulkRequest> {

    private static final Logger log = LoggerFactory.getLogger(TestBulkRequestsTracker.class);

    private Set<BulkRequest> registry = ConcurrentHashMap.newKeySet();

    @Override
    public void accept(BulkRequest request) {
        log.info("Accept bulk request with '{}' actions within indexes: {}", request.numberOfActions(), request.getIndices());
        registry.add(request);
    }

    public List<BulkRequest> getBulkRequests() {
        return new ArrayList<>(registry);
    }

    public void clear() {
        registry = ConcurrentHashMap.newKeySet();
    }
}
