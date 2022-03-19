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

package io.jmix.ui.settings.component.binder;

import io.jmix.ui.component.Component;
import io.jmix.ui.component.impl.TableImpl;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SettingsWrapper;
import io.jmix.ui.settings.component.TableSettings;

/**
 * Base interface for component settings registration. As an example see {@link TableSettingsBinder}.
 */
public interface ComponentSettingsBinder<V extends Component, S extends ComponentSettings> {

    /**
     * @return component class, e.g. {@link TableImpl}
     */
    Class<? extends Component> getComponentClass();

    /**
     * @return component settings class, e.g. {@link TableSettings}
     */
    Class<? extends ComponentSettings> getSettingsClass();

    /**
     * Applies settings to the component
     *
     * @param component component
     * @param wrapper   settings wrapper contains settings for the component
     */
    void applySettings(V component, SettingsWrapper wrapper);

    /**
     * @param component component
     * @param wrapper   settings wrapper contains settings for the component
     * @return true if settings were modified
     */
    boolean saveSettings(V component, SettingsWrapper wrapper);

    /**
     * @param component component
     * @return current component settings. It retrieves current property values from component and creates new settings
     * instance.
     */
    S getSettings(V component);
}
