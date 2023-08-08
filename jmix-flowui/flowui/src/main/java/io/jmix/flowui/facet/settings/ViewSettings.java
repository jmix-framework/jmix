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

package io.jmix.flowui.facet.settings;

import javax.annotation.Nullable;
import java.util.Optional;

public interface ViewSettings {

    String getViewId();

    /**
     * Set to {@code true} if screen settings changed manually. It guarantees that settings will be persisted.
     *
     * @param modified whether settings were modified
     */
    void setModified(boolean modified);

    /**
     * @return {@code true} if settings were modified
     */
    boolean isModified();

    /**
     * Puts a value with {@link String} type. Will replace value if the same key already exist.
     *
     * @param id    e.g. component id
     * @param key   key with which associated provided value, e.g. component's width or some state
     * @param value {@link String} value
     * @return current instance of settings
     */
    ViewSettings put(String id, String key, @Nullable String value);

    /**
     * Puts a value with {@link Integer} type. Will replace value if the same key already exist.
     *
     * @param id    e.g. component id
     * @param key   key with which associated provided value, e.g. component's property or some state
     * @param value {@link Integer} value
     * @return current instance of settings
     */
    ViewSettings put(String id, String key, @Nullable Integer value);

    /**
     * Puts a value with {@link Long} type. Will replace value if the same key already exist.
     *
     * @param id    e.g. component id
     * @param key   key with which associated provided value, e.g. component's property or some state
     * @param value {@link Long} value
     * @return current instance of settings
     */
    ViewSettings put(String id, String key, @Nullable Long value);

    /**
     * Puts a value with {@link Double} type. Will replace value if the same key already exist.
     *
     * @param id    e.g. component id
     * @param key   key with which associated provided value, e.g. component's property or some state
     * @param value {@link Double} value
     * @return current instance of settings
     */
    ViewSettings put(String id, String key, @Nullable Double value);

    /**
     * Puts a value with {@link Boolean}. Will replace value if the same key already exist.
     *
     * @param id    e.g. component id
     * @param key   key with which associated provided value, e.g. component's property or some state
     * @param value {@link Boolean} value
     * @return current instance of settings
     */
    ViewSettings put(String id, String key, @Nullable Boolean value);

    /**
     * Puts component's settings, e.g {@link TableSettings}. If setting with provided id already exist it will be
     * replaced.
     *
     * @param settings component settings
     * @return current instance of settings
     */
    ViewSettings put(ComponentSettings settings);

    /**
     * Deletes component's settings by identifier if they exist.
     *
     * @param id id to remove, e.g. component id
     * @return current instance of settings
     */
    ViewSettings delete(String id);

    /**
     * Deletes a key of object with provided identifier if it exists.
     *
     * @param id  e.g. component id
     * @param key object's key to remove
     * @return current instance of screen settings
     */
    ViewSettings delete(String id, String key);

    /**
     * @param id  e.g. component id
     * @param key object's key
     * @return {@link String} value wrapped in {@code Optional}
     */
    Optional<String> getString(String id, String key);

    /**
     * @param id  e.g. component id
     * @param key object's key
     * @return {@link Integer} value wrapped in {@code Optional}
     */
    Optional<Integer> getInteger(String id, String key);

    /**
     * @param id  e.g. component id
     * @param key object's key
     * @return {@link Long} value wrapped in {@code Optional}
     */
    Optional<Long> getLong(String id, String key);

    /**
     * @param id  e.g. component id
     * @param key object's key
     * @return {@link Double} value wrapped in {@code Optional}
     */
    Optional<Double> getDouble(String id, String key);

    /**
     * @param id  e.g. component id
     * @param key object's key
     * @return {@link Boolean} value wrapped in {@code Optional}
     */
    Optional<Boolean> getBoolean(String id, String key);

    /**
     * @param componentId   component id
     * @param settingsClass settings class
     * @param <T>           type of component settings class
     * @return component settings wrapped in {@code Optional}
     */
    <T extends ComponentSettings> Optional<T> getSettings(String componentId, Class<T> settingsClass);

    /**
     * @param componentId   component id
     * @param settingsClass settings class
     * @param <T>           type of component settings class
     * @return component settings if exist otherwise return created settings with corresponding id
     */
    <T extends ComponentSettings> T getSettingsOrCreate(String componentId, Class<T> settingsClass);

    void initialize(@Nullable String rawSettings);
}
