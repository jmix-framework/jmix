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
import io.jmix.pivottableflowui.kit.component.model.*;

@StudioUiKit
public interface StudioPivotTableElements {

    @StudioElement(
            name = "Aggregation",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.Aggregation",
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
                    @StudioProperty(xmlAttribute = "custom", type = StudioPropertyType.BOOLEAN)
            }
    )
    Aggregation aggregation();

    @StudioElement(
            name = "RendererOptions",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.RendererOptions",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "rendererOptions",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    RendererOptions rendererOptions();

    @StudioElement(
            name = "C3RendererOptions",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.C3RendererOptions",
            target = {"io.jmix.pivottableflowui.kit.component.model.RendererOptions"},
            xmlElement = "c3",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg"
    )
    C3RendererOptions c3();

    @StudioElement(
            name = "Size",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.Size",
            target = {"io.jmix.pivottableflowui.kit.component.model.C3RendererOptions"},
            xmlElement = "size",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.DOUBLE),
            }
    )
    Size size();

    @StudioElement(
            name = "DerivedProperty",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableDerivedProperty",
            xmlElement = "derivedProperty",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "caption", type = StudioPropertyType.LOCALIZED_STRING, required = true)
            }
    )
    void derivedProperty();

    @StudioElement(
            name = "HeatmapRendererOptions",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.HeatmapRendererOptions",
            target = {"io.jmix.pivottableflowui.kit.component.model.RendererOptions"},
            xmlElement = "heatmap",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg"
    )
    HeatmapRendererOptions heatmap();

    @StudioElement(
            name = "Row",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableRow",
            xmlElement = "row",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/row.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.LOCALIZED_STRING, required = true)
            }
    )
    void row();

    @StudioElement(
            name = "Column",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableColumn",
            xmlElement = "column",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/column.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.LOCALIZED_STRING, required = true)
            }
    )
    void column();

    @StudioElement(
            name = "Property",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableProperty",
            xmlElement = "property",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "localizedName", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void property();

    @StudioElement(
            name = "NamedProperty",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedProperty",
            target = {"io.jmix.pivottableflowui.kit.component.model.Aggregation"},
            xmlElement = "property",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.LOCALIZED_STRING, required = true)
            }
    )
    void namedProperty();

    @StudioElement(
            name = "NamedPropertyValue",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedPropertyValue",
            xmlElement = "value",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING, required = true)
            }
    )
    void namedPropertyValue();

    @StudioElement(
            name = "Inclusions",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedPropertiesWithValues",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "inclusions",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    void inclusions();

    @StudioElement(
            name = "Exclusions",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedPropertiesWithValues",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "exclusions",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    void exclusions();

    @StudioElement(
            name = "FilterFunction",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.JsFunction",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "filterFunction",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/function.svg"
    )
    void filterFunction();

    @StudioElement(
            name = "SortersFunction",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.JsFunction",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = "sortersFunction",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/function.svg"
    )
    void sortersFunction();

    @StudioElement(
            name = "ColorScaleGeneratorFunction",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.JsFunction",
            target = {"io.jmix.pivottableflowui.kit.component.model.HeatmapRendererOptions"},
            xmlElement = "colorScaleGeneratorFunction",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/function.svg"
    )
    void colorScaleGeneratorFunction();

    @StudioElement(
            name = "Function",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.JsFunction",
            target = {"io.jmix.pivottableflowui.kit.meta.StudioPivotTableDerivedProperty",
                    "io.jmix.pivottableflowui.kit.component.model.Aggregation"},
            xmlElement = "function",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/function.svg"
    )
    void function();

    @StudioElement(
            name = "Renderer",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableRenderer",
            target = {"io.jmix.pivottableflowui.kit.component.model.Renderers"},
            xmlElement = "renderer",
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg"
    )
    void renderer();


}
