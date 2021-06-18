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

package io.jmix.search.index;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum IndexSchemaManagementStrategy {

    /**
     * Does nothing.
     */
    NONE("none"),
    /**
     * Creates missing but skips existent indexes.
     */
    CREATE_ONLY("create-only"),
    /**
     * Creates missing and recreates irrelevant indexes.
     */
    CREATE_OR_RECREATE("create-or-recreate");

    private final String key;

    private static final Map<String, IndexSchemaManagementStrategy> ENUM_MAP;

    static {
        Map<String, IndexSchemaManagementStrategy> map = new HashMap<>();
        for (IndexSchemaManagementStrategy strategy : IndexSchemaManagementStrategy.values()) {
            map.put(strategy.key, strategy);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    IndexSchemaManagementStrategy(String key) {
        this.key = key;
    }

    public static IndexSchemaManagementStrategy getByKey(String key) {
        IndexSchemaManagementStrategy strategy = getByKeyOrNull(key);
        if (strategy == null) {
            throw new RuntimeException("Index Synchronization Strategy '" + key + "' not found");
        }
        return strategy;
    }

    public static IndexSchemaManagementStrategy getByKeyOrDefault(String key, IndexSchemaManagementStrategy defaultValue) {
        IndexSchemaManagementStrategy strategy = getByKeyOrNull(key);
        if (strategy == null) {
            return defaultValue;
        } else {
            return strategy;
        }
    }

    @Nullable
    public static IndexSchemaManagementStrategy getByKeyOrNull(String key) {
        return ENUM_MAP.get(key.toLowerCase());
    }
}
