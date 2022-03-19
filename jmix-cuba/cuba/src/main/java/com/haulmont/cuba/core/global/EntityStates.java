/*
 * Copyright (c) 2008-2017 Haulmont.
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


import io.jmix.core.FetchPlan;
import io.jmix.core.Entity;
import io.jmix.core.MetadataTools;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedList;

/**
 * Provides information about entities states.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.EntityStates}.
 */
@Deprecated
public class EntityStates extends io.jmix.core.EntityStates {

    public static final String NAME = "cuba_EntityStates";

    /**
     * DEPRECATED. Use {@link MetadataTools#isSoftDeletable(Class)} instead.
     */
    @Deprecated
    public boolean isSoftDeleted(Class entityClass) {
        return metadataTools.isSoftDeletable(entityClass);
    }

    /**
     * Check that all properties of the view are loaded from DB for the passed entity.
     * Throws exception if some property is not loaded.
     *
     * @param entity entity
     * @param view   view
     * @throws IllegalArgumentException if at least one of properties is not loaded
     */
    public void checkLoadedWithView(Entity entity, View view) {
        checkLoadedWithFetchPlan(entity, view);
    }

    /**
     * Check that all properties of the view are loaded from DB for the passed entity.
     * Throws exception if some property is not loaded.
     *
     * @param entity   entity
     * @param viewName view name
     * @throws IllegalArgumentException if at least one of properties is not loaded
     */
    public void checkLoadedWithView(Entity entity, String viewName) {
        checkLoadedWithFetchPlan(entity, viewName);
    }

    /**
     * Check that all properties of the view are loaded from DB for the passed entity.
     *
     * @param entity entity
     * @param view   view name
     * @return false if at least one of properties is not loaded
     */
    public boolean isLoadedWithView(Entity entity, View view) {
        return isLoadedWithFetchPlan(entity, view);
    }

    /**
     * Check that all properties of the view are loaded from DB for the passed entity.
     *
     * @param entity   entity
     * @param viewName view name
     * @return false if at least one of properties is not loaded
     */
    public boolean isLoadedWithView(Entity entity, String viewName) {
        return isLoadedWithFetchPlan(entity,viewName);
    }

    /**
     * Returns a view that corresponds to the loaded attributes of the given entity instance.
     * @param entity entity instance
     * @return view
     */
    public View getCurrentView(Entity entity) {
        FetchPlan fetchPlan = getCurrentFetchPlan(entity);
        return new View((Class<Entity>) fetchPlan.getEntityClass(),
                fetchPlan.getName(),
                new LinkedList<>(fetchPlan.getProperties()),
                fetchPlan.loadPartialEntities());
    }
}