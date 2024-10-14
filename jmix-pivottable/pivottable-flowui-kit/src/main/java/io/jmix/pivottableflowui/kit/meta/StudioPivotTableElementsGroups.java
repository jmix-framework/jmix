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
import io.jmix.pivottableflowui.kit.component.model.Aggregation;

@StudioUiKit
public interface StudioPivotTableElementsGroups {

    @StudioElementsGroup(
            name = "Aggregation",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedProperty",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "aggregation",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "mode", type = StudioPropertyType.ENUMERATION,
                            options = {"COUNT","COUNT_UNIQUE_VALUES", "LIST_UNIQUE_VALUES", "SUM", "INTEGER_SUM",
                                    "AVERAGE", "MINIMUM", "MAXIMUM", "SUM_OVER_SUM", "UPPER_BOUND_80",
                                    "LOWER_BOUND_80", "SUM_AS_FRACTION_OF_TOTAL", "SUM_AS_FRACTION_OF_ROWS",
                                    "SUM_AS_FRACTION_OF_COLUMNS", "COUNT_AS_FRACTION_OF_TOTAL",
                                    "COUNT_AS_FRACTION_OF_ROWS", "COUNT_AS_FRACTION_OF_COLUMNS"}),
                    @StudioProperty(xmlAttribute = "caption", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "custom", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "function", type = StudioPropertyType.STRING)
            }
    )
    Aggregation aggregation();

    @StudioElementsGroup(
            name = "Aggregations",
            elementClassFqn = "io.jmix.pivottableflowui.kit.component.model.Aggregation",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "aggregations",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/aggregations.svg"
    )
    void aggregations();

    @StudioElementsGroup(
            name = "Renderers",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableRenderer",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "renderers",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/renderers.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "defaultRenderer", type = StudioPropertyType.ENUMERATION,
                            options = {"TABLE", "TABLE_BAR_CHART", "HEATMAP", "ROW_HEATMAP", "COL_HEATMAP",
                                    "LINE_CHART", "BAR_CHART", "STACKED_BAR_CHART", "HORIZONTAL_BAR_CHART",
                                    "HORIZONTAL_STACKED_BAR_CHART", "AREA_CHART", "SCATTER_CHART", "TREEMAP",
                                    "TSV_EXPORT"
                    }),
                    @StudioProperty(xmlAttribute = "selectedRenderer", type = StudioPropertyType.ENUMERATION,
                            options = {"TABLE", "TABLE_BAR_CHART", "HEATMAP", "ROW_HEATMAP", "COL_HEATMAP",
                                    "LINE_CHART", "BAR_CHART", "STACKED_BAR_CHART", "HORIZONTAL_BAR_CHART",
                                    "HORIZONTAL_STACKED_BAR_CHART", "AREA_CHART", "SCATTER_CHART", "TREEMAP",
                                    "TSV_EXPORT"
                            })
            }
    )
    void renderers();

    @StudioElementsGroup(
            name = "DerivedProperties",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableDerivedProperty",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "derivedProperties",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    void derivedProperties();

    @StudioElementsGroup(
            name = "Rows",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableRow",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "rows",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/rows.svg"
    )
    void rows();

    @StudioElementsGroup(
            name = "Columns",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableColumn",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "columns",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/columns.svg"
    )
    void columns();

    @StudioElementsGroup(
            name = "Properties",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableProperty",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "properties",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    void properties();

    @StudioElementsGroup(
            name = "AggregationProperties",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedProperty",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "aggregationProperties",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    void aggregationProperties();

    @StudioElementsGroup(
            name = "HiddenProperties",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedProperty",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "hiddenProperties",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    void hiddenProperties();

    @StudioElementsGroup(
            name = "HiddenFromAggregations",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedProperty",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "hiddenFromAggregations",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    void hiddenFromAggregations();

    @StudioElementsGroup(
            name = "HiddenFromDragDrop",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedProperty",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "hiddenFromDragDrop",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    void hiddenFromDragDrop();

    @StudioElementsGroup(
            name = "NamedPropertyWithValues",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedPropertyValue",
            xmlElement = "property",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/properties.svg"
    )
    void propertyWithValues();

    @StudioElementsGroup(
            name = "Inclusions",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedPropertyWithValues",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "inclusions",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    void inclusions();

    @StudioElementsGroup(
            name = "Exclusions",
            elementClassFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedPropertyWithValues",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "exclusions",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    void exclusions();
}
