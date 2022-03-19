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

package io.jmix.uidata.settings;

import com.google.common.base.Strings;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.Component;
import io.jmix.ui.settings.AbstractScreenSettings;
import io.jmix.ui.settings.ComponentSettingsRegistry;
import io.jmix.ui.settings.ScreenSettingsManager;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SettingsWrapperImpl;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.settings.component.binder.DataLoadingSettingsBinder;
import io.jmix.ui.settings.ScreenSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

import static io.jmix.ui.component.ComponentsHelper.getComponentPath;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ScreenSettingsManagerImpl implements ScreenSettingsManager {

    private static final Logger log = LoggerFactory.getLogger(ScreenSettingsManagerImpl.class);

    @Autowired
    protected ComponentSettingsRegistry settingsRegistry;

    @Override
    public void applySettings(Collection<Component> components, ScreenSettings screenSettings) {
        Preconditions.checkNotNullArgument(components);
        Preconditions.checkNotNullArgument(screenSettings);

        for (Component component : components) {
            if (Strings.isNullOrEmpty(component.getId())
                    || !settingsRegistry.isSettingsRegisteredFor(component.getClass())) {
                continue;
            }

            log.trace("Applying settings for {} : {} ", getComponentPath(component), component);

            ComponentSettingsBinder binder = settingsRegistry.getSettingsBinder(component.getClass());

            Class<? extends ComponentSettings> settingsClass = settingsRegistry.getSettingsClass(component.getClass());
            ComponentSettings settings = screenSettings.getSettingsOrCreate(component.getId(), settingsClass);

            binder.applySettings(component, new SettingsWrapperImpl(settings));
        }
    }

    @Override
    public void applyDataLoadingSettings(Collection<Component> components, ScreenSettings screenSettings) {
        Preconditions.checkNotNullArgument(components);
        Preconditions.checkNotNullArgument(screenSettings);

        for (Component component : components) {
            if (!settingsRegistry.isSettingsRegisteredFor(component.getClass())
                    || component.getId() == null) {
                continue;
            }

            log.trace("Applying settings for {} : {} ", getComponentPath(component), component);

            Class<? extends ComponentSettings> settingsClass = settingsRegistry.getSettingsClass(component.getClass());

            ComponentSettingsBinder binder = settingsRegistry.getSettingsBinder(component.getClass());

            if (binder instanceof DataLoadingSettingsBinder) {
                ComponentSettings settings = screenSettings.getSettingsOrCreate(component.getId(), settingsClass);
                ((DataLoadingSettingsBinder) binder).applyDataLoadingSettings(component, new SettingsWrapperImpl(settings));
            }
        }
    }

    @Override
    public void saveSettings(Collection<Component> components, ScreenSettings screenSettings) {
        Preconditions.checkNotNullArgument(components);
        Preconditions.checkNotNullArgument(screenSettings);

        boolean isModified = false;

        for (Component component : components) {
            if (!settingsRegistry.isSettingsRegisteredFor(component.getClass())
                    || component.getId() == null) {
                continue;
            }

            log.trace("Saving settings for {} : {}", getComponentPath(component), component);

            Class<? extends ComponentSettings> settingsClass = settingsRegistry.getSettingsClass(component.getClass());

            ComponentSettings settings = screenSettings.getSettingsOrCreate(component.getId(), settingsClass);

            ComponentSettingsBinder binder = settingsRegistry.getSettingsBinder(component.getClass());

            boolean settingsChanged = binder.saveSettings(component, new SettingsWrapperImpl(settings));
            if (settingsChanged) {
                isModified = true;

                screenSettings.put(settings);
            }
        }

        if (isModified || screenSettings.isModified()) {
            if (screenSettings instanceof AbstractScreenSettings) {
                ((AbstractScreenSettings) screenSettings).commit();
            }
        }
    }
}
