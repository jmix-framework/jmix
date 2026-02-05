/*
 * Copyright 2025 Haulmont.
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

import com.vaadin.flow.component.Component;
import io.jmix.flowui.facet.settings.component.binder.ComponentSettingsBinder;
import io.jmix.flowui.facet.settings.component.binder.DataGridSettingsBinder;

import java.util.Collection;

/**
 * Saves and restores settings for components.
 */
public interface ComponentSettingsManager {

    /**
     * Applies settings from {@link UiComponentSettings} to provided components.
     * <p>
     * Note, component should have and id and {@link ComponentSettingsBinder}. Otherwise, it will be skipped
     * while applying settings. See {@link DataGridSettingsBinder} as an example.
     *
     * @param components components to apply settings
     * @param settings   {@link UiComponentSettings}
     */
    void applySettings(Collection<Component> components, UiComponentSettings<?> settings);

    /**
     * Applies data loading settings from {@link UiComponentSettings} to provided components.
     * <p>
     * Note, component should have and id and {@link ComponentSettingsBinder}. Otherwise, it will be skipped
     * while applying settings. See {@link DataGridSettingsBinder} as an example.
     *
     * @param components components to apply settings
     * @param settings   {@link UiComponentSettings}
     */
    void applyDataLoadingSettings(Collection<Component> components, UiComponentSettings<?> settings);

    /**
     * Persists settings if they are changed or {@link UiComponentSettings#isModified()} returns {@code true}.
     * <p>
     * Note, component should have and id and {@link ComponentSettingsBinder}. Otherwise, it will be skipped
     * while saving settings. See {@link DataGridSettingsBinder} as an example.
     *
     * @param components components to save settings
     * @param settings   {@link UiComponentSettings}
     */
    void saveSettings(Collection<Component> components, UiComponentSettings<?> settings);
}
