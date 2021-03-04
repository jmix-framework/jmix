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

import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasTablePresentations;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.settings.AbstractScreenSettings;
import io.jmix.ui.settings.ScreenSettingsManager;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.ComponentSettings.HasSettingsPresentation;
import io.jmix.ui.settings.component.SettingsWrapperImpl;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.settings.component.binder.DataLoadingSettingsBinder;
import io.jmix.ui.settings.ScreenSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.UUID;

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
            if (!settingsRegistry.isSettingsRegisteredFor(component.getClass())
                    || component.getId() == null) {
                continue;
            }

            log.trace("Applying settings for {} : {} ", getComponentPath(component), component);

            ComponentSettingsBinder binder = settingsRegistry.getBinder(component.getClass());

            if (component instanceof HasTablePresentations) {
                ComponentSettings defaultSettings = binder.getSettings(component);
                ((HasTablePresentations) component).setDefaultSettings(new SettingsWrapperImpl(defaultSettings));
            }

            Class<? extends ComponentSettings> settingsClass = settingsRegistry.getSettingsClass(component.getClass());
            ComponentSettings settings = screenSettings.getSettingsOrCreate(component.getId(), settingsClass);

            binder.applySettings(component, new SettingsWrapperImpl(settings));

            if (component instanceof HasTablePresentations
                    && settings instanceof HasSettingsPresentation) {
                UUID presentationId = ((HasSettingsPresentation) settings).getPresentationId();
                if (presentationId != null) {
                    ((HasTablePresentations) component).applyPresentationAsDefault(presentationId);
                }
            }
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

            ComponentSettingsBinder binder = settingsRegistry.getBinder(component.getClass());

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

            ComponentSettingsBinder binder = settingsRegistry.getBinder(component.getClass());

            boolean settingsChanged = binder.saveSettings(component, new SettingsWrapperImpl(settings));
            if (settingsChanged) {
                isModified = true;

                screenSettings.put(settings);
            }

            if (component instanceof HasTablePresentations) {
                HasTablePresentations compWithPres = (HasTablePresentations) component;
                if (compWithPres.isUsePresentations()) {
                    TablePresentations presentations = compWithPres.getPresentations();
                    presentations.commit();
                }
            }
        }

        if (isModified || screenSettings.isModified()) {
            if (screenSettings instanceof AbstractScreenSettings) {
                ((AbstractScreenSettings) screenSettings).commit();
            }
        }
    }
}
