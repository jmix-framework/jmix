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

package com.haulmont.cuba.core.global;

import com.haulmont.cuba.core.entity.contracts.Id;
import io.jmix.core.Entity;
import io.jmix.core.EntityAccessException;
import io.jmix.core.FetchPlan;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Central interface to provide CRUD functionality. Can be used on both middle and client tiers.
 * <p>
 * In case of {@code RdbmsStore}, works with non-managed (new or detached) entities, always starts and commits new
 * transactions.
 * <p>
 * When used on the client tier - always applies security restrictions. When used on the middleware - does not apply
 * security restrictions by default. If you want to apply security, get {@link #secure()} instance or set the
 * {@code cuba.dataManagerChecksSecurityOnMiddleware} application property to use it by default.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.DataManager}.
 */
@Deprecated
@SuppressWarnings("rawtypes")
public interface DataManager {

    String NAME = "cuba_DataManager";

    /**
     * Loads a single entity instance.
     * <p>The depth of object graphs, starting from loaded instances, defined by {@link FetchPlan}
     * object passed in {@link LoadContext}.</p>
     *
     * @param context {@link LoadContext} object, defining what and how to load
     * @return the loaded detached object, or null if not found
     */
    @Nullable
    <E extends Entity> E load(LoadContext<E> context);

    /**
     * Loads collection of entity instances.
     * <p>The depth of object graphs, starting from loaded instances, defined by {@link FetchPlan}
     * object passed in {@link LoadContext}.</p>
     *
     * @param context {@link LoadContext} object, defining what and how to load
     * @return a list of detached instances, or empty list if nothing found
     */
    <E extends Entity> List<E> loadList(LoadContext<E> context);

    /**
     * Returns the number of entity instances for the given query passed in the {@link LoadContext}.
     *
     * @param context defines the query
     * @return number of instances in the data store
     */
    long getCount(LoadContext<? extends Entity> context);

    /**
     * Reloads the entity instance from data store with the fetch plan specified.
     *
     * @param entity        reloading instance
     * @param fetchPlanName fetch plan name
     * @return reloaded instance
     * @throws EntityAccessException if the entity cannot be reloaded because it was deleted or access restrictions has been changed
     */
    <E extends Entity> E reload(E entity, String fetchPlanName);

    /**
     * Reloads the entity instance from data store with the fetch plan specified.
     *
     * @param entity    reloading instance
     * @param fetchPlan fetch plan object
     * @return reloaded instance
     * @throws EntityAccessException if the entity cannot be reloaded because it was deleted or access restrictions has been changed
     */
    <E extends Entity> E reload(E entity, FetchPlan fetchPlan);

    /**
     * Reloads the entity instance from data store with the fetch plan specified. Loading instance class may differ from original
     * instance if we want to load an ancestor or a descendant.
     *
     * @param entity    reloading instance
     * @param fetchPlan fetch plan object
     * @param metaClass desired MetaClass, if null - original entity's metaclass is used
     * @return reloaded instance
     * @throws EntityAccessException if the entity cannot be reloaded because it was deleted or access restrictions has been changed
     */
    <E extends Entity> E reload(E entity, FetchPlan fetchPlan, @Nullable MetaClass metaClass);

    /**
     * Reloads the entity instance from data store with the fetch plan specified. Loading instance class may differ from original
     * instance if we want to load an ancestor or a descendant.
     *
     * @param entity                reloading instance
     * @param fetchPlan             fetch plan object
     * @param metaClass             desired MetaClass, if null - original entity's metaclass is used
     * @param loadDynamicAttributes whether to load dynamic attributes for the entity
     * @return reloaded instance
     * @throws EntityAccessException if the entity cannot be reloaded because it was deleted or access restrictions has been changed
     */
    <E extends Entity> E reload(E entity, FetchPlan fetchPlan, @Nullable MetaClass metaClass, boolean loadDynamicAttributes);

    /**
     * Commits a collection of new or detached entity instances to the data store.
     *
     * @param context {@link CommitContext} object, containing committing entities and other information
     * @return set of committed instances
     */
    EntitySet commit(CommitContext context);

    /**
     * Commits new or detached entity instances to the data store.
     *
     * @param entities entities to commit
     * @return set of committed instances
     */
    EntitySet commit(Entity... entities);

    /**
     * Commits the entity to the data store.
     *
     * @param entity    entity instance
     * @param fetchPlan fetch plan object, affects the returned committed instance
     * @return committed instance
     */
    <E extends Entity> E commit(E entity, @Nullable FetchPlan fetchPlan);

    /**
     * Commits the entity to the data store.
     *
     * @param entity        entity instance
     * @param fetchPlanName fetch plan name, affects the returned committed instance
     * @return committed instance
     */
    <E extends Entity> E commit(E entity, @Nullable String fetchPlanName);

    /**
     * Commits the entity to the data store.
     *
     * @param entity entity instance
     * @return committed instance
     */
    <E extends Entity> E commit(E entity);

    /**
     * Removes the entity instance from the data store.
     *
     * @param entity entity instance
     */
    void remove(Entity entity);

    /**
     * Removes the entity instance from the data store by its id.
     *
     * @param entityId entity id
     */
    <T extends Entity, K> void remove(Id<T, K> entityId);

    /**
     * Loads list of key-value pairs.
     *
     * @param context defines a query for scalar values and a list of keys for returned KeyValueEntity
     * @return list of KeyValueEntity instances
     */
    List<KeyValueEntity> loadValues(ValueLoadContext context);

    /**
     * By default, DataManager does not apply security restrictions on entity operations and attributes, only row-level
     * constraints take effect.
     * <p>
     * This method returns the {@code DataManager} implementation that applies security restrictions on entity operations.
     * Attribute permissions will be enforced only if you additionally set the {@code cuba.entityAttributePermissionChecking}
     * application property to true.
     * <p>
     * Usage example:
     * <pre>
     *     AppBeans.get(DataManager.class).secure().load(context);
     * </pre>
     */
    DataManager secure();

    io.jmix.core.UnconstrainedDataManager getDelegate();

    /**
     * Entry point to the fluent API for loading entities.
     * <p>
     * Usage examples:
     * <pre>
     * Customer customer = dataManager.load(Customer.class).id(someId).one();
     *
     * List&lt;Customer&gt; customers = dataManager.load(Customer.class)
     *      .query("select c from sample$Customer c where c.name = :name")
     *      .parameter("name", "Smith")
     *      .fetchPlan("customer-fetch-plan")
     *      .list();
     * </pre>
     *
     * @param entityClass class of entity that needs to be loaded
     */
    <E extends Entity> FluentLoader<E> load(Class<E> entityClass);

    /**
     * Entry point to the fluent API for loading entities.
     * <p>
     * Usage example:
     * <pre>
     * Customer customer = dataManager.load(customerId).fetchPlan("with-grade").one();
     * </pre>
     *
     * @param entityId {@link Id} of entity that needs to be loaded
     */
    <E extends Entity, K> FluentLoader.ById<E> load(Id<E, K> entityId);

    /**
     * Entry point to the fluent API for loading scalar values.
     * <p>
     * Usage examples:
     * <pre>
     * List&lt;KeyValueEntity&gt; customerDataList = dataManager.loadValues(
     *          "select c.name, c.status from sample$Customer c where c.name = :n")
     *      .properties("custName", "custStatus")
     *      .parameter("name", "Smith")
     *      .list();
     *
     * KeyValueEntity customerData = dataManager.loadValues(
     *          "select c.name, count(c) from sample$Customer c group by c.name")
     *      .properties("custName", "custCount")
     *      .one();
     * </pre>
     *
     * @param queryString query string
     */
    FluentValuesLoader loadValues(String queryString);

    /**
     * Entry point to the fluent API for loading a single scalar value.
     * <p>
     * Terminal methods of this API ({@code list}, {@code one} and {@code optional}) return a single value
     * from the first column of the query result set. You should provide the expected type of this value in the second
     * parameter. Number types will be converted appropriately, so for example if the query returns Long and you
     * expected Integer, the returned value will be automatically converted from Long to Integer.
     * <p>
     * Usage examples:
     * <pre>
     * Long customerCount = dataManager.loadValue(
     *          "select count(c) from sample$Customer c", Long.class).one();
     * </pre>
     *
     * @param queryString query string
     * @param valueClass  type of the returning value
     */
    <T> FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass);

    /**
     * Creates a new entity instance in memory. This is a shortcut to {@code Metadata.create()}.
     *
     * @param entityClass entity class
     */
    <T extends Entity> T create(Class<T> entityClass);

    /**
     * Returns an entity instance which can be used as a reference to an object which exists in the database.
     * <p>
     * For example, if you are creating a User, you have to set a Group the user belongs to. If you know the group id,
     * you could load it from the database and set to the user. This method saves you from unneeded database round trip:
     * <pre>
     * user.setGroup(dataManager.getReference(Group.class, groupId));
     * dataManager.commit(user);
     * </pre>
     * A reference can also be used to delete an existing object by id:
     * <pre>
     * dataManager.remove(dataManager.getReference(Customer.class, customerId));
     * </pre>
     *
     * @param entityClass entity class
     * @param id          id of an existing object
     */
    <T extends Entity, K> T getReference(Class<T> entityClass, K id);

    /**
     * Returns an entity instance which can be used as a reference to an object which exists in the database.
     *
     * @param entityId id of an existing object
     * @see #getReference(Class, Object)
     */
    <T extends Entity, K> T getReference(Id<T, K> entityId);
}
