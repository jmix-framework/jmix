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

import org.jspecify.annotations.NullMarked;

import java.util.Collection;

/**
 * Contributes index definitions that are not declared as annotated Java interfaces, for example
 * definitions derived from runtime metadata. Implementations are Spring beans. They are consulted
 * every time {@link IndexConfigurationManager} builds the index configurations of a metadata
 * generation, so a contribution must describe the entities and properties of the generation that is
 * pinned or current for the calling thread.
 */
@NullMarked
public interface IndexDefinitionContributor {

    /**
     * Returns the definitions to add. A definition for an entity that also has an annotated Java
     * definition is appended to it: contributed fields that are not already mapped are added, fields
     * already mapped by the Java definition are kept as they are. A definition for an entity without a
     * Java definition creates a new index configuration.
     *
     * @return contributed definitions, empty when there is nothing to contribute
     */
    Collection<ContributedIndexDefinition> getIndexDefinitions();
}
