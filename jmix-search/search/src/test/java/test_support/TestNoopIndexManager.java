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

import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexManipulationResult;
import io.jmix.search.index.IndexOperationResult;
import io.jmix.search.index.impl.NoopIndexManager;
import org.jspecify.annotations.NullMarked;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Index manager that performs no actual operations but reports them as successful.
 * <p>
 * Unlike platform-specific managers it doesn't depend on index state and configuration comparing,
 * so it can be used in tests of the platform-independent module.
 */
@NullMarked
public class TestNoopIndexManager extends NoopIndexManager {

    public TestNoopIndexManager() {
        super();
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> createIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId) {
        return createResult(indexConfigurations, IndexManipulationResult.SUCCESS);
    }

    @Override
    public boolean dropIndex(String indexName) {
        return true;
    }

    @Override
    public boolean isIndexExist(String indexName) {
        return true;
    }

}
