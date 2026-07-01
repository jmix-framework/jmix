/*
 * Copyright 2026 Haulmont.
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

@StudioAPI
final class StudioPivotTablePropertyGroups {

    private StudioPivotTablePropertyGroups() {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MODE,
                            type = StudioPropertyType.ENUMERATION,
                            options = {"COUNT", "COUNT_UNIQUE_VALUES", "LIST_UNIQUE_VALUES", "SUM", "INTEGER_SUM",
                                    "AVERAGE", "MINIMUM", "MAXIMUM", "SUM_OVER_SUM", "UPPER_BOUND_80", "LOWER_BOUND_80",
                                    "SUM_AS_FRACTION_OF_TOTAL", "SUM_AS_FRACTION_OF_ROWS", "SUM_AS_FRACTION_OF_COLUMNS",
                                    "COUNT_AS_FRACTION_OF_TOTAL", "COUNT_AS_FRACTION_OF_ROWS",
                                    "COUNT_AS_FRACTION_OF_COLUMNS"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CAPTION,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CUSTOM,
                            type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface AggregationComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.WIDTH,
                            type = StudioPropertyType.DOUBLE),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.HEIGHT,
                            type = StudioPropertyType.DOUBLE)
            }
    )
    public interface SizeComponent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CAPTION,
            type = StudioPropertyType.LOCALIZED_STRING,
            required = true))
    public interface DerivedPropertyComponent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE,
            type = StudioPropertyType.LOCALIZED_STRING,
            required = true))
    public interface RowComponent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.VALUE,
            type = StudioPropertyType.LOCALIZED_STRING,
            required = true))
    public interface ColumnComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.NAME,
                            type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LOCALIZED_NAME,
                            type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface PropertyComponent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NAME,
            type = StudioPropertyType.LOCALIZED_STRING,
            required = true))
    public interface NamedPropertyComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.AUTO_SORT_UNUSED_PROPERTIES,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.COLUMN_ORDER,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.pivottableflowui.kit.component.model.Order",
                            options = {"KEYS_ASCENDING", "VALUES_ASCENDING", "VALUES_DESCENDING"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATA_CONTAINER,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
                            category = StudioProperty.Category.DATA_BINDING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EMPTY_DATA_MESSAGE,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MENU_LIMIT,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.RENDERER,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.pivottableflowui.kit.component.model.Renderer",
                            options = {"TABLE", "TABLE_BAR_CHART", "HEATMAP", "ROW_HEATMAP", "COL_HEATMAP",
                                    "LINE_CHART", "BAR_CHART", "STACKED_BAR_CHART", "HORIZONTAL_BAR_CHART",
                                    "HORIZONTAL_STACKED_BAR_CHART", "AREA_CHART", "SCATTER_CHART", "TREEMAP",
                                    "TSV_EXPORT"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ROW_ORDER,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.pivottableflowui.kit.component.model.Order",
                            options = {"KEYS_ASCENDING", "VALUES_ASCENDING", "VALUES_DESCENDING"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_COLUMN_TOTALS,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_ROW_TOTALS,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SHOW_UI,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.UNUSED_PROPERTIES_VERTICAL,
                            type = StudioPropertyType.BOOLEAN)
            }
    )
    public interface PivotTableComponent extends StudioPropertyGroups.HasSizeWithoutOptions,
            StudioPropertyGroups.ClassNamesAndCss, StudioPropertyGroups.Id, StudioPropertyGroups.Enabled,
            StudioPropertyGroups.Colspan, StudioPropertyGroups.AlignSelf {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ICON,
                            type = StudioPropertyType.ICON,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "SHIELD",
                            setParameterFqn = "com.vaadin.flow.component.Component"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "showPivotTable"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg:///actions.showPivotAction.caption")
            }
    )
    public interface ShowPivotTableActionComponent extends StudioPropertyGroups.BaseActionDefaultProperties {
    }

}
