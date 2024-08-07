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

import com.vaadin.flow.dom.Element;
import io.jmix.flowui.kit.meta.StudioElement;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.pivottableflowui.kit.component.model.Aggregation;
import io.jmix.pivottableflowui.kit.component.model.C3RendererOptions;
import io.jmix.pivottableflowui.kit.component.model.Size;

public interface StudioPivotTableElements {

    @StudioElement(
            name = "Aggregation",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.Aggregation",
            target = {"io.jmix.pivottableflowui.kit.component.model.Aggregations"},
            xmlElement = "aggregation",
            xmlns = "http://jmix.io/schema/pivot/ui",
            xmlnsAlias = "pivot",
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
                @StudioProperty(xmlAttribute = "properties", type = StudioPropertyType.VALUES_LIST),
            }
    )
    Aggregation aggregation();

    @StudioElement(
            name = "C3RendererOptions",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.C3RendererOptions",
            target = {"io.jmix.pivottableflowui.kit.component.model.RendererOptions"},
            xmlElement = "c3",
            xmlns = "http://jmix.io/schema/pivot/ui",
            xmlnsAlias = "pivot",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg"
    )
    C3RendererOptions c3();

    @StudioElement(
            name = "Size",
            classFqn = "io.jmix.pivottableflowui.kit.component.model.Size",
            target = {"io.jmix.pivottableflowui.kit.component.model.C3RendererOptions"},
            xmlElement = "size",
            xmlns = "http://jmix.io/schema/pivot/ui",
            xmlnsAlias = "pivot",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.DOUBLE),
            }
    )
    Size size();

    @StudioElement(
            name = "DerivedProperty",
            target = {"io.jmix.pivottableflowui.kit.component.model.DerivedProperties"},
            xmlElement = "derivedProperty",
            xmlns = "http://jmix.io/schema/pivot/ui",
            xmlnsAlias = "pivot",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "caption", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "function", type = StudioPropertyType.STRING),
            }
    )
    Object derivedProperty();
}
