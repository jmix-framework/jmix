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

package io.jmix.flowui.facet.settings.component.binder;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.component.JmixDetailsSettings;

/**
 * Base interface for component settings registration. As an example see {@link JmixDetailsSettingsBinder}.
 */
public interface ComponentSettingsBinder<V extends Component, S extends Settings> {

    /**
     * @return component class, e.g. {@link JmixDetails}
     */
    Class<? extends Component> getComponentClass();

    /**
     * @return component settings class, e.g. {@link JmixDetailsSettings}
     */
    Class<? extends Settings> getSettingsClass();

    /**
     * Applies settings to the component.
     *
     * @param component component
     * @param settings  settings for the component
     */
    void applySettings(V component, S settings);

    /**
     * Invoked when component properties or states should be saved to settings.
     *
     * @param component component
     * @param settings  settings for the component
     * @return {@code true} if settings were modified
     */
    boolean saveSettings(V component, S settings);

    /**
     * @param component component
     * @return current component settings. It retrieves current property values from component and creates new settings
     * instance.
     */
    S getSettings(V component);
}
