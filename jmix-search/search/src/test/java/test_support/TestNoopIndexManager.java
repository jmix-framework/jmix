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

package test_support;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.BaseIndexManager;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class TestNoopIndexManager extends BaseIndexManager {

    protected TestNoopIndexManager(IndexConfigurationManager indexConfigurationManager,
                                   IndexStateRegistry indexStateRegistry,
                                   SearchProperties searchProperties) {
        super(indexConfigurationManager, null, null, null, null);
    }

    @Override
    public boolean createIndex(IndexConfiguration indexConfiguration) {
        return true;
    }

    @Override
    public boolean dropIndex(String indexName) {
        return true;
    }

    @Override
    public boolean isIndexExist(String indexName) {
        return true;
    }

    @Override
    public ObjectNode getIndexMetadata(String indexName) {
        return objectMapper.createObjectNode();
    }

    @Override
    public boolean putMapping(String indexName, IndexMappingConfiguration mapping) {
        return false;
    }
}
