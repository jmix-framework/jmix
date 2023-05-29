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

import io.jmix.core.entity.KeyValueEntity;

import org.springframework.lang.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Interface defining methods for CRUD operations on entities.
 * <p>
 * Implementations of this interface must be prototype beans. They are used by {@link DataManager}, do not access
 * data stores directly from your application code.
 */
public interface DataStore {

    /**
     * This data store instance name with which it is registered in {@link Stores}.
     */
    String getName();

    /**
     * Sets this data store instance name with which it is registered in {@link Stores}.
     */
    void setName(String name);

    /**
     * Loads a single entity instance.
     *
     * @return the loaded object, or null if not found
     */
    @Nullable
    Object load(LoadContext<?> context);

    /**
     * Loads collection of entity instances.
     *
     * @return a list of instances, or empty list if nothing found
     */
    List<Object> loadList(LoadContext<?> context);

    /**
     * Returns the number of entity instances for the given query passed in the {@link LoadContext}.
     *
     * @return number of instances in the storage
     */
    long getCount(LoadContext<?> context);

    /**
     * Saves a collection of entity instances.
     *
     * @return set of saved instances
     */
    Set<?> save(SaveContext context);

    /**
     * Loads list of key-value pairs.
     *
     * @param context defines a query for scalar values and a list of keys for returned KeyValueEntity
     * @return list of KeyValueEntity instances
     */
    List<KeyValueEntity> loadValues(ValueLoadContext context);

    /**
     * Returns the number of key-value pairs for the given query passed in the {@link ValueLoadContext}.
     *
     * @param context defines the query
     * @return number of key-value pairs in the data store
     */
    long getCount(ValueLoadContext context);
}
