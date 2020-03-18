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

import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.Entity;
import io.jmix.core.entity.KeyValueEntity;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Central interface to provide CRUD functionality. Can be used on both middle and client tiers.
 * <p>
 * In case of {@code RdbmsStore}, works with non-managed (new or detached) entities, always starts and commits new
 * transactions.
 */
public interface DataManager {

    String NAME = "jmix_DataManager";

    /**
     * Loads a single entity instance.
     * <p>The depth of object graphs, starting from loaded instances, defined by {@link FetchPlan}
     * object passed in {@link LoadContext}.</p>
     * @param context   {@link LoadContext} object, defining what and how to load
     * @return          the loaded detached object, or null if not found
     */
    @Nullable
    <E extends Entity> E load(LoadContext<E> context);

    /**
     * Loads collection of entity instances.
     * <p>The depth of object graphs, starting from loaded instances, defined by {@link FetchPlan}
     * object passed in {@link LoadContext}.</p>
     * @param context   {@link LoadContext} object, defining what and how to load
     * @return          a list of detached instances, or empty list if nothing found
     */
    <E extends Entity> List<E> loadList(LoadContext<E> context);

    /**
     * Returns the number of entity instances for the given query passed in the {@link LoadContext}.
     * @param context   defines the query
     * @return          number of instances in the data store
     */
    long getCount(LoadContext<? extends Entity> context);

    /**
     * Commits a collection of new or detached entity instances to the data store.
     * @param context   {@link SaveContext} object, containing committing entities and other information
     * @return          set of committed instances
     */
    EntitySet save(SaveContext context);

    /**
     * Commits new or detached entity instances to the data store.
     * @param entities  entities to commit
     * @return          set of committed instances
     */
    EntitySet save(Entity... entities);

    /**
     * Commits the entity to the data store.
     * @param entity    entity instance
     * @return          committed instance
     */
    <E extends Entity> E save(E entity);

    /**
     * Removes the entities from the data store.
     * @param entity    entity instance
     */
    void remove(Entity... entity);

    /**
     * Removes the entity instance from the data store by its id.
     * @param entityId    entity id
     */
    default <T extends Entity<K>, K> void remove(Id<T, K> entityId) {
        remove(getReference(entityId));
    }

    /**
     * Loads list of key-value pairs.
     * @param context   defines a query for scalar values and a list of keys for returned KeyValueEntity
     * @return list of KeyValueEntity instances
     */
    List<KeyValueEntity> loadValues(ValueLoadContext context);

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
     * @param entityClass   class of entity that needs to be loaded
     */
    default <E extends Entity<K>, K> FluentLoader<E, K> load(Class<E> entityClass) {
        return new FluentLoader<>(entityClass, this);
    }

    /**
     * Entry point to the fluent API for loading entities.
     * <p>
     * Usage example:
     * <pre>
     * Customer customer = dataManager.load(customerId).fetchPlan("with-grade").one();
     * </pre>
     * @param entityId   {@link Id} of entity that needs to be loaded
     */
    default <E extends Entity<K>, K> FluentLoader.ById<E, K> load(Id<E, K> entityId) {
        return new FluentLoader<>(entityId.getEntityClass(), this).id(entityId.getValue());
    }

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
     * @param queryString   query string
     */
    default FluentValuesLoader loadValues(String queryString) {
        return new FluentValuesLoader(queryString, this);
    }

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
     * @param queryString   query string
     * @param valueClass    type of the returning value
     */
    default <T> FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
        return new FluentValueLoader<>(queryString, valueClass, this);
    }

    /**
     * Creates a new entity instance in memory. This is a shortcut to {@code Metadata.create()}.
     * @param entityClass   entity class
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
     * @param entityClass   entity class
     * @param id            id of an existing object
     */
    <T extends Entity<K>, K> T getReference(Class<T> entityClass, K id);

    /**
     * Returns an entity instance which can be used as a reference to an object which exists in the database.
     *
     * @param entityId id of an existing object
     *
     * @see #getReference(Class, Object)
     */
    default <T extends Entity<K>, K> T getReference(Id<T, K> entityId) {
        Preconditions.checkNotNullArgument(entityId, "entityId is null");
        return getReference(entityId.getEntityClass(), entityId.getValue());
    }
}
