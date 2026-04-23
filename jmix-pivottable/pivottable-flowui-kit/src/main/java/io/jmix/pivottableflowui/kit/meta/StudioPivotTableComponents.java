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
            xmlElement = StudioXmlElements.PIVOT_TABLE,
            icon = "io/jmix/pivottableflowui/kit/meta/icon/component/pivotTable.svg",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            propertyGroups = StudioPivotTablePropertyGroups.PivotTableComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO", category = StudioProperty.Category.POSITION,
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.AUTO_SORT_UNUSED_PROPERTIES, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.POSITION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLUMN_ORDER, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.pivottableflowui.kit.component.model.Order",
                            options = {"KEYS_ASCENDING", "VALUES_ASCENDING", "VALUES_DESCENDING"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EMPTY_DATA_MESSAGE, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, type = StudioPropertyType.BOOLEAN, defaultValue = "true",
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MENU_LIMIT, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RENDERER, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.pivottableflowui.kit.component.model.Renderer",
                            options = {"TABLE", "TABLE_BAR_CHART", "HEATMAP", "ROW_HEATMAP", "COL_HEATMAP",
                                    "LINE_CHART", "BAR_CHART", "STACKED_BAR_CHART", "HORIZONTAL_BAR_CHART",
                                    "HORIZONTAL_STACKED_BAR_CHART", "AREA_CHART", "SCATTER_CHART", "TREEMAP",
                                    "TSV_EXPORT"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ROW_ORDER, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.pivottableflowui.kit.component.model.Order",
                            options = {"KEYS_ASCENDING", "VALUES_ASCENDING", "VALUES_DESCENDING"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHOW_COLUMN_TOTALS, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHOW_ROW_TOTALS, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHOW_UI, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.UNUSED_PROPERTIES_VERTICAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE)
            }
    )
    JmixPivotTable pivotTable();
}