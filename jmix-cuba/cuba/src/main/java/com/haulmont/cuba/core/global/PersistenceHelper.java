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

import io.jmix.core.Entity;
import io.jmix.core.MetadataTools;

/**
 * Utility class providing information about entity states.
 * <p>
 * Delegates to {@link EntityStates} bean, so consider using it directly.
 *
 * @deprecated use {@link EntityStates} instead
 */
public class PersistenceHelper {

    //this instance is required for unit tests where application context can be not created
    private static EntityStates _entityStates = new EntityStates();

    /**
     * @see EntityStates#isNew(Object)
     */
    public static boolean isNew(Object entity) {
        EntityStates entityStates = AppBeans.get(EntityStates.class);
        return entityStates != null ? entityStates.isNew(entity) : _entityStates.isNew(entity);
    }

    /**
     * @see EntityStates#isManaged(Object)
     */
    public static boolean isManaged(Object entity) {
        EntityStates entityStates = AppBeans.get(EntityStates.class);
        return entityStates != null ? entityStates.isManaged(entity) : _entityStates.isManaged(entity);
    }

    /**
     * @see EntityStates#isDeleted(Object)
     */
    public static boolean isDetached(Object entity) {
        EntityStates entityStates = AppBeans.get(EntityStates.class);
        return entityStates != null ? entityStates.isDetached(entity) : _entityStates.isDetached(entity);
    }

    /**
     * DEPRECATED. Use {@link MetadataTools#isSoftDeletable(Class)} instead.
     */
    @Deprecated
    public static boolean isSoftDeleted(Class entityClass) {
        EntityStates entityStates = AppBeans.get(EntityStates.class);
        return entityStates != null ? entityStates.isSoftDeleted(entityClass) : _entityStates.isSoftDeleted(entityClass);
    }

    /**
     * @see EntityStates#isLoaded(Object, String)
     */
    public static boolean isLoaded(Object entity, String property) {
        EntityStates entityStates = AppBeans.get(EntityStates.class);
        return entityStates != null ? entityStates.isLoaded(entity, property) : _entityStates.isLoaded(entity, property);
    }

    /**
     * @see EntityStates#checkLoaded(Object, String...)
     */
    public static void checkLoaded(Object entity, String... properties) {
        EntityStates entityStates = AppBeans.get(EntityStates.class);
        if (entityStates != null) {
            entityStates.checkLoaded(entity, properties);
        } else {
            _entityStates.checkLoaded(entity, properties);
        }
    }

    /**
     * Check that all properties of the view are loaded from DB for the passed entity.
     * Throws exception if some property is not loaded.
     *
     * @param entity   entity
     * @param viewName view name
     * @throws IllegalArgumentException if at least one of properties is not loaded
     */
    public static void checkLoadedWithView(Entity entity, String viewName) {
        EntityStates entityStates = AppBeans.get(EntityStates.class);
        if (entityStates != null) {
            entityStates.checkLoadedWithFetchPlan(entity, viewName);
        } else {
            _entityStates.checkLoadedWithFetchPlan(entity, viewName);
        }
    }

    /**
     * Check that all properties of the view are loaded from DB for the passed entity.
     *
     * @param entity   entity
     * @param viewName view name
     * @return false if at least one of properties is not loaded
     */
    public static boolean isLoadedWithView(Entity entity, String viewName) {
        EntityStates entityStates = AppBeans.get(EntityStates.class);
        if (entityStates != null) {
            return entityStates.isLoadedWithView(entity, viewName);
        } else {
            return _entityStates.isLoadedWithView(entity, viewName);
        }
    }

    /**
     * @see EntityStates#isDeleted(Object)
     */
    public static boolean isDeleted(Object entity) {
        EntityStates entityStates = AppBeans.get(EntityStates.class);
        return entityStates != null ? entityStates.isDeleted(entity) : _entityStates.isDeleted(entity);
    }

    /**
     * @see EntityStates#makeDetached(Object)
     */
    public static void makeDetached(Entity entity) {
        EntityStates entityStates = AppBeans.get(EntityStates.class);
        if (entityStates != null) {
            entityStates.makeDetached(entity);
        } else {
            _entityStates.makeDetached(entity);
        }
    }

    /**
     * @see EntityStates#makePatch(Object)
     */
    public static void makePatch(Entity entity) {
        EntityStates entityStates = AppBeans.get(EntityStates.class);
        if (entityStates != null) {
            entityStates.makePatch(entity);
        } else {
            _entityStates.makePatch(entity);
        }
    }
}
