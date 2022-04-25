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

package io.jmix.flowui.model;

import io.jmix.core.querycondition.Condition;
import io.jmix.flowui.RequiresChanges;
import io.jmix.flowui.SameAsUi;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;

/**
 * The root interface in the <i>data loaders</i> hierarchy. Data loaders are designed to load entity instances and
 * collections from the middle tier to data containers.
 *
 * @see InstanceContainer
 * @see CollectionContainer
 */
@RequiresChanges
@SameAsUi
public interface DataLoader {

    /**
     * Loads data to the connected container.
     */
    void load();

    /**
     * Returns connected container.
     */
    InstanceContainer getContainer();

    /**
     * Returns data context. If the data context is set, all loaded instance will be merged into it.
     */
    @Nullable
    DataContext getDataContext();

    /**
     * Sets the data context for the loader. If the data context is set, all loaded instance will be merged into it.
     */
    void setDataContext(@Nullable DataContext dataContext);

    /**
     * Returns the query which is used for loading entities.
     */
    String getQuery();

    /**
     * Sets a query which will be used for loading entities.
     */
    void setQuery(String query);

    /**
     * Returns the root condition which is used together with the query when loading entities.
     */
    @Nullable
    Condition getCondition();

    /**
     * Sets the root condition which will be used together with the query when loading entities.
     */
    void setCondition(Condition condition);

    /**
     * Returns the map of query parameters.
     */
    Map<String, Object> getParameters();

    /**
     * Sets the map of query parameters.
     */
    void setParameters(Map<String, Object> parameters);

    /**
     * Returns a query parameter by its name.
     */
    Object getParameter(String name);

    /**
     * Sets a query parameter.
     */
    void setParameter(String name, @Nullable Object value);

    /**
     * Removes a query parameter.
     */
    void removeParameter(String name);

    /**
     * Sets custom hint that should be used by the query for loading data.
     */
    void setHint(String hintName, Serializable value);

    /**
     * @return custom hints which are used by the query for loading data.
     */
    Map<String, Serializable> getHints();
}
