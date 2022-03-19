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

import io.jmix.ui.component.impl.AbstractPagination;
import io.jmix.ui.settings.component.AbstractPaginationSettings;
import io.jmix.ui.settings.component.SettingsWrapper;

import java.util.Objects;

public abstract class AbstractPaginationSettingsBinder
        <C extends AbstractPagination, S extends AbstractPaginationSettings>
        implements DataLoadingSettingsBinder<C, S> {

    @Override
    public void applySettings(C component, SettingsWrapper wrapper) {
        // do nothing
    }

    @Override
    public void applyDataLoadingSettings(C component, SettingsWrapper wrapper) {
        S settings = wrapper.getSettings();

        if (component.isItemsPerPageVisible()) {
            Integer value = settings.getItemsPerPageValue();
            if (value != null
                    || Boolean.TRUE.equals(settings.getIsItemsPerPageUnlimitedOption())) {
                component.setItemsPerPageValue(value);
            }
        }
    }

    @Override
    public S getSettings(C component) {
        S settings = createSettings();

        if (component.isItemsPerPageVisible()) {
            Integer value = component.getItemsPerPageValue();
            if (value == null) {
                if (component.isItemsPerPageUnlimitedOptionVisible()) {
                    settings.setIsItemsPerPageUnlimitedOption(true);
                }
            } else {
                settings.setItemsPerPageValue(value);
            }
        }
        return settings;
    }

    @Override
    public boolean saveSettings(C component, SettingsWrapper wrapper) {
        S settings = wrapper.getSettings();
        if (isSettingsChanged(component, settings)) {
            Integer value = component.getItemsPerPageValue();
            if (value == null && component.isItemsPerPageUnlimitedOptionVisible()) {
                settings.setIsItemsPerPageUnlimitedOption(true);
            }
            settings.setItemsPerPageValue(value);
            return true;
        }
        return false;
    }

    protected boolean isSettingsChanged(C component, S settings) {
        if (!component.isItemsPerPageVisible()) {
            return false;
        }

        Integer compValue = component.getItemsPerPageValue();
        Integer settingsValue = settings.getItemsPerPageValue();

        if (compValue == null && settingsValue == null) {
            boolean compUnlimitedOption = component.isItemsPerPageUnlimitedOptionVisible();
            if (!compUnlimitedOption) {
                return false;
            }
            return !Boolean.TRUE.equals(settings.getIsItemsPerPageUnlimitedOption());
        }
        return !Objects.equals(compValue, settingsValue);
    }

    protected abstract S createSettings();
}
