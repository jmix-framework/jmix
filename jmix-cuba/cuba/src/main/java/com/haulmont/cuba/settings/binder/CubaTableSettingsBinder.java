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

package com.haulmont.cuba.settings.binder;

import com.haulmont.cuba.settings.component.CubaTableSettings;
import com.haulmont.cuba.web.gui.components.WebTable;
import io.jmix.core.JmixOrder;
import io.jmix.core.UuidProvider;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Table;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.GroupTableSettings;
import io.jmix.ui.settings.component.SettingsWrapper;
import io.jmix.ui.settings.component.TableSettings;
import io.jmix.ui.settings.component.binder.TableSettingsBinder;
import org.springframework.core.annotation.Order;

import java.util.Objects;
import java.util.UUID;

@Order(JmixOrder.LOWEST_PRECEDENCE - 10)
@org.springframework.stereotype.Component(CubaTableSettingsBinder.NAME)
public class CubaTableSettingsBinder extends TableSettingsBinder {

    public static final String NAME = "cuba_CubaTableSettingsBinder";

    @Override
    public Class<? extends Component> getComponentClass() {
        return WebTable.class;
    }

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return CubaTableSettings.class;
    }

    @Override
    public boolean saveSettings(Table table, SettingsWrapper wrapper) {
        boolean settingsChanged = super.saveSettings(table, wrapper);
        if (settingsChanged) {
            return true;
        }

        CubaTableSettings tableSettings = wrapper.getSettings();

        if (!Objects.equals(tableSettings.getPresentationId(), table.getDefaultPresentationId())) {
            tableSettings.setPresentationId((UUID) table.getDefaultPresentationId());

            settingsChanged = true;
        }
        return settingsChanged;
    }

    @Override
    public TableSettings getSettings(Table table) {
        CubaTableSettings settings = (CubaTableSettings) super.getSettings(table);

        Object presentationId = table.getDefaultPresentationId();
        if (presentationId != null) {
            settings.setPresentationId(UuidProvider.fromString(String.valueOf(presentationId)));
        }

        return settings;
    }

    @Override
    protected TableSettings createTableSettings() {
        return new GroupTableSettings();
    }

    @Override
    protected boolean isPresentationsEnabled(Table table) {
        if (table instanceof com.haulmont.cuba.gui.components.Table) {
            return ((com.haulmont.cuba.gui.components.Table<?>) table).isUsePresentations();
        }
        return super.isPresentationsEnabled(table);
    }
}
