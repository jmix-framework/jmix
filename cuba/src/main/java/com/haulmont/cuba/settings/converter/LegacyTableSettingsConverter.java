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

import com.haulmont.cuba.settings.component.CubaTableSettings;
import com.haulmont.cuba.settings.component.HasSettingsPresentation;
import io.jmix.core.UuidProvider;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.TableSettings;
import io.jmix.ui.settings.component.TableSettings.ColumnSettings;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LegacyTableSettingsConverter implements LegacySettingsConverter {

    @Override
    public void copyToElement(ComponentSettings settings, Element element) {
        element.attributes().clear();
        element.clearContent();

        copySettingsToElement((TableSettings) settings, element);
    }

    @Override
    public Element convertToElement(ComponentSettings settings) {
        Element element = DocumentHelper.createElement("component");

        copySettingsToElement((TableSettings) settings, element);

        return element;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TableSettings convertToComponentSettings(Element settings) {
        TableSettings tableSettings = createSettings();
        tableSettings.setId(settings.attributeValue("name"));

        String textSelection = settings.attributeValue("textSelection");
        if (StringUtils.isNotBlank(textSelection)) {
            tableSettings.setTextSelection(Boolean.parseBoolean(textSelection));
        }

        String presentationId = settings.attributeValue("presentation");
        if (StringUtils.isNotBlank(presentationId)
                && tableSettings instanceof HasSettingsPresentation) {
            ((HasSettingsPresentation) tableSettings).setPresentationId(UuidProvider.fromString(presentationId));
        }

        Element columnsElem = settings.element("columns");
        if (columnsElem != null) {
            String sortProperty = columnsElem.attributeValue("sortProperty");
            if (StringUtils.isNotBlank(sortProperty)) {
                tableSettings.setSortProperty(sortProperty);

                String sortAscending = columnsElem.attributeValue("sortAscending");
                tableSettings.setSortAscending(Boolean.parseBoolean(sortAscending));
            }

            List<Element> columns = columnsElem.elements("columns");
            List<ColumnSettings> columnsSett = new ArrayList<>(columns.size());
            for (Element column : columns) {
                ColumnSettings columnSett = new ColumnSettings();
                columnSett.setId(column.attributeValue("id"));

                String width = column.attributeValue("width");
                if (StringUtils.isNotBlank(width)) {
                    columnSett.setWidth(Integer.parseInt(width));
                }

                String visible = column.attributeValue("visible");
                if (StringUtils.isNotBlank(visible)) {
                    columnSett.setVisible(Boolean.parseBoolean(visible));
                }

                columnsSett.add(columnSett);
            }

            tableSettings.setColumns(columnsSett);
        }

        return tableSettings;
    }


    protected void copySettingsToElement(TableSettings tableSettings, Element element) {
        element.addAttribute("name", tableSettings.getId());

        Boolean textSelection = tableSettings.getTextSelection();
        if (textSelection != null)
            element.addAttribute("textSelection", textSelection.toString());

        if (tableSettings instanceof HasSettingsPresentation) {
            UUID presentationId = ((HasSettingsPresentation) tableSettings).getPresentationId();
            if (presentationId != null) {
                element.addAttribute("presentation", presentationId.toString());
            }
        }

        List<ColumnSettings> columns = tableSettings.getColumns();
        if (columns != null) {
            Element columnsElem = element.addElement("columns");

            if (tableSettings.getSortProperty() != null) {
                columnsElem.addAttribute("sortProperty", tableSettings.getSortProperty());
                columnsElem.addAttribute("sortAscending", tableSettings.getSortAscending().toString());
            }

            for (ColumnSettings column : columns) {
                Element columnElem = columnsElem.addElement("columns");
                columnElem.addAttribute("id", column.getId());

                if (column.getWidth() != null) {
                    columnElem.addAttribute("width", column.getWidth().toString());
                }

                if (column.getVisible() != null) {
                    columnElem.addAttribute("visible", column.getVisible().toString());
                }
            }
        }
    }

    protected TableSettings createSettings() {
        return new CubaTableSettings();
    }
}
