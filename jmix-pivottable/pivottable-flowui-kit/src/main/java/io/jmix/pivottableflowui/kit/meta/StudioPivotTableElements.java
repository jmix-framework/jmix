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

import io.jmix.flowui.kit.meta.StudioElement;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.pivottableflowui.kit.component.model.Aggregation;
import io.jmix.pivottableflowui.kit.component.model.C3RendererOptions;
import io.jmix.pivottableflowui.kit.component.model.HeatmapRendererOptions;
import io.jmix.pivottableflowui.kit.component.model.Size;

public interface StudioPivotTableElements {

    @StudioElement(
            name = "Aggregation",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.Aggregation",
            target = {"io.jmix.pivottableflowui.kit.component.model.Aggregations"},
            xmlElement = "aggregation",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                @StudioProperty(xmlAttribute = "mode", type = StudioPropertyType.ENUMERATION,
                                options = {"COUNT","COUNT_UNIQUE_VALUES", "LIST_UNIQUE_VALUES", "SUM", "INTEGER_SUM",
                                        "AVERAGE", "MINIMUM", "MAXIMUM", "SUM_OVER_SUM", "UPPER_BOUND_80",
                                        "LOWER_BOUND_80", "SUM_AS_FRACTION_OF_TOTAL", "SUM_AS_FRACTION_OF_ROWS",
                                        "SUM_AS_FRACTION_OF_COLUMNS", "COUNT_AS_FRACTION_OF_TOTAL",
                                        "COUNT_AS_FRACTION_OF_ROWS", "COUNT_AS_FRACTION_OF_COLUMNS"}),
                @StudioProperty(xmlAttribute = "caption", type = StudioPropertyType.STRING),
                @StudioProperty(xmlAttribute = "custom", type = StudioPropertyType.BOOLEAN),
                @StudioProperty(xmlAttribute = "function", type = StudioPropertyType.STRING),
                @StudioProperty(xmlAttribute = "properties", type = StudioPropertyType.VALUES_LIST)
            }
    )
    Aggregation aggregation();

    @StudioElement(
            name = "C3RendererOptions",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.C3RendererOptions",
            target = {"io.jmix.pivottableflowui.kit.component.model.RendererOptions"},
            xmlElement = "c3",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg"
    )
    C3RendererOptions c3();

    @StudioElement(
            name = "Size",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.Size",
            target = {"io.jmix.pivottableflowui.kit.component.model.C3RendererOptions"},
            xmlElement = "size",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.DOUBLE),
            }
    )
    Size size();

    @StudioElement(
            name = "DerivedProperty",
            target = {"io.jmix.pivottableflowui.kit.component.model.meta.DerivedProperties"},
            xmlElement = "derivedProperty",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "caption", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "function", type = StudioPropertyType.STRING),
            }
    )
    DerivedProperty derivedProperty();

    @StudioElement(
            name = "Renderer",
            target = {"io.jmix.pivottableflowui.kit.component.model.meta.Renderers"},
            xmlElement = "renderer",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.ENUMERATION,
                            options = {"TABLE", "TABLE_BAR_CHART", "HEATMAP", "ROW_HEATMAP", "COL_HEATMAP",
                                    "LINE_CHART", "BAR_CHART", "STACKED_BAR_CHART", "HORIZONTAL_BAR_CHART",
                                    "HORIZONTAL_STACKED_BAR_CHART", "AREA_CHART", "SCATTER_CHART", "TREEMAP",
                                    "TSV_EXPORT"
                            })

            }
    )
    Renderer renderer();

    @StudioElement(
            name = "HeatmapRendererOptions",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.HeatmapRendererOptions",
            target = {"io.jmix.pivottableflowui.kit.component.model.RendererOptions"},
            xmlElement = "heatmap",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "colorScaleGeneratorFunction", type = StudioPropertyType.STRING)
            }
    )
    HeatmapRendererOptions heatmap();

    @StudioElement(
            name = "Row",
            classFqn = "io.jmix.pivottableflowui.kit.meta.Row",
            xmlElement = "row",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    Row row();

    @StudioElement(
            name = "Col",
            classFqn = "io.jmix.pivottableflowui.kit.meta.Col",
            xmlElement = "col",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    Column col();

    @StudioElement(
            name = "Property",
            classFqn = "io.jmix.pivottableflowui.kit.meta.Property",
            xmlElement = "property",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "localizedName", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    Property property();

    @StudioElement(
            name = "NamedProperty",
            classFqn = "io.jmix.pivottableflowui.kit.meta.NamedProperty",
            xmlElement = "property",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    NamedProperty namedProperty();

    @StudioElement(
            name = "NamedPropertyValue",
            classFqn = "io.jmix.pivottableflowui.kit.meta.NamedPropertyValue",
            target = "io.jmix.pivottableflowui.kit.meta.NamedPropertyWithValues",
            xmlElement = "value",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING)
            }
    )
    NamedPropertyValue namedPropertyValue();
}
