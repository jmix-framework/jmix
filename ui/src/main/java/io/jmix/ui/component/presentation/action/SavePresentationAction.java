/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.presentation.action;

import io.jmix.ui.component.Component;
import io.jmix.ui.component.Table;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.settings.SettingsHelper;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SettingsWrapperImpl;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;

public class SavePresentationAction extends AbstractPresentationAction {

    public SavePresentationAction(Table table, ComponentSettingsBinder settingsBinder) {
        super(table, "PresentationsPopup.save", settingsBinder);
    }

    @Override
    public void actionPerform(Component component) {
        tableImpl.hidePresentationsPopup();

        TablePresentations presentations = table.getPresentations();
        TablePresentation current = presentations.getCurrent();

        setSettingsToPresentation(presentations, current);

        presentations.commit();
    }

    protected void setSettingsToPresentation(TablePresentations presentations, TablePresentation current) {
        ComponentSettings settings = SettingsHelper.createSettings(settingsBinder.getSettingsClass(), table.getId());

        String settingsString = presentations.getSettingsString(current);
        if (settingsString != null) {
            ComponentSettings convertedSettings =
                    SettingsHelper.toComponentSettings(settingsString, settingsBinder.getSettingsClass());
            if (convertedSettings != null) {
                settings = convertedSettings;
            }
        }

        settingsBinder.saveSettings(table, new SettingsWrapperImpl(settings));
        presentations.setSettings(current, SettingsHelper.toSettingsString(settings));
    }
}
