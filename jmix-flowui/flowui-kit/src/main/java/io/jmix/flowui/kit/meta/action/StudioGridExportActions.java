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
