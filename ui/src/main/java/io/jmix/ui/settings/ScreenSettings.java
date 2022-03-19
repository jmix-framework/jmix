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

import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.TableSettings;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Base interface for screen settings container. It provides API for putting, getting, removing component settings.
 */
public interface ScreenSettings {

    /**
     * Set to true if screen settings changed manually. It will guarantee that settings will be persisted.
     *
     * @param modified whether settings were modified
     */
    void setModified(boolean modified);

    /**
     * @return true if screen setting were modified
     */
    boolean isModified();

    /**
     * Puts a String value. Will replace value if property already exist.
     *
     * @param componentId component id
     * @param property    component's property
     * @param value       String value
     * @return current instance of screen settings
     */
    ScreenSettings put(String componentId, String property, @Nullable String value);

    /**
     * Puts an Integer value. Will replace value if property already exist.
     *
     * @param componentId component id
     * @param property    component's property
     * @param value       Integer value
     * @return current instance of screen settings
     */
    ScreenSettings put(String componentId, String property, @Nullable Integer value);

    /**
     * Puts a Long value. Will replace value if property already exist.
     *
     * @param componentId component id
     * @param property    component's property
     * @param value       Long value
     * @return current instance of screen settings
     */
    ScreenSettings put(String componentId, String property, @Nullable Long value);

    /**
     * Puts a Double value. Will replace value if property already exist.
     *
     * @param componentId component id
     * @param property    component's property
     * @param value       Double value
     * @return current instance of screen settings
     */
    ScreenSettings put(String componentId, String property, @Nullable Double value);

    /**
     * Puts a Boolean value. Will replace value if property already exist.
     *
     * @param componentId component id
     * @param property    component's property
     * @param value       Boolean value
     * @return current instance of screen settings
     */
    ScreenSettings put(String componentId, String property, @Nullable Boolean value);

    /**
     * Puts component's settings, e.g {@link TableSettings}. If settings with provided id already exist they will be
     * replaced.
     *
     * @param settings component settings
     * @return current instance of screen settings
     */
    ScreenSettings put(ComponentSettings settings);

    /**
     * Removes component's settings if they exist.
     *
     * @param componentId component id to remove
     * @return current instance of screen settings
     */
    ScreenSettings remove(String componentId);

    /**
     * Removes property of component's settings if it exists.
     *
     * @param componentId component id
     * @param property    component's property to remove
     * @return current instance of screen settings
     */
    ScreenSettings remove(String componentId, String property);

    /**
     * @param componentId component id
     * @param property    component's property
     * @return String value wrapped in {@code Optional}
     */
    Optional<String> getString(String componentId, String property);

    /**
     * @param componentId component id
     * @param property    component's property
     * @return Integer value wrapped in {@code Optional}
     */
    Optional<Integer> getInteger(String componentId, String property);

    /**
     * @param componentId component id
     * @param property    component's property
     * @return Long value wrapped in {@code Optional}
     */
    Optional<Long> getLong(String componentId, String property);

    /**
     * @param componentId component id
     * @param property    component's property
     * @return Double value wrapped in {@code Optional}
     */
    Optional<Double> getDouble(String componentId, String property);

    /**
     * @param componentId component id
     * @param property    component's property
     * @return Boolean value wrapped in {@code Optional}
     */
    Optional<Boolean> getBoolean(String componentId, String property);

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
}
