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

import io.jmix.search.index.mapping.IndexMappingConfiguration;
import org.elasticsearch.common.settings.Settings;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Contains configuration of index related to some entity
 */
public class IndexConfiguration {

    protected final String entityName;

    protected final Class<?> entityClass;

    protected final Set<Class<?>> affectedEntityClasses;

    protected final String indexName;

    protected final IndexMappingConfiguration mapping;

    protected final Settings settings;

    protected final Predicate<Object> indexablePredicate;

    public IndexConfiguration(String entityName,
                              Class<?> entityClass,
                              String indexName,
                              IndexMappingConfiguration mapping,
                              Settings settings,
                              Set<Class<?>> affectedEntityClasses,
                              Predicate<Object> indexablePredicate) {
        this.entityName = entityName;
        this.entityClass = entityClass;
        this.indexName = indexName;
        this.mapping = mapping;
        this.settings = settings;
        this.affectedEntityClasses = affectedEntityClasses;
        this.indexablePredicate = indexablePredicate;
    }

    /**
     * Gets name of entity indexed in this index
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
     * Gets name of this index
     *
     * @return index name
     */
    public String getIndexName() {
        return indexName;
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
     * Gets settings of this index
     *
     * @return settings
     */
    public Settings getSettings() {
        return settings;
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
}
