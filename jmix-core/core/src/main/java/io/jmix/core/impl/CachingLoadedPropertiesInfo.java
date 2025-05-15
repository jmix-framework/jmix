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

import io.jmix.core.PersistentAttributesLoadChecker;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.LoadedPropertiesInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link LoadedPropertiesInfo} that caches information about loaded properties to avoid
 * expensive operations for the same instance and property.
 * <p>
 * Used by default for JPA entities.
 */
public class CachingLoadedPropertiesInfo implements LoadedPropertiesInfo {

    private Map<String, Boolean> cache = new HashMap<>();

    @Override
    public boolean isLoaded(Object entity, String property, PersistentAttributesLoadChecker checker) {
        if (EntitySystemAccess.getEntityEntry(entity).isManaged()) {
            return checker.isLoaded(entity, property);
        }
        return cache.computeIfAbsent(property, name ->
                checker.isLoaded(entity, name));
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
