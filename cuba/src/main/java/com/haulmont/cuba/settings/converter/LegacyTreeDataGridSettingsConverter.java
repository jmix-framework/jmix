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

package com.haulmont.cuba.settings.converter;

import io.jmix.ui.settings.component.DataGridSettings;
import io.jmix.ui.settings.component.TreeDataGridSettings;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class LegacyTreeDataGridSettingsConverter extends LegacyDataGridSettingsConverter {

    @Override
    public DataGridSettings convertToComponentSettings(Element settings) {
        TreeDataGridSettings treeDataGridSettings = (TreeDataGridSettings) super.convertToComponentSettings(settings);

        String hierarchyColumn = settings.attributeValue("hierarchyColumn");
        if (StringUtils.isNotEmpty(hierarchyColumn)) {
            treeDataGridSettings.setHierarchyColumn(hierarchyColumn);
        }

        return treeDataGridSettings;
    }

    @Override
    protected void copySettingsToElement(DataGridSettings settings, Element element) {
        super.copySettingsToElement(settings, element);

        TreeDataGridSettings treeDataGridSettings = (TreeDataGridSettings) settings;
        if (treeDataGridSettings.getHierarchyColumn() != null) {
            element.addAttribute("hierarchyColumn", treeDataGridSettings.getHierarchyColumn());
        }
    }

    @Override
    protected DataGridSettings createDataGridSettings() {
        return new TreeDataGridSettings();
    }
}
