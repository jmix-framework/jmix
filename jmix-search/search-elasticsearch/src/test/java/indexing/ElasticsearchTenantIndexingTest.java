/*
 * Copyright 2026 Haulmont.
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

package indexing;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AbstractTenantIndexingTest;
import test_support.ElasticsearchTenantIndexingTestConfiguration;
import test_support.ElasticsearchTestBulkRequestsTracker;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ElasticsearchTenantIndexingTestConfiguration.class})
public class ElasticsearchTenantIndexingTest extends AbstractTenantIndexingTest {

    @Autowired
    protected ElasticsearchTestBulkRequestsTracker bulkRequestsTracker;

    @Override
    protected List<String> targetIndexes() {
        return bulkRequestsTracker.getBulkRequests().stream()
                .flatMap(request -> request.operations().stream())
                .map(operation -> operation.index().index())
                .toList();
    }

    @Override
    protected void clearRequests() {
        bulkRequestsTracker.clear();
    }
}
