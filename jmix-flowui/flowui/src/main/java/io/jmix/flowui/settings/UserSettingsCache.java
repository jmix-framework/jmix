/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.settings;

import io.jmix.core.annotation.Internal;

import javax.annotation.Nullable;

/**
 * Provides saving/loading settings using the cache.
 */
@Internal
public interface UserSettingsCache {

    /**
     * @param key setting identifier
     * @return setting from cache or if cache does not contain setting, loads it from store
     */
    @Nullable
    String get(String key);

    /**
     * Sets a setting to the cache and store.
     *
     * @param key  setting identifier
     * @param value setting value
     */
    void set(String key, @Nullable String value);

    /**
     * Deletes setting from cache and store.
     *
     * @param key setting identifier
     */
    void delete(String key);

    /**
     * Clears cache.
     */
    void clear();
}
