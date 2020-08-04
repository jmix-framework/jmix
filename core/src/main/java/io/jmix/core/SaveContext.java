/*
 * Copyright 2019 Haulmont.
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
package io.jmix.core;


import io.jmix.core.constraint.AccessConstraint;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

/**
 *
 */
public class SaveContext implements Serializable {

    private static final long serialVersionUID = 7239959802146936706L;

    protected Collection<JmixEntity> entitiesToSave = new LinkedHashSet<>();
    protected Collection<JmixEntity> entitiesToRemove = new LinkedHashSet<>();

    protected Map<Object, FetchPlan> fetchPlans = new HashMap<>();

    protected boolean softDeletion = true;
    protected boolean discardSaved;
    protected boolean joinTransaction = true;
    protected List<AccessConstraint<?>> accessConstraints;
    protected Map<String, Object> dbHints = new HashMap<>();

    /**
     * Adds an entity to be committed to the database.
     *
     * @param entities collection of entities
     * @return this instance for chaining
     */
    public SaveContext saving(Collection<? extends JmixEntity> entities) {
        entitiesToSave.addAll(entities);
        return this;
    }

    /**
     * /**
     * Adds an entity to be committed to the database.
     *
     * @param entity entity instance
     * @return this instance for chaining
     */
    public SaveContext saving(JmixEntity... entity) {
        entitiesToSave.addAll(Arrays.asList(entity));
        return this;
    }

    /**
     * Adds an entity to be committed to the database.
     *
     * @param entity    entity instance
     * @param fetchPlan fetch plan which is used in merge operation to ensure all required attributes are loaded in the returned instance
     * @return this instance for chaining
     */
    public SaveContext saving(JmixEntity entity, @Nullable FetchPlan fetchPlan) {
        entitiesToSave.add(entity);
        if (fetchPlan != null)
            fetchPlans.put(entity, fetchPlan);
        return this;
    }

    /**
     * Adds an entity to be removed from the database.
     *
     * @param entity entity instance
     * @return this instance for chaining
     */
    public SaveContext removing(JmixEntity... entity) {
        entitiesToRemove.addAll(Arrays.asList(entity));
        return this;
    }

    /**
     * Adds an entity to be removed from the database.
     *
     * @param entities collection of entities
     * @return this instance for chaining
     */
    public SaveContext removing(Collection<? extends JmixEntity> entities) {
        entitiesToRemove.addAll(entities);
        return this;
    }

    /**
     * @return direct reference to collection of changed entities that will be committed to the database.
     * The collection is modifiable.
     */
    public Collection<JmixEntity> getEntitiesToSave() {
        return entitiesToSave;
    }

    /**
     * @return direct reference to collection of entities that will be removed from the database.
     * The collection is modifiable.
     */
    public Collection<JmixEntity> getEntitiesToRemove() {
        return entitiesToRemove;
    }

    /**
     * Enables defining a view for each committed entity. These views are used in merge operation to ensure all
     * required attributes are loaded in returned instances.
     *
     * @return mutable map of entities to their views
     */
    public Map<Object, FetchPlan> getFetchPlans() {
        return fetchPlans;
    }

    public SaveContext setDbHint(String name, Object value) {
        dbHints.put(name, value);
        return this;
    }

    /**
     * @return custom hints which can be used later during query construction
     */
    public Map<String, Object> getDbHints() {
        return dbHints;
    }

    /**
     * @return whether to use soft deletion for this commit
     */
    public boolean isSoftDeletion() {
        return softDeletion;
    }

    /**
     * @param softDeletion whether to use soft deletion for this commit
     */
    public SaveContext setSoftDeletion(boolean softDeletion) {
        this.softDeletion = softDeletion;
        return this;
    }

    /**
     * @return true if calling code does not need committed instances, which allows for performance optimization
     */
    public boolean isDiscardSaved() {
        return discardSaved;
    }

    /**
     * Set to true if calling code does not need saved instances, which allows for performance optimization.
     */
    public SaveContext setDiscardSaved(boolean discardSaved) {
        this.discardSaved = discardSaved;
        return this;
    }

    public List<AccessConstraint<?>> getAccessConstraints() {
        return this.accessConstraints == null ? Collections.emptyList() : this.accessConstraints;
    }

    public SaveContext setAccessConstraints(List<AccessConstraint<?>> accessConstraints) {
        this.accessConstraints = accessConstraints;
        return this;
    }

    public boolean isJoinTransaction() {
        return joinTransaction;
    }

    public SaveContext setJoinTransaction(boolean joinTransaction) {
        this.joinTransaction = joinTransaction;
        return this;
    }
}
