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
import io.jmix.flowui.kit.meta.StudioUiKit;

import static io.jmix.flowui.kit.meta.StudioMetaConstants.TAG_PREFIX;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
interface StudioElementsGroups {

    @StudioElementsGroup(
            name = "Columns",
            elementClassFqn = "com.vaadin.flow.component.grid.Grid.Column",
            xmlElement = StudioXmlElements.COLUMNS,
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/columns.svg",
            target = {"com.vaadin.flow.component.grid.Grid"},
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#columns",
            propertyGroups = StudioPropertyGroups.ColumnsElementGroupComponent.class)
    void columns();

    @StudioElementsGroup(
            name = "Formatter",
            elementClassFqn = "io.jmix.flowui.kit.component.formatter.Formatter",
            xmlElement = StudioXmlElements.FORMATTER,
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/formatters.svg",
            target = {"io.jmix.flowui.kit.component.SupportsFormatter"},
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/formatter.html"
    )
    void formatter();

    @StudioElementsGroup(
            name = "Items",
            elementClassFqn = "io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem",
            xmlElement = StudioXmlElements.ITEMS,
            target = {"io.jmix.flowui.kit.component.dropdownbutton.DropdownButton",
                    "io.jmix.flowui.kit.component.combobutton.ComboButton"},
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#_elements"
    )
    void items();

    @StudioElementsGroup(
            name = "Items",
            elementClassFqn = "io.jmix.flowui.kit.component.usermenu.UserMenuItem",
            xmlElement = StudioXmlElements.ITEMS,
            target = {"io.jmix.flowui.component.usermenu.UserMenu"},
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#_elements"
    )
    void userMenuItems();

    @StudioElementsGroup(
            name = "Items",
            elementClassFqn = "io.jmix.flowui.kit.component.usermenu.UserMenuItem",
            xmlElement = StudioXmlElements.ITEMS,
            target = {"io.jmix.flowui.kit.component.usermenu.UserMenuItem"},
            unsupportedTarget = {
                    "io.jmix.flowui.kit.component.stub.UserMenuStubSeparatorItem",
                    "io.jmix.flowui.kit.component.usermenu.ActionUserMenuItem",
                    "io.jmix.flowui.component.usermenu.ViewUserMenuItem"
            },
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#_elements"
    )
    void userMenuItemItems();

    @StudioElementsGroup(
            name = "ResponsiveSteps",
            elementClassFqn = "com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep",
            xmlElement = StudioXmlElements.RESPONSIVE_STEPS,
            target = {"com.vaadin.flow.component.formlayout.FormLayout"},
            documentationLink = "%VERSION%/flow-ui/vc/layouts/formLayout.html#responsive-steps"
    )
    void formLayoutResponsiveSteps();

    @StudioElementsGroup(
            name = "ResponsiveSteps",
            elementClassFqn = "io.jmix.flowui.component.SupportsResponsiveSteps.ResponsiveStep",
            xmlElement = StudioXmlElements.RESPONSIVE_STEPS,
            target = {"io.jmix.flowui.component.SupportsResponsiveSteps"}
    )
    void responsiveSteps();

    @StudioElementsGroup(
            name = "Validators",
            elementClassFqn = "io.jmix.flowui.component.validation.Validator",
            xmlElement = StudioXmlElements.VALIDATORS,
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/validators.svg",
            target = {"io.jmix.flowui.component.SupportsValidation"},
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html",
            unsupportedTarget = {
                    "io.jmix.flowui.kit.component.upload.AbstractSingleUploadField",
                    "io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup",
                    "io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup",
                    "io.jmix.flowui.component.select.JmixSelect",
                    "io.jmix.flowui.component.combobox.JmixComboBox",
                    "io.jmix.flowui.component.checkbox.JmixCheckbox",
                    "io.jmix.flowui.component.checkbox.Switch"
            }
    )
    void validator();

    @StudioElementsGroup(
            name = "Configurations",
            elementClassFqn = "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration",
            xmlElement = StudioXmlElements.CONFIGURATIONS,
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/configurations.svg",
            target = {"io.jmix.flowui.component.genericfilter.GenericFilter"}
    )
    void configurations();

    @StudioElementsGroup(
            name = "Conditions",
            elementClassFqn = "io.jmix.flowui.component.filter.FilterComponent",
            xmlElement = StudioXmlElements.CONDITIONS,
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/conditions.svg",
            target = {"io.jmix.flowui.component.genericfilter.GenericFilter"}
    )
    void conditions();

    @StudioElementsGroup(
            name = "Properties",
            elementClassFqn = "io.jmix.flowui.kit.stub.StudioFragmentPropertyElement",
            xmlElement = StudioXmlElements.PROPERTIES,
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/properties.svg",
            target = {"io.jmix.flowui.fragment.Fragment", "io.jmix.flowui.kit.stub.StudioFragmentRenderer"}
    )
    void fragmentProperties();

    @StudioElementsGroup(
            name = "Properties",
            elementClassFqn = "io.jmix.flowui.kit.stub.StudioGenericComponentPropertyElement",
            xmlElement = StudioXmlElements.PROPERTIES,
            icon = "io/jmix/flowui/kit/meta/icon/elementsgroup/properties.svg",
            target = TAG_PREFIX + "component"
    )
    void genericComponentProperties();
}
