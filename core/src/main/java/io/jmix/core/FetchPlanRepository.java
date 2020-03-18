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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.Entity;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Represents a repository of {@link FetchPlan} objects, accessible by names.
 * <br>Repository contains all fetch plans defined in XML and deployed at runtime.
 */
public interface FetchPlanRepository {

    String NAME = "jmix_FetchPlanRepository";

    /**
     * Get fetch plan for an entity.
     *
     * @param entityClass   entity class
     * @param name          fetch plan name
     * @return              fetch plan instance. Throws {@link FetchPlanNotFoundException} if not found.
     */
    FetchPlan getFetchPlan(Class<? extends Entity> entityClass, String name);

    /**
     * Get View for an entity.
     *
     * @param metaClass     entity class
     * @param name          view name
     * @return              view instance. Throws {@link FetchPlanNotFoundException} if not found.
     */
    FetchPlan getFetchPlan(MetaClass metaClass, String name);

    /**
     * Searches for a View for an entity.
     *
     * @param metaClass     entity class
     * @param name          view name
     * @return              view instance or null if no view found
     */
    @Nullable
    FetchPlan findFetchPlan(MetaClass metaClass, String name);

    /**
     * Returns names of views defined for the metaClass
     * @param metaClass entity class
     * @return names of views
     */
    Collection<String> getFetchPlanNames(MetaClass metaClass);

    /**
     * Returns names of views defined for the entityClass
     * @param entityClass entity class
     * @return names of views
     */
    Collection<String> getFetchPlanNames(Class<? extends Entity> entityClass);
}
