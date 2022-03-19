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

package io.jmix.search.index.impl;

import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Holds markers of search indexes availability.
 * Only entities with available indexes can be used in index-modification operations
 * (to prevent storing data into incorrect indexes or automatic creation of them by ES).
 * <p>Doesn't affect searching.
 * <p>Every action that makes or detects index is valid (creation or successful synchronization\validation) marks it as available.
 * <p>Every action that makes or detects index is invalid (drop or unsuccessful synchronization\validation) marks it as unavailable.
 */
@Component("search_IndexStateRegistry")
public class IndexStateRegistry {

    protected final Map<String, Boolean> registry;
    protected final IndexConfigurationManager indexConfigurationManager;

    @Autowired
    public IndexStateRegistry(IndexConfigurationManager indexConfigurationManager) {
        Map<String, Boolean> tmpRegistry = new ConcurrentHashMap<>();
        indexConfigurationManager.getAllIndexedEntities().forEach(entity -> tmpRegistry.put(entity, false));
        this.registry = tmpRegistry;
        this.indexConfigurationManager = indexConfigurationManager;
    }

    public Map<String, Boolean> getIndexAvailabilityStates() {
        return Collections.unmodifiableMap(registry);
    }

    public boolean isIndexAvailable(String entityName) {
        return registry.getOrDefault(entityName, false);
    }

    public void markIndexAsAvailable(String entityName) {
        setRegistryValue(entityName, true);
    }

    public void markIndexAsUnavailable(String entityName) {
        setRegistryValue(entityName, false);
    }

    public List<String> getAllUnavailableIndexedEntities() {
        return registry.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    protected void setRegistryValue(String entityName, boolean value) {
        if (indexConfigurationManager.isDirectlyIndexed(entityName)) {
            registry.put(entityName, value);
        } else {
            throw new IllegalArgumentException(String.format("Entity '%s' is not indexed", entityName));
        }
    }
}
