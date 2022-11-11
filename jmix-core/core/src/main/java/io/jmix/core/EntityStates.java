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

import com.google.common.collect.Sets;
import io.jmix.core.common.util.StackTrace;
import io.jmix.core.entity.EntityPreconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.core.entity.EntitySystemAccess.getUncheckedEntityEntry;

/**
 * Provides information about entities states.
 */
@Component("core_EntityStates")
public class EntityStates {

    @Autowired
    protected PersistentAttributesLoadChecker checker;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected FetchPlans fetchPlans;

    private static final Logger log = LoggerFactory.getLogger(EntityStates.class);

    /**
     * Determines whether the instance is <em>New</em>, i.e. just created and not stored in database yet.
     *
     * @param entity entity instance
     * @return - true if the instance is a new persistent entity, or if it is actually in Managed state
     * but newly-persisted in this transaction <br>
     * - true if the instance is a new non-persistent entity never returned from DataManager <br>
     * - false otherwise
     * @throws IllegalArgumentException if entity instance is null
     */
    public boolean isNew(Object entity) {
        checkNotNullArgument(entity, "entity is null");
        if (entity instanceof Entity) {
            return getUncheckedEntityEntry(entity).isNew();
        } else {
            if (log.isTraceEnabled()) {
                log.trace("EntityStates.isNew is called for unsupported type '{}'. Stacktrace:\n{}",
                        entity.getClass().getSimpleName(), StackTrace.asString());
            }
        }
        return false;
    }

    /**
     * Determines whether the instance is <em>Managed</em>, i.e. attached to a persistence context.
     *
     * @param entity entity instance
     * @return - true if the instance is managed,<br>
     * - false if it is New (and not yet persisted) or Detached, or if it is not a persistent entity
     * @throws IllegalArgumentException if entity instance is null
     */
    public boolean isManaged(Object entity) {
        checkNotNullArgument(entity, "entity is null");
        if (entity instanceof Entity) {
            return getUncheckedEntityEntry(entity).isManaged();
        } else {
            if (log.isTraceEnabled()) {
                log.trace("EntityStates.isManaged is called for unsupported type '{}'. Stacktrace:\n{}",
                        entity.getClass().getSimpleName(), StackTrace.asString());
            }
        }
        return false;
    }

    /**
     * Determines whether the instance is <em>Detached</em>, i.e. stored in database but not attached to a persistence
     * context at the moment.
     *
     * @param entity entity instance
     * @return - true if the instance is detached,<br>
     * - false if it is New or Managed, or if it is not a persistent entity
     * @throws IllegalArgumentException if entity instance is null
     */
    public boolean isDetached(Object entity) {
        checkNotNullArgument(entity, "entity is null");
        if (entity instanceof Entity && getUncheckedEntityEntry(entity).isDetached()) {
            return true;
        } else {
            if (log.isTraceEnabled()) {
                log.trace("EntityStates.isDetached is called for unsupported type '{}'. Stacktrace:\n{}",
                        entity.getClass().getSimpleName(), StackTrace.asString());
            }
        }
        return false;
    }

    /**
     * Checks if the property is loaded from DB.
     * <p>Non-persistent attributes are considered loaded if they do not have related properties, or all related
     * properties are loaded.
     *
     * @param entity   entity
     * @param property name of the property. Only immediate attributes of the entity are supported.
     * @return true if loaded
     */
    public boolean isLoaded(Object entity, String property) {
        return checker.isLoaded(entity, property);
    }

    /**
     * Check that entity has all specified properties loaded from DB.
     * Throw exception if property is not loaded.
     *
     * @param entity     entity
     * @param properties property names
     * @throws IllegalArgumentException if at least one of properties is not loaded
     */
    public void checkLoaded(Object entity, String... properties) {
        checkNotNullArgument(entity);

        for (String property : properties) {
            if (!isLoaded(entity, property)) {
                String errorMessage = String.format("%s.%s is not loaded", entity.getClass().getSimpleName(), property);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    protected void checkLoadedWithFetchPlan(Object entity, FetchPlan fetchPlan, Set visited) {
        if (visited.contains(entity)) {
            return;
        }

        visited.add(entity);

        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            MetaClass metaClass = metadata.getClass(entity);
            MetaProperty metaProperty = metaClass.getProperty(property.getName());

            if (!isLoaded(entity, property.getName())) {
                String errorMessage = String.format("%s.%s is not loaded",
                        entity.getClass().getSimpleName(), property.getName());
                throw new IllegalArgumentException(errorMessage);
            }

            if (metaProperty.getRange().isClass()) {
                FetchPlan propertyFetchPlan = property.getFetchPlan();

                if (propertyFetchPlan != null && metadataTools.isJpa(metaProperty)) {
                    Object value = EntityValues.getValue(entity, metaProperty.getName());

                    if (value != null) {
                        if (!metaProperty.getRange().getCardinality().isMany()) {
                            checkLoadedWithFetchPlan(value, propertyFetchPlan, visited);
                        } else {
                            @SuppressWarnings("unchecked")
                            Collection collection = (Collection) value;

                            for (Object item : collection) {
                                checkLoadedWithFetchPlan(item, propertyFetchPlan, visited);
                            }
                        }
                    }
                }
            }
        }

        // after check we remove item from visited because different subtrees may have different fetch plan for the same instance
        visited.remove(entity);
    }

    /**
     * Check that all properties of the fetch plan are loaded from DB for the passed entity.
     * Throws exception if some property is not loaded.
     *
     * @param entity    entity
     * @param fetchPlan fetch plan
     * @throws IllegalArgumentException if at least one of properties is not loaded
     */
    public void checkLoadedWithFetchPlan(Object entity, FetchPlan fetchPlan) {
        checkNotNullArgument(entity);
        checkNotNullArgument(fetchPlan);

        checkLoadedWithFetchPlan(entity, fetchPlan, Sets.newIdentityHashSet());
    }

    /**
     * Check that all properties of the fetch plan are loaded from DB for the passed entity.
     * Throws exception if some property is not loaded.
     *
     * @param entity        entity
     * @param fetchPlanName fetch plan name
     * @throws IllegalArgumentException if at least one of properties is not loaded
     */
    public void checkLoadedWithFetchPlan(Object entity, String fetchPlanName) {
        checkNotNullArgument(fetchPlanName);

        checkLoadedWithFetchPlan(entity, fetchPlanRepository.getFetchPlan(metadata.getClass(entity), fetchPlanName));
    }

    protected boolean isLoadedWithFetchPlan(Object entity, FetchPlan fetchPlan, Set visited) {
        if (visited.contains(entity)) {
            return true;
        }

        visited.add(entity);

        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            MetaClass metaClass = metadata.getClass(entity);
            MetaProperty metaProperty = metaClass.getProperty(property.getName());

            if (!isLoaded(entity, property.getName())) {
                return false;
            }

            if (metaProperty.getRange().isClass()) {
                FetchPlan propertyFetchPlan = property.getFetchPlan();

                if (propertyFetchPlan != null && metadataTools.isJpa(metaProperty)) {
                    Object value = EntityValues.getValue(entity, metaProperty.getName());

                    if (value != null) {
                        if (!metaProperty.getRange().getCardinality().isMany()) {
                            if (!isLoadedWithFetchPlan(value, propertyFetchPlan, visited)) {
                                return false;
                            }
                        } else {
                            @SuppressWarnings("unchecked")
                            Collection collection = (Collection) value;

                            for (Object item : collection) {
                                if (!isLoadedWithFetchPlan(item, propertyFetchPlan, visited)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        // after check we remove item from visited because different subtrees may have different fetch plan for the same instance
        visited.remove(entity);

        return true;
    }

    /**
     * Check that all properties of the fetch plan are loaded from DB for the passed entity.
     *
     * @param entity    entity
     * @param fetchPlan fetch plan
     * @return false if at least one of properties is not loaded
     */
    public boolean isLoadedWithFetchPlan(Object entity, FetchPlan fetchPlan) {
        checkNotNullArgument(entity);
        checkNotNullArgument(fetchPlan);
        EntityPreconditions.checkEntityType(entity);

        return isLoadedWithFetchPlan(entity, fetchPlan, Sets.newIdentityHashSet());
    }

    /**
     * Check that all properties of the fetch plan are loaded from DB for the passed entity.
     *
     * @param entity        entity
     * @param fetchPlanName fetch plan name
     * @return false if at least one of properties is not loaded
     */
    public boolean isLoadedWithFetchPlan(Object entity, String fetchPlanName) {
        checkNotNullArgument(fetchPlanName);
        EntityPreconditions.checkEntityType(entity);

        return isLoadedWithFetchPlan(entity, fetchPlanRepository.getFetchPlan(metadata.getClass(entity), fetchPlanName));
    }

    /**
     * Returns a fetch plan that corresponds to the loaded attributes of the given entity instance.
     *
     * @param entity entity instance
     * @return fetch plan
     */
    public FetchPlan getCurrentFetchPlan(Object entity) {
        checkNotNullArgument(entity);
        EntityPreconditions.checkEntityType(entity);

        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(entity.getClass());
        recursivelyConstructCurrentFetchPlan((Entity) entity, fetchPlanBuilder, new HashSet<>());
        return fetchPlanBuilder.build();
    }

    protected void recursivelyConstructCurrentFetchPlan(Entity entity, FetchPlanBuilder builder, HashSet<Object> visited) {
        if (visited.contains(entity))
            return;
        visited.add(entity);

        // Using MetaClass of the fetchPlan helps in the case when the entity is an item of a collection, and the collection
        // can contain instances of different subclasses. So we don't want to add specific properties of subclasses
        // to the resulting fetch plan.
        MetaClass metaClass = metadata.getClass(builder.getEntityClass());

        for (MetaProperty property : metaClass.getProperties()) {
            if (!isLoaded(entity, property.getName()))
                continue;
            if (property.getRange().isClass()) {
                Object value = EntityValues.getValue(entity, property.getName());
                if (value != null) {
                    FetchPlanBuilder propertyBuilder = fetchPlans.builder(property.getRange().asClass().getJavaClass());
                    // The input object graph can be large, so we use FetchMode.UNDEFINED to avoid huge SQLs with
                    // unpredictably high number of joins
                    builder.add(property.getName(), propertyBuilder, FetchMode.UNDEFINED);
                    if (value instanceof Collection) {
                        for (Object item : ((Collection<?>) value)) {
                            recursivelyConstructCurrentFetchPlan((Entity) item, propertyBuilder, visited);
                        }
                    } else {
                        recursivelyConstructCurrentFetchPlan((Entity) value, propertyBuilder, visited);
                    }
                }
            } else {
                builder.add(property.getName());
            }
        }
    }

    /**
     * Determines whether the entity instance was <em>deleted</em>.
     *
     * @param entity entity instance
     * @return - true if the instance was deleted
     * - false otherwise
     * @throws IllegalArgumentException if entity instance is null
     */
    public boolean isDeleted(Object entity) {
        checkNotNullArgument(entity, "entity is null");
        if (entity instanceof Entity) {
            if (EntityValues.isSoftDeleted(entity))
                return true;

            if (getUncheckedEntityEntry(entity).isRemoved()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Makes a newly constructed object detached. The detached object can be passed to {@code DataManager.commit()} or
     * to {@code EntityManager.merge()} to save its state to the database.
     * <p>If an object with such ID does not exist in the database, a new object will be inserted.
     * <p>If the entity is {@code Versioned}, the version attribute should be equal to the latest version existing in
     * the database, or null for a new object.
     *
     * @param entity entity in the New state
     * @throws IllegalStateException if the entity is Managed
     * @see #isDetached(Object)
     * @see #makePatch(Object)
     */
    public void makeDetached(Object entity) {
        checkNotNullArgument(entity, "entity is null");
        EntityPreconditions.checkEntityType(entity);

        if (getUncheckedEntityEntry(entity).isManaged())
            throw new IllegalStateException("entity is managed");

        getUncheckedEntityEntry(entity).setNew(false);
        getUncheckedEntityEntry(entity).setDetached(true);
    }

    /**
     * Makes a newly constructed object a patch object. The patch object is {@code !isNew() && !isDetached() && !isManaged()}.
     * The patch object can be passed to {@code DataManager.commit()} or
     * to {@code EntityManager.merge()} to save its state to the database. Only <b>non-null values</b> of attributes are
     * updated.
     * <p>If an object with such ID does not exist in the database, a new object will be inserted.
     * <p>If the entity is {@code Versioned}, the version attribute should be null or equal to the latest version existing in
     * the database.
     *
     * @param entity entity in the New or Detached state
     * @throws IllegalStateException if the entity is Managed
     * @see #isDetached(Object)
     * @see #makeDetached(Object)
     */
    public void makePatch(Object entity) {
        checkNotNullArgument(entity, "entity is null");

        EntityPreconditions.checkEntityType(entity);

        if (getUncheckedEntityEntry(entity).isManaged())
            throw new IllegalStateException("entity is managed");

        getUncheckedEntityEntry(entity).setNew(false);
        getUncheckedEntityEntry(entity).setDetached(false);
    }
}
