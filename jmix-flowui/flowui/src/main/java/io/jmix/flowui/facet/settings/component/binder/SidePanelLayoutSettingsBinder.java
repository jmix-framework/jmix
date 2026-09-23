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

package io.jmix.flowui.facet.settings.component.binder;

import com.vaadin.flow.component.Component;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.component.sidepanellayout.SidePanelLayout;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.component.SidePanelLayoutSettings;
import org.springframework.core.annotation.Order;

import java.util.Objects;

/**
 * Binder that applies and saves {@link SidePanelLayoutSettings} for a {@link SidePanelLayout}.
 * Only active when the panel is {@link SidePanelLayout#isSidePanelResizable() resizable}; fixed
 * panels are left untouched by user settings.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE)
@org.springframework.stereotype.Component("flowui_SidePanelLayoutSettingsBinder")
public class SidePanelLayoutSettingsBinder
        implements ComponentSettingsBinder<SidePanelLayout, SidePanelLayoutSettings> {

    @Override
    public Class<? extends Component> getComponentClass() {
        return SidePanelLayout.class;
    }

    @Override
    public Class<? extends Settings> getSettingsClass() {
        return SidePanelLayoutSettings.class;
    }

    @Override
    public void applySettings(SidePanelLayout component, SidePanelLayoutSettings settings) {
        if (!component.isSidePanelResizable()) {
            return;
        }
        if (settings.getHorizontalSize() != null) {
            component.setSidePanelHorizontalSize(settings.getHorizontalSize());
        }
        if (settings.getVerticalSize() != null) {
            component.setSidePanelVerticalSize(settings.getVerticalSize());
        }
    }

    @Override
    public boolean saveSettings(SidePanelLayout component, SidePanelLayoutSettings settings) {
        if (!component.isSidePanelResizable()) {
            return false;
        }
        boolean changed = false;
        if (!Objects.equals(settings.getHorizontalSize(), component.getSidePanelHorizontalSize())) {
            settings.setHorizontalSize(component.getSidePanelHorizontalSize());
            changed = true;
        }
        if (!Objects.equals(settings.getVerticalSize(), component.getSidePanelVerticalSize())) {
            settings.setVerticalSize(component.getSidePanelVerticalSize());
            changed = true;
        }
        return changed;
    }

    @Override
    public SidePanelLayoutSettings getSettings(SidePanelLayout component) {
        SidePanelLayoutSettings settings = createSettings();
        settings.setId(component.getId().orElse(null));
        if (component.isSidePanelResizable()) {
            settings.setHorizontalSize(component.getSidePanelHorizontalSize());
            settings.setVerticalSize(component.getSidePanelVerticalSize());
        }
        return settings;
    }

    protected SidePanelLayoutSettings createSettings() {
        return new SidePanelLayoutSettings();
    }
}
