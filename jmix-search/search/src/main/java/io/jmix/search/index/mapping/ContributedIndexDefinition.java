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

import java.util.function.Predicate;

/**
 * An index definition provided by an {@link IndexDefinitionContributor}.
 * <p>
 * Instances should be built using {@link #builder(String, MappingDefinition)}.
 * <p>
 * The index name, the extended search settings and the indexable predicate are used only when the
 * contribution creates a new index configuration. When the entity also has an annotated Java definition, the
 * values of the Java definition are kept.
 */
@NullMarked
public class ContributedIndexDefinition {

    protected final String entityName;
    protected final MappingDefinition mappingDefinition;
    @Nullable
    protected final String indexName;
    protected final ExtendedSearchSettings extendedSearchSettings;
    protected final Predicate<Object> indexablePredicate;

    protected ContributedIndexDefinition(Builder builder) {
        this.entityName = builder.entityName;
        this.mappingDefinition = builder.mappingDefinition;
        this.indexName = builder.indexName;
        this.extendedSearchSettings = builder.extendedSearchSettings;
        this.indexablePredicate = builder.indexablePredicate;
    }

    /**
     * Creates a builder of a definition.
     *
     * @param entityName        Jmix entity name
     * @param mappingDefinition the fields to index
     * @return builder
     */
    public static Builder builder(String entityName, MappingDefinition mappingDefinition) {
        return new Builder(entityName, mappingDefinition);
    }

    /**
     * @return Jmix entity name
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * @return the fields to index
     */
    public MappingDefinition getMappingDefinition() {
        return mappingDefinition;
    }

    /**
     * @return the pattern the index name of this entity is built from, or {@code null} to build it from the
     * application-wide pattern. The placeholders {@code {entityName}} and {@code {tenantId}} are replaced and
     * everything else is taken as written, so a value without placeholders is the index name itself. An entity
     * whose data is stored in a separate index per tenant must have {@code {tenantId}} in its pattern
     */
    @Nullable
    public String getIndexName() {
        return indexName;
    }

    /**
     * @return extended search settings of the index; disabled unless set in the builder
     */
    public ExtendedSearchSettings getExtendedSearchSettings() {
        return extendedSearchSettings;
    }

    /**
     * @return predicate that decides whether an instance is indexed; accepts every instance unless set in
     * the builder
     */
    public Predicate<Object> getIndexablePredicate() {
        return indexablePredicate;
    }

    public static class Builder {

        protected final String entityName;
        protected final MappingDefinition mappingDefinition;
        @Nullable
        protected String indexName;
        protected ExtendedSearchSettings extendedSearchSettings = ExtendedSearchSettings.empty();
        protected Predicate<Object> indexablePredicate = instance -> true;

        protected Builder(String entityName, MappingDefinition mappingDefinition) {
            Preconditions.checkNotNullArgument(entityName, "entityName is null");
            Preconditions.checkNotNullArgument(mappingDefinition, "mappingDefinition is null");
            this.entityName = entityName;
            this.mappingDefinition = mappingDefinition;
        }

        /**
         * Sets the pattern the index name of this entity is built from. By default, the application-wide pattern
         * is used.
         *
         * @param indexName index name pattern, or {@code null} to use the application-wide one. A value without
         *                  the {@code {entityName}} and {@code {tenantId}} placeholders is the index name itself;
         *                  an entity stored in a separate index per tenant must have {@code {tenantId}} in it
         * @return builder
         */
        public Builder withIndexName(@Nullable String indexName) {
            this.indexName = indexName;
            return this;
        }

        /**
         * Sets the extended search settings of the index. By default, extended search is disabled.
         *
         * @param extendedSearchSettings extended search settings
         * @return builder
         * @see io.jmix.search.index.annotation.ExtendedSearch
         */
        public Builder withExtendedSearchSettings(ExtendedSearchSettings extendedSearchSettings) {
            Preconditions.checkNotNullArgument(extendedSearchSettings, "extendedSearchSettings is null");
            this.extendedSearchSettings = extendedSearchSettings;
            return this;
        }

        /**
         * Sets the predicate that decides whether an instance is indexed. By default, every instance is indexed.
         *
         * @param indexablePredicate predicate that receives an entity instance
         * @return builder
         * @see io.jmix.search.index.annotation.IndexablePredicate
         */
        public Builder withIndexablePredicate(Predicate<Object> indexablePredicate) {
            Preconditions.checkNotNullArgument(indexablePredicate, "indexablePredicate is null");
            this.indexablePredicate = indexablePredicate;
            return this;
        }

        public ContributedIndexDefinition build() {
            return new ContributedIndexDefinition(this);
        }
    }
}
