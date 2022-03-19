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

package com.haulmont.cuba.settings.component;

import com.haulmont.cuba.gui.components.HasDataLoadingSettings;
import com.haulmont.cuba.gui.components.HasSettings;
import io.jmix.ui.component.Component;
import com.haulmont.cuba.settings.converter.LegacySettingsConverter;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.HasTablePresentations;
import com.haulmont.cuba.settings.CubaLegacySettings;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SettingsWrapperImpl;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.settings.component.binder.DataLoadingSettingsBinder;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

@SuppressWarnings({"rawtypes", "unchecked"})
@org.springframework.stereotype.Component(LegacySettingsDelegate.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LegacySettingsDelegate implements HasSettings, HasDataLoadingSettings {

    public static final String NAME = "cuba_LegacySettingsDelegate";

    protected Component component;

    protected LegacySettingsConverter settingsConverter;
    protected ComponentSettingsBinder settingsBinder;

    protected Document defaultSettings;

    protected boolean settingsEnabled = true;

    public LegacySettingsDelegate(Component component,
                                  LegacySettingsConverter settingsConverter,
                                  ComponentSettingsBinder settingsBinder) {
        this.component = component;
        this.settingsConverter = settingsConverter;
        this.settingsBinder = settingsBinder;
    }

    @Override
    public void applySettings(Element element) {
        if (!isSettingsEnabled()) {
            return;
        }

        if (defaultSettings == null
                && component instanceof HasTablePresentations) {
            // save default view before apply custom
            defaultSettings = DocumentHelper.createDocument();
            defaultSettings.setRootElement(defaultSettings.addElement("presentation"));

            saveSettings(defaultSettings.getRootElement());

            ComponentSettings settings = settingsConverter.convertToComponentSettings(defaultSettings.getRootElement());
            ((HasTablePresentations) component).setDefaultSettings(new SettingsWrapperImpl(settings));
        }

        ComponentSettings settings = settingsConverter.convertToComponentSettings(element);

        settingsBinder.applySettings(component, new SettingsWrapperImpl(settings));
    }

    @Override
    public void applyDataLoadingSettings(Element element) {
        if (!isSettingsEnabled()) {
            return;
        }

        ComponentSettings settings = settingsConverter.convertToComponentSettings(element);

        ((DataLoadingSettingsBinder) settingsBinder)
                .applyDataLoadingSettings(component, new SettingsWrapperImpl(settings));
    }

    @Override
    public boolean saveSettings(Element element) {
        if (!isSettingsEnabled()) {
            return false;
        }

        ComponentSettings settings = settingsConverter.convertToComponentSettings(element);

        boolean modified = settingsBinder.saveSettings(component, new SettingsWrapperImpl(settings));
        if (modified)
            settingsConverter.copyToElement(settings, element);

        return modified;
    }

    @Override
    public boolean isSettingsEnabled() {
        return settingsEnabled;
    }

    @Override
    public void setSettingsEnabled(boolean settingsEnabled) {
        this.settingsEnabled = settingsEnabled;
    }

    public boolean isLegacySettings(Frame frame) {
        return frame.getFrameOwner() instanceof CubaLegacySettings;
    }

    public Document getDefaultSettings() {
        return defaultSettings;
    }
}
