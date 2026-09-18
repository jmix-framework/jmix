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

package io.jmix.search.index.mapping;

import io.jmix.core.common.util.Preconditions;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * An index definition provided by an {@link IndexDefinitionContributor}.
 *
 * @param entityName        Jmix entity name
 * @param indexName         explicit index name, or {@code null} to derive it from the entity name. Honoured only
 *                          when the contribution creates a new configuration; otherwise the existing index name is
 *                          kept and this one is dropped.
 * @param mappingDefinition the fields to index
 */
@NullMarked
public record ContributedIndexDefinition(String entityName,
                                         @Nullable String indexName,
                                         MappingDefinition mappingDefinition) {

    public ContributedIndexDefinition {
        Preconditions.checkNotNullArgument(entityName, "entityName is null");
        Preconditions.checkNotNullArgument(mappingDefinition, "mappingDefinition is null");
    }
}
