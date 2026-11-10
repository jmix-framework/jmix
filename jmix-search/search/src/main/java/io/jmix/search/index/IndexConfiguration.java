/*
 * Copyright 2020 Haulmont.
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

package io.jmix.search.index;

import io.jmix.search.index.mapping.ExtendedSearchSettings;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import org.jspecify.annotations.Nullable;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Describes how an entity is indexed - not a single index.
 * <p>
 * The data of a tenant-aware entity is stored in one index per tenant, so a configuration corresponds to as many
 * physical indexes as the application has tenants. The name of an index is not held here at all: it is worked out
 * from the configuration and a tenant by {@link io.jmix.search.index.impl.IndexLayout}.
 * <p>
 * The name of this class is older than that and reads as "configuration of an index". Renaming it to
 * {@code EntityIndexingConfiguration} cannot be softened: Java has no alias for a class, and this type stands both
 * in arguments and in return values, so every rename is a compile-time break. A break like that belongs to a major
 * version, where it needs no bridge at all.
 */
//TODO rename to EntityIndexingConfiguration in 4.0
public class IndexConfiguration {

    protected final String entityName;

    protected final Class<?> entityClass;

    protected final Set<Class<?>> affectedEntityClasses;

    protected final IndexMappingConfiguration mapping;

    protected final Predicate<Object> indexablePredicate;

    protected final ExtendedSearchSettings extendedSearchSettings;

    protected final boolean tenantAware;

    @Nullable
    protected final String indexNamePattern;

    public IndexConfiguration(String entityName,
                              Class<?> entityClass,
                              IndexMappingConfiguration mapping,
                              Set<Class<?>> affectedEntityClasses,
                              Predicate<Object> indexablePredicate,
                              ExtendedSearchSettings extendedSearchSettings,
                              boolean tenantAware,
                              @Nullable String indexNamePattern) {
        this.entityName = entityName;
        this.entityClass = entityClass;
        this.mapping = mapping;
        this.affectedEntityClasses = Set.copyOf(affectedEntityClasses);
        this.indexablePredicate = indexablePredicate;
        this.extendedSearchSettings = extendedSearchSettings;
        this.tenantAware = tenantAware;
        this.indexNamePattern = indexNamePattern;
    }

    /**
     * Gets entity name of entity indexed in this index
     *
     * @return entity name
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Gets java class of entity indexed in this index
     *
     * @return java class
     */
    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * Indicates whether the indexed entity has an attribute annotated with
     * {@link io.jmix.core.annotation.TenantId}.
     * <p>
     * This is a structural fact about the entity, not a decision about index layout: whether the index is actually
     * split per tenant additionally depends on the application configuration.
     *
     * @return true if the indexed entity has a tenant attribute
     */
    public boolean isTenantAware() {
        return tenantAware;
    }

    /**
     * Returns the pattern that names the indexes of this entity, as written in
     * {@link io.jmix.search.index.annotation.JmixEntitySearchIndex#indexName()}.
     * <p>
     * It overrides the application-wide pattern taken from the properties. A pattern without placeholders is the
     * index name itself.
     *
     * @return pattern of the index name, or null if the entity has none of its own
     */
    @Nullable
    public String getIndexNamePattern() {
        return indexNamePattern;
    }

    /**
     * Gets mapping of this index
     *
     * @return mapping configuration
     */
    public IndexMappingConfiguration getMapping() {
        return mapping;
    }

    /**
     * Gets java classes of all entities presented in indexed properties. Transitive entities are included too.
     *
     * @return set of java classes
     */
    public Set<Class<?>> getAffectedEntityClasses() {
        return affectedEntityClasses;
    }

    /**
     * Gets {@link Predicate}&lt;{@link Object}&gt; that will be applied to every entity instance during indexing process.
     * Only instances passed the predicate check will be indexed.
     * Predicate is not used during deletion process.
     *
     * @return indexable predicate
     */
    public Predicate<Object> getIndexablePredicate() {
        return indexablePredicate;
    }

    public ExtendedSearchSettings getExtendedSearchSettings() {
        return extendedSearchSettings;
    }

    @Override
    public String toString() {
        return "IndexConfiguration{" +
               "entityName='" + entityName + '\'' +
               ", entityClass=" + entityClass +
               ", affectedEntityClasses=" + affectedEntityClasses +
               ", mapping=" + mapping +
               ", indexablePredicate=" + indexablePredicate +
               ", extendedSearchSettings=" + extendedSearchSettings +
               ", tenantAware=" + tenantAware +
               ", indexNamePattern='" + indexNamePattern + '\'' +
               '}';
    }
}
