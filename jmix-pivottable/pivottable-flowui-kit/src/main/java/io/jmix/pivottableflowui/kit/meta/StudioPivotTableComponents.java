/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.kit.meta;

import io.jmix.flowui.kit.meta.*;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;

@StudioUiKit(studioClassloaderDependencies = "io.jmix.pivottable:jmix-pivottable-flowui-kit")
public interface StudioPivotTableComponents {

    @StudioComponent(
            name = "PivotTable",
            classFqn = "io.jmix.pivottableflowui.component.PivotTable",
            category = "Components",
            xmlElement = "pivotTable",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/component/pivotTable.svg",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO", category = StudioProperty.Category.POSITION,
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "autoSortUnusedProperties", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.POSITION),
                    @StudioProperty(xmlAttribute = "columnOrder", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.pivottableflowui.kit.component.model.Order",
                            options = {"KEYS_ASCENDING", "VALUES_ASCENDING", "VALUES_DESCENDING"}),
                    @StudioProperty(xmlAttribute = "css", type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "emptyDataMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true",
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "menuLimit", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "renderer", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.pivottableflowui.kit.component.model.Renderer",
                            options = {"TABLE", "TABLE_BAR_CHART", "HEATMAP", "ROW_HEATMAP", "COL_HEATMAP",
                                    "LINE_CHART", "BAR_CHART", "STACKED_BAR_CHART", "HORIZONTAL_BAR_CHART",
                                    "HORIZONTAL_STACKED_BAR_CHART", "AREA_CHART", "SCATTER_CHART", "TREEMAP",
                                    "TSV_EXPORT"}),
                    @StudioProperty(xmlAttribute = "rowOrder", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.pivottableflowui.kit.component.model.Order",
                            options = {"KEYS_ASCENDING", "VALUES_ASCENDING", "VALUES_DESCENDING"}),
                    @StudioProperty(xmlAttribute = "showColumnTotals", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "showRowTotals", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "showUI", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "unusedPropertiesVertical", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE)
            }
    )
    JmixPivotTable pivotTable();
}