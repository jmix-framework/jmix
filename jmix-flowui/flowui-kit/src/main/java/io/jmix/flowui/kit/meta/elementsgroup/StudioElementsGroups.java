/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.kit.meta.elementsgroup;

import io.jmix.flowui.kit.meta.StudioElementsGroup;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioElementsGroups {

    @StudioElementsGroup(
            name = "Formatter",
            elementClassFqn = "io.jmix.flowui.kit.component.formatter.Formatter",
            xmlElement = "formatter",
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/formatters.svg",
            target = {"io.jmix.flowui.kit.component.SupportsFormatter"}
    )
    void formatter();

    @StudioElementsGroup(
            name = "Columns",
            elementClassFqn = "com.vaadin.flow.component.grid.Grid.Column",
            xmlElement = "columns",
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/columns.svg",
            target = {"com.vaadin.flow.component.grid.Grid"},
            properties = {
                    @StudioProperty(xmlAttribute = "exclude", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "includeAll", type = StudioPropertyType.BOOLEAN, defaultValue = "false")
            }
    )
    void columns();

    @StudioElementsGroup(
            name = "ResponsiveSteps",
            elementClassFqn = "com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep",
            xmlElement = "responsiveSteps",
            target = {"com.vaadin.flow.component.formlayout.FormLayout"}
    )
    void responsiveSteps();

    @StudioElementsGroup(
            name = "Items",
            elementClassFqn = "io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem",
            xmlElement = "items",
            target = {"io.jmix.flowui.kit.component.dropdownbutton.DropdownButton",
                    "io.jmix.flowui.kit.component.combobutton.ComboButton"}
    )
    void items();
}
