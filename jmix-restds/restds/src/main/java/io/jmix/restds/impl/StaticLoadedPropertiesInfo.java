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

package io.jmix.restds.impl;

import io.jmix.core.PersistentAttributesLoadChecker;
import io.jmix.core.entity.LoadedPropertiesInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link LoadedPropertiesInfo} that stores names of loaded properties.
 * <p>
 * Used by default for DTO entities loaded by REST DataStore.
 */
public class StaticLoadedPropertiesInfo implements LoadedPropertiesInfo {

    private Set<String> loadedProperties = new HashSet<>();

    @Override
    public boolean isLoaded(Object entity, String property, PersistentAttributesLoadChecker checker) {
        return loadedProperties.contains(property);
    }

    @Override
    public void registerProperty(String name, boolean loaded) {
        if (loaded)
            loadedProperties.add(name);
        else
            loadedProperties.remove(name);
    }

    @Override
    public LoadedPropertiesInfo copy() {
        StaticLoadedPropertiesInfo dstInfo = new StaticLoadedPropertiesInfo();
        dstInfo.loadedProperties.addAll(loadedProperties);
        return dstInfo;
    }
}
