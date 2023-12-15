/*
 * Copyright 2023 Haulmont.
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
import io.jmix.core.JmixOrder;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.component.GenericFilterSettings;
import org.springframework.core.annotation.Order;

import java.util.Objects;

@Order(JmixOrder.LOWEST_PRECEDENCE)
@org.springframework.stereotype.Component("flowui_GenericFilterSettingsBinder")
public class GenericFilterSettingsBinder implements ComponentSettingsBinder<GenericFilter, GenericFilterSettings> {

    @Override
    public Class<? extends Component> getComponentClass() {
        return GenericFilter.class;
    }

    @Override
    public Class<? extends Settings> getSettingsClass() {
        return GenericFilterSettings.class;
    }

    @Override
    public void applySettings(GenericFilter component, GenericFilterSettings settings) {
        if (settings.getOpened() != null) {
            component.setOpened(settings.getOpened());
        }

        if (settings.getDefaultConfigurationId() != null) {
            Configuration defaultConfiguration = component.getConfiguration(settings.getDefaultConfigurationId());

            if (defaultConfiguration != null) {
                component.setCurrentConfiguration(defaultConfiguration);
                component.apply();
            }
        }
    }

    @Override
    public boolean saveSettings(GenericFilter component, GenericFilterSettings settings) {
        if (!Objects.equals(settings.getOpened(), component.isOpened())) {
            settings.setOpened(component.isOpened());
            return true;
        }
        return false;
    }

    @Override
    public GenericFilterSettings getSettings(GenericFilter component) {
        GenericFilterSettings settings = createSettings();
        settings.setId(component.getId().orElse(null));
        settings.setOpened(component.isOpened());
        return settings;
    }

    protected GenericFilterSettings createSettings() {
        return new GenericFilterSettings();
    }
}
