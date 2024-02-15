/*
 * Copyright 2021 Haulmont.
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

import io.jmix.core.entity.KeyValueEntity;

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Central interface to provide CRUD functionality for entities.
 * <p>
 * Delegates to {@link DataStore} implementations, handles references between entities from different stores.
 * <p>
 * Does not apply registered access constraints. If you want to perform CRUD operations with authorization,
 * use {@link DataManager} instead.
 */
public interface UnconstrainedDataManager {

    /**
     * Loads a single entity instance.
     * <p>The depth of object graphs, starting from loaded instances, defined by {@link FetchPlan}
     * object passed in {@link LoadContext}.</p>
     *
     * @param context {@link LoadContext} object, defining what and how to load
     * @return the loaded object, or null if not found
     */
    @Nullable
    <E> E load(LoadContext<E> context);

    /**
     * Loads collection of entity instances.
     * <p>The depth of object graphs, starting from loaded instances, defined by {@link FetchPlan}
     * object passed in {@link LoadContext}.</p>
     *
     * @param context {@link LoadContext} object, defining what and how to load
     * @return a list of entity instances, or empty list if nothing found
     */
    <E> List<E> loadList(LoadContext<E> context);

    /**
     * Returns the number of entity instances for the given query passed in the {@link LoadContext}.
     *
     * @param context defines the query
     * @return number of instances in the data store
     */
    long getCount(LoadContext<?> context);

    /**
     * Saves a collection of entity instances to their data stores.
     *
     * @param context {@link SaveContext} object, containing entities and other information
     * @return set of saved instances
     */
    EntitySet save(SaveContext context);

    /**
     * Saves entities to their data stores.
     *
     * @param entities entities to save
     * @return set of saved instances
     */
    EntitySet save(Object... entities);

    /**
     * Saves a collection of entities to their data stores.
     *
     * @param entities entities to save
     * @return set of saved instances
     */
    EntitySet saveAll(Collection<?> entities);

    /**
     * Saves the entity to its data store.
     *
     * @param entity entity instance
     * @return saved instance
     */
    <E> E save(E entity);

    /**
     * Removes the entities from their data stores.
     *
     * @param entity entity instance
     */
    void remove(Object... entity);

    /**
     * Removes the entity instance from the data store by its id.
     *
     * @param entityId entity id
     */
    <E> void remove(Id<E> entityId);

    /**
     * Loads list of key-value pairs.
     *
     * @param context defines a query for scalar values and a list of keys for returned KeyValueEntity
     * @return list of KeyValueEntity instances
     */
    List<KeyValueEntity> loadValues(ValueLoadContext context);

    /**
     * Returns the number of records for the given query passed in the {@link ValueLoadContext}.
     *
     * @param context defines the query
     * @return number of records
     */
    long getCount(ValueLoadContext context);

    /**
     * Entry point to the fluent API for loading entities.
     * <p>
     * Usage examples:
     * <pre>
     * Customer customer = dataManager.load(Customer.class).id(someId).one();
     *
     * List&lt;Customer&gt; customers = dataManager.load(Customer.class)
     *      .query("select c from Customer c where c.name = :name")
     *      .parameter("name", "Smith")
     *      .maxResults(100)
     *      .list();
     * </pre>
     *
     * @param entityClass class of the loaded entity
     */
    <E> FluentLoader<E> load(Class<E> entityClass);

    /**
     * Entry point to the fluent API for loading entities.
     * <p>
     * Usage example:
     * <pre>
     * Customer customer = dataManager.load(customerId).one();
     * </pre>
     *
     * @param entityId {@link Id} of the loaded entity
     */
    <E> FluentLoader.ById<E> load(Id<E> entityId);

    /**
     * Entry point to the fluent API for loading scalar values.
     * <p>
     * Usage examples:
     * <pre>
     * List&lt;KeyValueEntity&gt; customerDataList = dataManager.loadValues(
     *          "select c.name, c.status from Customer c where c.name = :n")
     *      .properties("custName", "custStatus")
     *      .parameter("name", "Smith")
     *      .list();
     *
     * KeyValueEntity customerData = dataManager.loadValues(
     *          "select c.name, count(c) from Customer c group by c.name")
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
     *          "select count(c) from Customer c", Long.class).one();
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
    <T> T create(Class<T> entityClass);

    /**
     * Returns an entity instance which can be used as a reference to an object which exists in the data store.
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
    <T> T getReference(Class<T> entityClass, Object id);

    /**
     * Returns an entity instance which can be used as a reference to an object which exists in the data store.
     *
     * @param entityId id of an existing object
     * @see #getReference(Class, Object)
     */
    <T> T getReference(Id<T> entityId);
}
