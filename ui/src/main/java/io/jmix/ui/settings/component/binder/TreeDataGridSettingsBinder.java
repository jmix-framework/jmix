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

import com.google.common.base.Strings;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.DataGrid.Column;
import io.jmix.ui.component.TreeDataGrid;
import io.jmix.ui.component.impl.WebTreeDataGrid;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.DataGridSettings;
import io.jmix.ui.settings.component.SettingsWrapper;
import io.jmix.ui.settings.component.TreeDataGridSettings;

@SuppressWarnings("rawtypes")
@org.springframework.stereotype.Component(TreeDataGridSettingsBinder.NAME)
public class TreeDataGridSettingsBinder extends AbstractDataGridSettingsBinder {

    public static final String NAME = "ui_TreeDataGridSettingsBinder";

    @Override
    public Class<? extends Component> getComponentClass() {
        return WebTreeDataGrid.class;
    }

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return TreeDataGridSettings.class;
    }

    @Override
    public void applySettings(DataGrid component, SettingsWrapper wrapper) {
        super.applySettings(component, wrapper);

        TreeDataGrid dataGrid = (TreeDataGrid) component;
        TreeDataGridSettings settings = wrapper.getSettings();

        String hierarchyColumn = settings.getHierarchyColumn();
        if (!Strings.isNullOrEmpty(hierarchyColumn)
                && dataGrid.getColumn(hierarchyColumn) != null) {
            dataGrid.setHierarchyColumn(hierarchyColumn);
        }
    }

    @Override
    public boolean saveSettings(DataGrid component, SettingsWrapper wrapper) {
        boolean settingsChanged = super.saveSettings(component, wrapper);

        TreeDataGrid dataGrid = (TreeDataGrid) component;
        TreeDataGridSettings settings = wrapper.getSettings();

        Column hierarchyColumn = dataGrid.getHierarchyColumn();
        if (hierarchyColumn != null
                && !hierarchyColumn.getId().equals(settings.getHierarchyColumn())) {
            settings.setHierarchyColumn(hierarchyColumn.getId());
            settingsChanged = true;
        }

        return settingsChanged;
    }

    @Override
    public TreeDataGridSettings getSettings(DataGrid component) {
        TreeDataGridSettings settings = (TreeDataGridSettings) super.getSettings(component);
        TreeDataGrid dataGrid = (TreeDataGrid) component;

        Column hierarchyColumn = dataGrid.getHierarchyColumn();
        if (hierarchyColumn != null) {
            settings.setHierarchyColumn(hierarchyColumn.getId());
        }

        return settings;
    }

    @Override
    protected DataGridSettings createSettings() {
        return new TreeDataGridSettings();
    }
}
