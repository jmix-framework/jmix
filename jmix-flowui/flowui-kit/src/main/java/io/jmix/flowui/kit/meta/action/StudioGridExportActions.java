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

@StudioUiKit(requiredDependencies = "io.jmix.gridexport:jmix-gridexport-flowui-starter")
public interface StudioGridExportActions {

    @StudioAction(
            type = "grdexp_excelExport",
            description = "Action for export table content in XLSX format",
            classFqn = "io.jmix.gridexportflowui.action.ExcelExportAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            target = {"io.jmix.flowui.component.ListDataComponent"},
            documentationLink = "%VERSION%/grid-export/index.html",
            availableInViewWizard = true,
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "excelExport"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "availableExportModes", type = StudioPropertyType.VALUES_LIST,
                            options = {"ALL_ROWS", "CURRENT_PAGE", "SELECTED_ROWS"}),
                    @StudioPropertiesItem(xmlAttribute = "columnsToExport", type = StudioPropertyType.ENUMERATION,
                            options = {"ALL_COLUMNS", "VISIBLE_COLUMNS"}),
                    @StudioPropertiesItem(xmlAttribute = "columnKeysToExport", type = StudioPropertyType.VALUES_LIST)
            }
    )
    void excelExport();

    @StudioAction(
            type = "grdexp_jsonExport",
            description = "Action for export table content as JSON",
            classFqn = "io.jmix.gridexportflowui.action.JsonExportAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            target = {"io.jmix.flowui.component.ListDataComponent"},
            documentationLink = "%VERSION%/grid-export/index.html",
            availableInViewWizard = true,
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_ID, required = true, initialValue = "jsonExport"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            },
            items = {
                    @StudioPropertiesItem(xmlAttribute = "availableExportModes", type = StudioPropertyType.VALUES_LIST,
                            options = {"ALL_ROWS", "CURRENT_PAGE", "SELECTED_ROWS"}),
                    @StudioPropertiesItem(xmlAttribute = "columnsToExport", type = StudioPropertyType.ENUMERATION,
                            options = {"ALL_COLUMNS", "VISIBLE_COLUMNS"}),
                    @StudioPropertiesItem(xmlAttribute = "columnKeysToExport", type = StudioPropertyType.VALUES_LIST)
            }
    )
    void jsonExport();
}
