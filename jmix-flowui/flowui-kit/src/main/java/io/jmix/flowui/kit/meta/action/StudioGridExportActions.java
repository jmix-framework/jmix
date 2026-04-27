/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.kit.meta.action;

import io.jmix.flowui.kit.meta.StudioAction;
import io.jmix.flowui.kit.meta.StudioPropertiesItem;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;

@StudioUiKit(requiredDependencies = "io.jmix.gridexport:jmix-gridexport-flowui-starter")
interface StudioGridExportActions {

    @StudioAction(
            type = "grdexp_excelExport",
            description = "Action for export table content in XLSX format",
            classFqn = "io.jmix.gridexportflowui.action.ExcelExportAction",
            target = {"io.jmix.flowui.component.ListDataComponent"},
            documentationLink = "%VERSION%/grid-export/index.html",
            availableInViewWizard = true,
            propertyGroups = StudioActionPropertyGroups.ExcelExportComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon", defaultValue = "FILE_TABLE"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "excelExport"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///excelExporter.label"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.AVAILABLE_EXPORT_MODES, type = StudioPropertyType.VALUES_LIST,
                            options = {"ALL_ROWS", "CURRENT_PAGE", "SELECTED_ROWS"}),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.COLUMNS_TO_EXPORT, type = StudioPropertyType.ENUMERATION,
                            options = {"ALL_COLUMNS", "VISIBLE_COLUMNS"}),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.COLUMN_KEYS_TO_EXPORT, type = StudioPropertyType.VALUES_LIST)
            }
    )
    void excelExport();

    @StudioAction(
            type = "grdexp_jsonExport",
            description = "Action for export table content as JSON",
            classFqn = "io.jmix.gridexportflowui.action.JsonExportAction",
            target = {"io.jmix.flowui.component.ListDataComponent"},
            documentationLink = "%VERSION%/grid-export/index.html",
            availableInViewWizard = true,
            propertyGroups = StudioActionPropertyGroups.JsonExportComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon", defaultValue = "FILE_CODE"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "jsonExport"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///jsonExporter.label"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.AVAILABLE_EXPORT_MODES, type = StudioPropertyType.VALUES_LIST,
                            options = {"ALL_ROWS", "CURRENT_PAGE", "SELECTED_ROWS"}),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.COLUMNS_TO_EXPORT, type = StudioPropertyType.ENUMERATION,
                            options = {"ALL_COLUMNS", "VISIBLE_COLUMNS"}),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.COLUMN_KEYS_TO_EXPORT, type = StudioPropertyType.VALUES_LIST)
            }
    )
    void jsonExport();
}
