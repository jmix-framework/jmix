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

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AbstractTenantIsolationEngineTest;
import test_support.OpenSearchEngineTestConfiguration;

import java.io.IOException;
import java.util.List;

@Tag("search-engine")
// The shared properties switch change tracking off - the other scenarios drive the indexer directly - but the
// restoration scenario is about a record travelling from the listener through the queue on its own.
@TestPropertySource(properties = "jmix.search.changed-entities-indexing-enabled=true")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OpenSearchEngineTestConfiguration.class})
public class OpenSearchTenantIsolationEngineTest extends AbstractTenantIsolationEngineTest {

    @Autowired
    protected OpenSearchClient client;

    @Override
    protected List<String> instanceNamesIn(String indexName) throws IOException {
        SearchResponse<JsonNode> response = client.search(request -> request.index(indexName), JsonNode.class);
        return response.hits().hits().stream()
                .map(hit -> hit.source() == null ? "" : hit.source().path("_instance_name").asText())
                .sorted()
                .toList();
    }

    /**
     * Refreshes every index of the application rather than a fixed pair: a scenario that works with one tenant
     * leaves the indexes of the others uncreated, and naming them explicitly fails on whichever is absent.
     */
    @Override
    protected void refresh() throws IOException {
        client.indices().refresh(request -> request.index("search_index_*").ignoreUnavailable(true));
    }
}
