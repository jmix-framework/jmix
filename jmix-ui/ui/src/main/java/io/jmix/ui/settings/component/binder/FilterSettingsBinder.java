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

import io.jmix.core.JmixOrder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.impl.FilterImpl;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.FilterSettings;
import io.jmix.ui.settings.component.SettingsWrapper;
import org.springframework.core.annotation.Order;

@Order(JmixOrder.LOWEST_PRECEDENCE)
@org.springframework.stereotype.Component("ui_FilterSettingsBinder")
public class FilterSettingsBinder implements ComponentSettingsBinder<Filter, FilterSettings> {

    @Override
    public Class<? extends Component> getComponentClass() {
        return FilterImpl.class;
    }

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return FilterSettings.class;
    }

    @Override
    public void applySettings(Filter component, SettingsWrapper wrapper) {
        FilterSettings settings = wrapper.getSettings();

        if (settings.getDefaultConfigurationId() != null) {
            Filter.Configuration defaultConfiguration = component.getConfiguration(settings.getDefaultConfigurationId());
            if (defaultConfiguration != null) {
                component.setCurrentConfiguration(defaultConfiguration);
                component.apply();
            }
        }
    }

    @Override
    public boolean saveSettings(Filter component, SettingsWrapper wrapper) {
        return false;
    }

    @Override
    public FilterSettings getSettings(Filter component) {
        FilterSettings settings = createSettings();
        settings.setId(component.getId());

        return settings;
    }

    protected FilterSettings createSettings() {
        return new FilterSettings();
    }
}
