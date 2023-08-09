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
package io.jmix.flowui.settings;

import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * Service providing settings functionality for the current user. An application can save/load
 * some "setting" (plain or JSON/XML string) for the current user.
 * <p>
 * It is usually used by UI Views and components.
 */
public interface UserSettingsService {

    /**
     * Loads setting for the current user.
     *
     * @param key the setting identifier
     * @return loaded setting value
     */
    Optional<String> load(String key);

    /**
     * Saves value for the current user.
     *
     * @param key setting identifier
     * @param value setting value
     */
    void save(String key, @Nullable String value);

    /**
     * Deletes setting by key for the current user.
     */
    void delete(String key);

    /**
     * Copies all settings to another user.
     */
    void copy(String fromUsername, String toUsername);

    // todo rp delete view settings
    /**
     * Delete settings of screens (settings of tables, filters etc) for the current user.
     *
     * @param screens    set of window ids, whose settings must be deleted
     *//*
    void deleteScreenSettings(Set<String> screens);*/
}