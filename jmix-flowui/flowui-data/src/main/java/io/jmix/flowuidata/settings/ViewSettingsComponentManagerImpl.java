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

package io.jmix.flowuidata.settings;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.ViewSettings;
import io.jmix.flowui.facet.settings.ViewSettingsComponentManager;
import io.jmix.flowui.facet.settings.ViewSettingsComponentRegistry;
import io.jmix.flowui.facet.settings.component.binder.ComponentSettingsBinder;
import io.jmix.flowui.facet.settings.component.binder.DataLoadingSettingsBinder;
import io.jmix.flowui.settings.UserSettingsCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@org.springframework.stereotype.Component("flowui_ViewSettingsComponentManagerImpl")
public class ViewSettingsComponentManagerImpl implements ViewSettingsComponentManager {

    private static final Logger log = LoggerFactory.getLogger(ViewSettingsComponentManagerImpl.class);

    protected ViewSettingsComponentRegistry settingsRegistry;
    protected UserSettingsCache userSettingsCache;

    public ViewSettingsComponentManagerImpl(ViewSettingsComponentRegistry settingsRegistry,
                                            UserSettingsCache userSettingsCache) {
        this.settingsRegistry = settingsRegistry;
        this.userSettingsCache = userSettingsCache;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void applySettings(Collection<Component> components, ViewSettings viewSettings) {
        Preconditions.checkNotNullArgument(components);
        Preconditions.checkNotNullArgument(viewSettings);

        for (Component component : components) {
            if (Strings.isNullOrEmpty(component.getId().orElse(null))
                    || !settingsRegistry.isSettingsRegisteredFor(component.getClass())) {
                continue;
            }

            log.trace("Applying settings for {} : {} ", component.getId().get(), component);

            ComponentSettingsBinder<Component, ?> binder = (ComponentSettingsBinder<Component, ?>)
                    settingsRegistry.getSettingsBinder(component.getClass());

            Class<? extends Settings> settingsClass = settingsRegistry.getSettingsClass(component.getClass());
            Settings settings = viewSettings.getSettingsOrCreate(component.getId().get(), settingsClass);

            binder.applySettings(component, settings.as());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void applyDataLoadingSettings(Collection<Component> components, ViewSettings viewSettings) {
        Preconditions.checkNotNullArgument(components);
        Preconditions.checkNotNullArgument(viewSettings);

        for (Component component : components) {
            if (!settingsRegistry.isSettingsRegisteredFor(component.getClass())
                    || component.getId().orElse(null) == null) {
                continue;
            }

            log.trace("Applying settings for {} : {} ", component.getId().get(), component);

            Class<? extends Settings> settingsClass = settingsRegistry.getSettingsClass(component.getClass());

            ComponentSettingsBinder<Component, ?> binder = (ComponentSettingsBinder<Component, ?>)
                    settingsRegistry.getSettingsBinder(component.getClass());

            if (binder instanceof DataLoadingSettingsBinder) {
                Settings settings = viewSettings.getSettingsOrCreate(component.getId().get(), settingsClass);
                ((DataLoadingSettingsBinder<Component, ?>) binder).applyDataLoadingSettings(component, settings.as());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void saveSettings(Collection<Component> components, ViewSettings viewSettings) {
        Preconditions.checkNotNullArgument(components);
        Preconditions.checkNotNullArgument(viewSettings);

        boolean isModified = false;

        for (Component component : components) {
            if (!settingsRegistry.isSettingsRegisteredFor(component.getClass())
                    || component.getId().orElse(null) == null) {
                continue;
            }

            log.trace("Saving settings for {} : {}", component.getId().get(), component);

            Class<? extends Settings> settingsClass = settingsRegistry.getSettingsClass(component.getClass());

            Settings settings = viewSettings.getSettingsOrCreate(component.getId().get(), settingsClass);

            ComponentSettingsBinder<Component, ?> binder = (ComponentSettingsBinder<Component, ?>)
                    settingsRegistry.getSettingsBinder(component.getClass());

            boolean settingsChanged = binder.saveSettings(component, settings.as());
            if (settingsChanged) {
                isModified = true;

                viewSettings.put(settings);
            }
        }

        if (isModified || viewSettings.isModified()) {
            userSettingsCache.put(viewSettings.getViewId(), viewSettings.serialize());
        }
    }
}
