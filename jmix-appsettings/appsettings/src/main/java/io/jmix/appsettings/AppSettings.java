/*
 * Copyright 2026 Haulmont.
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

package io.jmix.appsettings;

import io.jmix.appsettings.entity.AppSettingsEntity;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface AppSettings {

    /**
     * Loads application settings entity of type {@code T}.
     * Note, that in default implementation attributes with empty values will be replaced with default values if specified.
     *
     * @param clazz class of entity that extends {@link AppSettingsEntity}
     */
    <T extends AppSettingsEntity> T load(Class<T> clazz);

    /**
     * Saves application settings entity of type {@code T}.
     * Note, that in default implementation attributes with values equal to default will be overridden with null value.
     *
     * @param settingsEntityToSave entity to be saved
     */
    <T extends AppSettingsEntity> void save(T settingsEntityToSave);

}
