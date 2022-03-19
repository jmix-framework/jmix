/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.settings;

import io.jmix.core.annotation.Internal;

import javax.annotation.Nullable;

/**
 * Provides saving/loading settings using the cache.
 */
@Internal
public interface UiSettingsCache {

    /**
     * @param name setting name
     * @return setting from cache or if cache does not contain setting, loads it from store
     */
    @Nullable
    String getSetting(String name);

    /**
     * Sets a setting to the cache and store.
     *
     * @param name  setting name
     * @param value setting value
     */
    void setSetting(String name, @Nullable String value);

    /**
     * Deletes setting from cache and store.
     *
     * @param name setting name
     */
    void deleteSettings(String name);

    /**
     * Clears cache.
     */
    void clear();
}
