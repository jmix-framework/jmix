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

import io.jmix.flowui.kit.meta.StudioElementsGroup;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioPivotTableElementsGroups {

    @StudioElementsGroup(
            name = "Aggregations",
            elementClassFqn = "io.jmix.pivottableflowui.kit.component.model.Aggregation",
            target = {"io.jmix.pivottableflowui.kit.component.model.Aggregations"},
            xmlElement = "aggregations",
            xmlns = "http://jmix.io/schema/pivot-table/ui",
            xmlnsAlias = "pivot",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg"
    )
    void aggregations();

    @StudioElementsGroup(
            name = "Renderers",
            elementClassFqn = "io.jmix.pivottableflowui.kit.component.model.Renderer",
            target = {"io.jmix.pivottableflowui.kit.component.model.Renderers"},
            xmlElement = "renderers",
            xmlns = "http://jmix.io/schema/pivot-table/ui",
            xmlnsAlias = "pivot",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg"
    )
    void renderers();

    @StudioElementsGroup(
            name = "DerivedProperties",

            target = {"io.jmix.pivottableflowui.kit.component.model.DerivedProperties"},
            xmlElement = "derivedProperties",
            xmlns = "http://jmix.io/schema/pivot-table/ui",
            xmlnsAlias = "pivot",
            icon = "io/jmix/pivottableflowui/kit/meta/icon/unknownComponent.svg"
    )
    void derivedProperties();
}
