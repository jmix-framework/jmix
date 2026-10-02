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

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds markers of search indexes availability.
 * Only available indexes can be used in index-modification operations
 * (to prevent storing data into incorrect indexes or automatic creation of them by ES).
 * <p>Doesn't affect searching.
 * <p>Every action that makes or detects index is valid (creation or successful synchronization\validation) marks it as available.
 * <p>Every action that makes or detects index is invalid (drop or unsuccessful synchronization\validation) marks it as unavailable.
 * <p>Availability is tracked per index, not per entity: a tenant-aware entity is mapped to a separate index per
 * tenant, and these indexes become available independently of each other.
 */
@Component("search_IndexStateRegistry")
public class IndexStateRegistry {

    /**
     * An index is considered unavailable until it is explicitly marked as available, so the registry starts empty.
     */
    protected final Map<String, Boolean> registry = new ConcurrentHashMap<>();

    public boolean isIndexAvailable(String indexName) {
        return registry.getOrDefault(indexName, false);
    }

    public void markIndexAsAvailable(String indexName) {
        setRegistryValue(indexName, true);
    }

    public void markIndexAsUnavailable(String indexName) {
        setRegistryValue(indexName, false);
    }

    protected void setRegistryValue(String indexName, boolean value) {
        registry.put(indexName, value);
    }

    /**
     * Clears the registry, so every index becomes unavailable until it is marked as available again.
     * <p>
     * Should be called after index configurations are refreshed: the previous availability markers may refer to
     * indexes that are no longer configured.
     */
    public void clean() {
        registry.clear();
    }
}
