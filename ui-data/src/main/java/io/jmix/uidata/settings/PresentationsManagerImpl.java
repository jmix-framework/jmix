/*
 * Copyright 2021 Haulmont.
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
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasTablePresentations;
import io.jmix.ui.presentation.PresentationsManager;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.settings.ComponentSettingsRegistry;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SettingsWrapperImpl;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Objects;

public class PresentationsManagerImpl implements PresentationsManager {

    protected ComponentSettingsRegistry settingsRegistry;

    @Autowired
    public PresentationsManagerImpl(ComponentSettingsRegistry settingsRegistry) {
        this.settingsRegistry = settingsRegistry;
    }

    @Override
    public void setupDefaultSettings(Collection<Component> components) {
        for (Component component : components) {
            if (Strings.isNullOrEmpty(component.getId())
                    || !settingsRegistry.isSettingsRegisteredFor(component.getClass())
                    || !(component instanceof HasTablePresentations)) {
                continue;
            }

            ComponentSettingsBinder binder = settingsRegistry.getSettingsBinder(component.getClass());

            ComponentSettings defaultSettings = binder.getSettings(component);
            ((HasTablePresentations) component).setDefaultSettings(new SettingsWrapperImpl(defaultSettings));
        }
    }

    @Override
    public void applyDefaultPresentation(Collection<Component> components) {
        for (Component component : components) {
            if (Strings.isNullOrEmpty(component.getId())
                    || !settingsRegistry.isSettingsRegisteredFor(component.getClass())
                    || !(component instanceof HasTablePresentations)
                    || ((HasTablePresentations) component).getPresentations() == null) {
                continue;
            }

            TablePresentations presentations = ((HasTablePresentations) component).getPresentations();
            assert presentations != null;

            for (Object id : presentations.getPresentationIds()) {
                TablePresentation presentation = presentations.getPresentation(id);

                if (presentation != null
                        && Boolean.TRUE.equals(presentation.getIsDefault())) {
                    ((HasTablePresentations) component).applyPresentationAsDefault(id);
                    break;
                }
            }
        }
    }

    @Override
    public void commitPresentations(Collection<Component> components) {
        for (Component component : components) {
            if (Strings.isNullOrEmpty(component.getId())
                    || !settingsRegistry.isSettingsRegisteredFor(component.getClass())
                    || !(component instanceof HasTablePresentations)
                    || ((HasTablePresentations) component).getPresentations() == null) {
                continue;
            }

            Objects.requireNonNull(((HasTablePresentations) component).getPresentations()).commit();
        }
    }
}
