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

import io.jmix.ui.component.DataGrid;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.DataGridSettings;
import io.jmix.ui.settings.component.DataGridSettings.ColumnSettings;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class LegacyDataGridSettingsConverter implements LegacySettingsConverter {

    @Override
    public Element convertToElement(ComponentSettings settings) {
        Element element = DocumentHelper.createElement("component");

        copySettingsToElement((DataGridSettings) settings, element);

        return element;
    }

    @Override
    public void copyToElement(ComponentSettings settings, Element element) {
        element.attributes().clear();
        element.clearContent();

        copySettingsToElement((DataGridSettings) settings, element);
    }

    protected void copySettingsToElement(DataGridSettings settings, Element element) {
        element.addAttribute("name", settings.getId());

        if (settings.getColumns() != null) {
            Element columnsElem = element.addElement("columns");

            if (settings.getSortColumnId() != null) {
                columnsElem.addAttribute("sortColumnId", settings.getSortColumnId());
                columnsElem.addAttribute("sortDirection", settings.getSortDirection().name());
            }

            for (ColumnSettings columnSettings : settings.getColumns()) {
                Element column = columnsElem.addElement("columns");
                column.addAttribute("id", columnSettings.getId());

                if (columnSettings.getWidth() != null) {
                    column.addAttribute("width", columnSettings.getWidth().toString());
                }

                if (columnSettings.getCollapsed() != null) {
                    column.addAttribute("collapsed", columnSettings.getCollapsed().toString());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataGridSettings convertToComponentSettings(Element settings) {
        DataGridSettings dataGridSettings = createDataGridSettings();
        dataGridSettings.setId(settings.attributeValue("name"));

        Element columnElem = settings.element("columns");
        if (columnElem != null) {
            String sortColumnId = columnElem.attributeValue("sortColumnId");
            if (StringUtils.isNotBlank(sortColumnId)) {
                dataGridSettings.setSortColumnId(sortColumnId);
            }

            String sortDirection = columnElem.attributeValue("sortDirection");
            if (StringUtils.isNotBlank(sortDirection)) {
                dataGridSettings.setSortDirection(DataGrid.SortDirection.valueOf(sortDirection));
            }

            List<Element> columns = columnElem.elements("columns");
            if (!columns.isEmpty()) {
                List<ColumnSettings> columnsSettings = new ArrayList<>(columns.size());

                for (Element column : columns) {
                    ColumnSettings columnSettings = new ColumnSettings();
                    columnSettings.setId(column.attributeValue("id"));

                    String width = column.attributeValue("width");
                    if (StringUtils.isNotBlank(width)) {
                        columnSettings.setWidth(Double.parseDouble(width));
                    }

                    String collapsed = column.attributeValue("collapsed");
                    if (StringUtils.isNotBlank(collapsed)) {
                        columnSettings.setCollapsed(Boolean.parseBoolean(collapsed));
                    }

                    columnsSettings.add(columnSettings);
                }

                dataGridSettings.setColumns(columnsSettings);
            }
        }

        return dataGridSettings;
    }

    protected DataGridSettings createDataGridSettings() {
        return new DataGridSettings();
    }
}
