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

import io.jmix.ui.component.Component;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.settings.component.binder.TableSettingsBinder;

import java.util.Collection;

/**
 * Provides functionality for applying and saving component settings.
 */
public interface ScreenSettingsManager {

    /**
     * Applies settings for component if {@link ComponentSettingsBinder} is created for it. See
     * {@link TableSettingsBinder} as an example.
     *
     * @param components     components to apply settings
     * @param screenSettings screen settings
     */
    void applySettings(Collection<Component> components, ScreenSettings screenSettings);

    /**
     * Applies data loading settings for component if {@link ComponentSettingsBinder} is created for it. See
     * {@link TableSettingsBinder} as an example.
     *
     * @param components     components to apply settings
     * @param screenSettings screen settings
     */
    void applyDataLoadingSettings(Collection<Component> components, ScreenSettings screenSettings);

    /**
     * Saves settings and persist if they are changed or screen settings is modified. {@link ComponentSettingsBinder}
     * must be created for component. See {@link TableSettingsBinder} as an example.
     *
     * @param components     components to save settings
     * @param screenSettings screen settings
     */
    void saveSettings(Collection<Component> components, ScreenSettings screenSettings);
}
