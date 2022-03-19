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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.CubaComponentsHelper;
import com.haulmont.cuba.gui.components.GroupBoxLayout;
import com.haulmont.cuba.settings.component.LegacySettingsDelegate;
import com.haulmont.cuba.settings.converter.LegacyGroupBoxSettingsConverter;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.impl.GroupBoxImpl;
import io.jmix.ui.settings.ComponentSettingsRegistry;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

@Deprecated
public class WebGroupBox extends GroupBoxImpl implements GroupBoxLayout {

    protected ComponentSettingsRegistry settingsRegistry;
    protected LegacySettingsDelegate settingsDelegate;

    @Autowired
    public void setSettingsRegistry(ComponentSettingsRegistry settingsRegistry) {
        this.settingsRegistry = settingsRegistry;
    }

    @Override
    public void expand(Component component, String height, String width) {
        com.vaadin.ui.Component expandedComponent = ComponentsHelper.getComposition(component);
        CubaComponentsHelper.expand(getComponentContent(), expandedComponent, height, width);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        settingsDelegate = createSettingsDelegate();
    }

    @Override
    public void applySettings(Element element) {
        settingsDelegate.applySettings(element);
    }

    @Override
    public boolean saveSettings(Element element) {
        return settingsDelegate.saveSettings(element);
    }

    @Override
    public boolean isSettingsEnabled() {
        return settingsDelegate.isSettingsEnabled();
    }

    @Override
    public void setSettingsEnabled(boolean settingsEnabled) {
        settingsDelegate.setSettingsEnabled(settingsEnabled);
    }

    protected LegacySettingsDelegate createSettingsDelegate() {
        return (LegacySettingsDelegate) applicationContext.getBean(LegacySettingsDelegate.NAME,
                this, new LegacyGroupBoxSettingsConverter(), getSettingsBinder());
    }

    protected ComponentSettingsBinder getSettingsBinder() {
        return settingsRegistry.getSettingsBinder(this.getClass());
    }

    @Override
    public void removeExpandedStateChangeListener(Consumer<ExpandedStateChangeEvent> listener) {
        unsubscribe(ExpandedStateChangeEvent.class, listener);
    }
}
