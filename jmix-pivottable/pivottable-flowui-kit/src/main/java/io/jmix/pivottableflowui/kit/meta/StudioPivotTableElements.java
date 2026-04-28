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
            xmlElement = StudioXmlElements.AGGREGATION,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg",
            propertyGroups = StudioPivotTablePropertyGroups.AggregationComponent.class)
    Aggregation aggregation();

    @StudioElement(
            name = "RendererOptions",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.RendererOptions",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = StudioXmlElements.RENDERER_OPTIONS,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    RendererOptions rendererOptions();

    @StudioElement(
            name = "C3RendererOptions",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.C3RendererOptions",
            target = {"io.jmix.pivottableflowui.kit.component.model.RendererOptions"},
            xmlElement = StudioXmlElements.C3,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg"
    )
    C3RendererOptions c3();

    @StudioElement(
            name = "Size",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.Size",
            target = {"io.jmix.pivottableflowui.kit.component.model.C3RendererOptions"},
            xmlElement = StudioXmlElements.SIZE,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg",
            propertyGroups = StudioPivotTablePropertyGroups.SizeComponent.class)
    Size size();

    @StudioElement(
            name = "DerivedProperty",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableDerivedProperty",
            xmlElement = StudioXmlElements.DERIVED_PROPERTY,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg",
            propertyGroups = StudioPivotTablePropertyGroups.DerivedPropertyComponent.class)
    void derivedProperty();

    @StudioElement(
            name = "HeatmapRendererOptions",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.HeatmapRendererOptions",
            target = {"io.jmix.pivottableflowui.kit.component.model.RendererOptions"},
            xmlElement = StudioXmlElements.HEATMAP,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg"
    )
    HeatmapRendererOptions heatmap();

    @StudioElement(
            name = "Row",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableRow",
            xmlElement = StudioXmlElements.ROW,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/row.svg",
            propertyGroups = StudioPivotTablePropertyGroups.RowComponent.class)
    void row();

    @StudioElement(
            name = "Column",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableColumn",
            xmlElement = StudioXmlElements.COLUMN,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/column.svg",
            propertyGroups = StudioPivotTablePropertyGroups.ColumnComponent.class)
    void column();

    @StudioElement(
            name = "Property",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableProperty",
            xmlElement = StudioXmlElements.PROPERTY,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg",
            propertyGroups = StudioPivotTablePropertyGroups.PropertyComponent.class)
    void property();

    @StudioElement(
            name = "NamedProperty",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedProperty",
            target = {"io.jmix.pivottableflowui.kit.component.model.Aggregation"},
            xmlElement = StudioXmlElements.PROPERTY,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg",
            propertyGroups = StudioPivotTablePropertyGroups.NamedPropertyComponent.class)
    void namedProperty();

    @StudioElement(
            name = "NamedPropertyValue",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedPropertyValue",
            xmlElement = StudioXmlElements.VALUE,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg",
            propertyGroups = {
                    StudioPropertyGroups.RequiredStringValue.class
            })
    void namedPropertyValue();

    @StudioElement(
            name = "Inclusions",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedPropertiesWithValues",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = StudioXmlElements.INCLUSIONS,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    void inclusions();

    @StudioElement(
            name = "Exclusions",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableNamedPropertiesWithValues",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = StudioXmlElements.EXCLUSIONS,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/properties.svg"
    )
    void exclusions();

    @StudioElement(
            name = "FilterFunction",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.JsFunction",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = StudioXmlElements.FILTER_FUNCTION,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/function.svg"
    )
    void filterFunction();

    @StudioElement(
            name = "SortersFunction",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.JsFunction",
            target = {"io.jmix.pivottableflowui.component.PivotTable"},
            xmlElement = StudioXmlElements.SORTERS_FUNCTION,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/function.svg"
    )
    void sortersFunction();

    @StudioElement(
            name = "ColorScaleGeneratorFunction",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.JsFunction",
            target = {"io.jmix.pivottableflowui.kit.component.model.HeatmapRendererOptions"},
            xmlElement = StudioXmlElements.COLOR_SCALE_GENERATOR_FUNCTION,
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
            xmlElement = StudioXmlElements.FUNCTION,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/function.svg"
    )
    void function();

    @StudioElement(
            name = "Renderer",
            classFqn = "io.jmix.pivottableflowui.kit.meta.StudioPivotTableRenderer",
            target = {"io.jmix.pivottableflowui.kit.component.model.Renderers"},
            xmlElement = StudioXmlElements.RENDERER,
            xmlns = "http://jmix.io/schema/pvttbl/ui",
            xmlnsAlias = "pvttbl",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/element/property.svg"
    )
    void renderer();


}
