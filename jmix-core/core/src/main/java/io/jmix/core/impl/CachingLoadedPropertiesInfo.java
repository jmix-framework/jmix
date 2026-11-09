/*
 * Copyright 2024 Haulmont.
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

package io.jmix.core.impl;

import io.jmix.core.EntityStates;
import io.jmix.core.PersistentAttributesLoadChecker;
import io.jmix.core.EntityStates.PropertyLoadedState;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.LoadedPropertiesInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link LoadedPropertiesInfo} that caches information about loaded properties to avoid
 * expensive operations for the same instance and property.
 * <p>
 * Used by default for JPA entities.
 * <p>
 * The instance is thread-safe: the same detached entity instance can be shared between threads, for example
 * through the EclipseLink shared cache.
 */
public class CachingLoadedPropertiesInfo implements LoadedPropertiesInfo {

    private final Map<String, Boolean> cache = new ConcurrentHashMap<>();

    @Override
    public boolean isLoaded(Object entity, String property, PersistentAttributesLoadChecker checker) {
        if (EntitySystemAccess.getEntityEntry(entity).isManaged()) {
            return checker.isLoaded(entity, property);
        }
        Boolean loaded = cache.get(property);
        if (loaded != null) {
            return loaded;
        }
        // Do not use cache.computeIfAbsent() here. The checker may call the property getter to find out
        // if the property is loaded. For a detached entity, the getter can trigger lazy loading, and lazy
        // loading calls registerProperty() on this object, that is, it writes to the same map. A write to
        // the map from inside the computeIfAbsent() function is not allowed and can fail with IllegalStateException
        // ("Recursive update"). So the checker is called first, and its result is stored afterwards.
        // If another thread or lazy loading has stored a value in the meantime, that value is kept
        // and returned.
        loaded = checker.isLoaded(entity, property);
        Boolean registered = cache.putIfAbsent(property, loaded);
        return registered != null ? registered : loaded;
    }

    @Override
    public PropertyLoadedState isLoadedSafe(Object entity, String property, PersistentAttributesLoadChecker checker) {
        if (EntitySystemAccess.getEntityEntry(entity).isManaged()) {
            return checker.isLoadedSafe(entity, property);
        }
        Boolean loaded = cache.get(property);
        if (loaded != null) {
            return EntityStates.PropertyLoadedState.fromBoolean(loaded);
        } else {
            PropertyLoadedState state = checker.isLoadedSafe(entity, property);
            if (state != EntityStates.PropertyLoadedState.UNKNOWN) {
                cache.put(property, EntityStates.PropertyLoadedState.YES.equals(state));
            }
            return state;
        }
    }

    @Override
    public void registerProperty(String name, boolean loaded) {
        cache.put(name, loaded);
    }

    @Override
    public LoadedPropertiesInfo copy() {
        CachingLoadedPropertiesInfo dstInfo = new CachingLoadedPropertiesInfo();
        dstInfo.cache.putAll(cache);
        return dstInfo;
    }
}
