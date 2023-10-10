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
import io.jmix.core.JmixOrder;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.component.JmixDetailsSettings;
import org.springframework.core.annotation.Order;

import java.util.Objects;

@Order(JmixOrder.LOWEST_PRECEDENCE)
@org.springframework.stereotype.Component("flowui_JmixDetailsSettingsBinder")
public class JmixDetailsSettingsBinder implements ComponentSettingsBinder<JmixDetails, JmixDetailsSettings> {

    @Override
    public Class<? extends Component> getComponentClass() {
        return JmixDetails.class;
    }

    @Override
    public Class<? extends Settings> getSettingsClass() {
        return JmixDetailsSettings.class;
    }

    @Override
    public void applySettings(JmixDetails component, JmixDetailsSettings settings) {
        if (settings.getOpened() != null) {
            component.setOpened(settings.getOpened());
        }
    }

    @Override
    public boolean saveSettings(JmixDetails component, JmixDetailsSettings settings) {
        if (!Objects.equals(settings.getOpened(), component.isOpened())) {
            settings.setOpened(component.isOpened());
            return true;
        }
        return false;
    }

    @Override
    public JmixDetailsSettings getSettings(JmixDetails component) {
        JmixDetailsSettings settings = createSettings();
        settings.setId(component.getId().orElse(null));
        settings.setOpened(component.isOpened());
        return settings;
    }

    protected JmixDetailsSettings createSettings() {
        return new JmixDetailsSettings();
    }
}
