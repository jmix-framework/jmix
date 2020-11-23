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

import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Service providing current user settings functionality:
 * an application can save/load some "setting" (plain or XML string) for current user.
 * <br>
 * It is usually used by UI forms and components.
 */
public interface UserSettingService {

    /**
     * Load settings for the current user and null client type. Returns null if no such setting found.
     */
    @Nullable
    String loadSetting(String name);

    /**
     * Save settings for the current user and null client type
     */
    void saveSetting(String name, @Nullable String value);

    /**
     * Delete settings for the current user
     */
    void deleteSettings(String name);

    /**
     * Copy user settings to another user
     */
    void copySettings(UserDetails fromUser, UserDetails toUser);

    /**
     * Delete settings of screens (settings of tables, filters etc) for the current user.
     *
     * @param screens    set of window ids, whose settings must be deleted
     */
    void deleteScreenSettings(Set<String> screens);
}