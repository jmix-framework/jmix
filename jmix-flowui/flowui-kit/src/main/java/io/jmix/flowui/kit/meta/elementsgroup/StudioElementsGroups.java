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
            name = "Columns",
            elementClassFqn = "com.vaadin.flow.component.grid.Grid.Column",
            xmlElement = "columns",
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/columns.svg",
            target = {"com.vaadin.flow.component.grid.Grid"},
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#columns",
            properties = {
                    @StudioProperty(xmlAttribute = "exclude", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "sortable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "resizable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "includeAll", type = StudioPropertyType.BOOLEAN, defaultValue = "false")
            }
    )
    void columns();

    @StudioElementsGroup(
            name = "Formatter",
            elementClassFqn = "io.jmix.flowui.kit.component.formatter.Formatter",
            xmlElement = "formatter",
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/formatters.svg",
            target = {"io.jmix.flowui.kit.component.SupportsFormatter"},
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/formatter.html"
    )
    void formatter();

    @StudioElementsGroup(
            name = "Items",
            elementClassFqn = "io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem",
            xmlElement = "items",
            target = {"io.jmix.flowui.kit.component.dropdownbutton.DropdownButton",
                    "io.jmix.flowui.kit.component.combobutton.ComboButton"},
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#_elements"
    )
    void items();

    @StudioElementsGroup(
            name = "ResponsiveSteps",
            elementClassFqn = "com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep",
            xmlElement = "responsiveSteps",
            target = {"com.vaadin.flow.component.formlayout.FormLayout"},
            documentationLink = "%VERSION%/flow-ui/vc/layouts/formLayout.html#columns"
    )
    void formLayoutResponsiveSteps();

    @StudioElementsGroup(
            name = "ResponsiveSteps",
            elementClassFqn = "io.jmix.flowui.component.SupportsResponsiveSteps.ResponsiveStep",
            xmlElement = "responsiveSteps",
            target = {"io.jmix.flowui.component.SupportsResponsiveSteps"}
    )
    void responsiveSteps();

    @StudioElementsGroup(
            name = "Validators",
            elementClassFqn = "io.jmix.flowui.component.validation.Validator",
            xmlElement = "validators",
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/validators.svg",
            target = {"io.jmix.flowui.component.SupportsValidation"},
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html",
            unsupportedTarget = {
                    "io.jmix.flowui.kit.component.upload.AbstractSingleUploadField",
                    "io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup",
                    "io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup",
                    "io.jmix.flowui.component.select.JmixSelect"
            }
    )
    void validator();

    @StudioElementsGroup(
            name = "Configurations",
            elementClassFqn = "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration",
            xmlElement = "configurations",
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/configurations.svg",
            target = {"io.jmix.flowui.component.genericfilter.GenericFilter"}
    )
    void configurations();

    @StudioElementsGroup(
            name = "Conditions",
            elementClassFqn = "io.jmix.flowui.component.filter.FilterComponent",
            xmlElement = "conditions",
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/conditions.svg",
            target = {"io.jmix.flowui.component.genericfilter.GenericFilter"}
    )
    void conditions();
}
