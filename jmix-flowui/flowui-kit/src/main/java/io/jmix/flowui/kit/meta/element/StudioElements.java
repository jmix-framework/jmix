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

package io.jmix.flowui.kit.meta.element;

import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.tabs.Tab;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem;
import io.jmix.flowui.kit.component.loginform.JmixLoginI18n;
import io.jmix.flowui.kit.meta.StudioElement;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioElements {

    @StudioElement(
            name = "AccordionPanel",
            classFqn = "com.vaadin.flow.component.accordion.AccordionPanel",
            target = {"com.vaadin.flow.component.accordion.Accordion"},
            xmlElement = "accordionPanel",
            visible = true,
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "summaryText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"filled", "reverse", "small"}),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE, defaultValue = "100%")
            }
    )
    AccordionPanel accordionPanel();

    @StudioElement(
            name = "ActionItem",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.ActionItem",
            xmlElement = "actionItem",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "ref", type = StudioPropertyType.ACTION_REF)
            }
    )
    DropdownButtonItem actionItem();

    @StudioElement(
            name = "AdditionalInformation",
            xmlElement = "additionalInformation",
            target = {"io.jmix.flowui.kit.component.loginform.EnhancedLoginForm"},
            properties = {
                    @StudioProperty(xmlAttribute = "message", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void additionalInformation();

    @StudioElement(
            name = "Column",
            classFqn = "com.vaadin.flow.component.grid.Grid.Column",
            xmlElement = "column",
            icon = "io/jmix/flowui/kit/meta/icon/element/column.svg",
            visible = true,
            properties = {
                    @StudioProperty(xmlAttribute = "autoWidth", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "flexGrow", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "footer", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "frozen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "header", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "key", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF, required = true),
                    @StudioProperty(xmlAttribute = "resizable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "sortable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "textAlign", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.ColumnTextAlign", defaultValue = "START",
                            options = {"CENTER", "END", "START"}),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE, defaultValue = "UNDEFINED"),
                    @StudioProperty(xmlAttribute = "editable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    Grid.Column column();

    @StudioElement(
            name = "ComponentItem",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.ComponentItem",
            xmlElement = "componentItem",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true)
            }
    )
    DropdownButtonItem componentItem();

    @StudioElement(
            name = "ErrorMessage",
            classFqn = "com.vaadin.flow.component.login.LoginI18n.ErrorMessage",
            xmlElement = "errorMessage",
            target = {"io.jmix.flowui.kit.component.loginform.EnhancedLoginForm"},
            properties = {
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "message", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    LoginI18n.ErrorMessage loginErrorMessage();

    @StudioElement(
            name = "Form",
            classFqn = "io.jmix.flowui.kit.component.loginform.JmixLoginI18n.JmixForm",
            xmlElement = "form",
            target = {"io.jmix.flowui.kit.component.loginform.EnhancedLoginForm"},
            properties = {
                    @StudioProperty(xmlAttribute = "forgotPassword", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "password", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "rememberMe", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "submit", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "username", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    JmixLoginI18n.JmixForm loginForm();

    @StudioElement(
            name = "GenericFilter",
            classFqn = "io.jmix.flowui.facet.queryparameters.GenericFilterQueryParametersBinder",
            xmlElement = "genericFilter",
            target = {"io.jmix.flowui.facet.QueryParametersFacet"},
            properties = {
                    @StudioProperty(xmlAttribute = "component", type = StudioPropertyType.COMPONENT_REF, required = true),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "conditionParam", type = StudioPropertyType.STRING)
            }
    )
    void genericFilter();

    @StudioElement(
            name = "Tab",
            classFqn = "com.vaadin.flow.component.tabs.Tab",
            target = {"com.vaadin.flow.component.tabs.Tabs", "io.jmix.flowui.component.tabsheet.JmixTabSheet"},
            xmlElement = "tab",
            visible = true,
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "flewGrow", type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"icon-on-top"}),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
            }
    )
    Tab tab();

    @StudioElement(
            name = "TextItem",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.TextItem",
            xmlElement = "textItem",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    DropdownButtonItem textItem();

    @StudioElement(
            name = "Tooltip",
            classFqn = "com.vaadin.flow.component.shared.Tooltip",
            icon = "io/jmix/flowui/kit/meta/icon/element/tooltip.svg",
            xmlElement = "tooltip",
            target = {"io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.kit.component.button.JmixButton",
                    "io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup",
                    "io.jmix.flowui.component.checkbox.JmixCheckbox",
                    "io.jmix.flowui.component.combobox.JmixComboBox",
                    "io.jmix.flowui.kit.component.combobox.ComboBoxPicker",
                    "io.jmix.flowui.kit.component.combobutton.ComboButton",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "com.vaadin.flow.component.applayout.DrawerToggle",
                    "io.jmix.flowui.kit.component.dropdownbutton.DropdownButton",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.upload.FileStorageUploadField",
                    "io.jmix.flowui.component.upload.FileUploadField",
                    "com.vaadin.flow.component.icon.Icon",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.listbox.JmixListBox",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.listbox.JmixMultiSelectListBox",
                    "io.jmix.flowui.component.valuepicker.JmixMultiValuePicker",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup",
                    "io.jmix.flowui.component.select.JmixSelect",
                    "io.jmix.flowui.component.pagination.SimplePagination",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.TypedTextField",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.main.JmixUserIndicator",
                    "io.jmix.flowui.component.valuepicker.JmixValuePicker",
                    "io.jmix.flowui.component.accordion.JmixAccordionPanel",
                    "io.jmix.flowui.component.details.JmixDetails",
                    "io.jmix.flowui.component.genericfilter.GenericFilter",
                    "io.jmix.flowui.component.propertyfilter.PropertyFilter",
                    "io.jmix.flowui.component.jpqlfilter.JpqlFilter",
                    "com.vaadin.flow.component.tabs.Tab"},
            properties = {
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "focusDelay", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "hideDelay", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "hoverDelay", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "manual", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "opened", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "position", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.shared.Tooltip.TooltipPosition",
                            options = {"TOP_START", "TOP", "TOP_END", "BOTTOM_START", "BOTTOM", "BOTTOM_END",
                                    "START_TOP", "START", "START_BOTTOM", "END_TOP", "END", "END_BOTTOM"})
            }
    )
    Tooltip tooltip();

    @StudioElement(
            name = "Pagination",
            classFqn = "io.jmix.flowui.facet.queryparameters.PaginationQueryParametersBinder",
            xmlElement = "pagination",
            target = {"io.jmix.flowui.facet.QueryParametersFacet"},
            properties = {
                    @StudioProperty(xmlAttribute = "component", type = StudioPropertyType.COMPONENT_REF, required = true),
                    @StudioProperty(xmlAttribute = "firstResultParam", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxResultsParam", type = StudioPropertyType.STRING)
            }
    )
    void pagination();

    @StudioElement(
            name = "PropertyFilter",
            classFqn = "io.jmix.flowui.facet.queryparameters.PropertyFilterQueryParametersBinder",
            xmlElement = "propertyFilter",
            target = {"io.jmix.flowui.facet.QueryParametersFacet"},
            properties = {
                    @StudioProperty(xmlAttribute = "component", type = StudioPropertyType.COMPONENT_REF, required = true),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "param", type = StudioPropertyType.STRING),
            }
    )
    void facetPropertyFilter();

    @StudioElement(
            name = "ResponsiveStep",
            classFqn = "com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep",
            xmlElement = "responsiveStep",
            properties = {
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE, required = true),
                    @StudioProperty(xmlAttribute = "columns", type = StudioPropertyType.INTEGER, required = true),
                    @StudioProperty(xmlAttribute = "labelsPosition", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.formlayout.FormLayout$ResponsiveStep$LabelsPosition",
                            options = {"ASIDE", "TOP"})
            }
    )
    FormLayout.ResponsiveStep formLayoutResponsiveStep();

    @StudioElement(
            name = "ResponsiveStep",
            classFqn = "io.jmix.flowui.component.SupportsResponsiveSteps.ResponsiveStep",
            xmlElement = "responsiveStep",
            properties = {
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE, required = true),
                    @StudioProperty(xmlAttribute = "columns", type = StudioPropertyType.INTEGER, required = true),
                    @StudioProperty(xmlAttribute = "labelsPosition", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.SupportsResponsiveSteps$ResponsiveStep$LabelsPosition",
                            options = {"ASIDE", "TOP"})
            }
    )
    void responsiveStep();

    @StudioElement(
            name = "PropertyFilter",
            classFqn = "io.jmix.flowui.component.propertyfilter.PropertyFilter",
            xmlElement = "propertyFilter",
            target = {
                    "io.jmix.flowui.component.logicalfilter.GroupFilter",
                    "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration"
            },
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "defaultValue", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "labelVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "operation", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.propertyfilter.PropertyFilter$Operation",
                            options = {"EQUAL", "NOT_EQUAL", "GREATER",
                                    "GREATER_OR_EQUAL", "LESS", "LESS_OR_EQUAL", "CONTAINS", "NOT_CONTAINS",
                                    "STARTS_WITH", "ENDS_WITH", "IS_SET"}, required = true),
                    @StudioProperty(xmlAttribute = "operationEditable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "operationTextVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "parameterName", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V", required = true),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    void propertyFilter();

    @StudioElement(
            name = "JpqlFilter",
            classFqn = "io.jmix.flowui.component.jpqlfilter.JpqlFilter",
            xmlElement = "jpqlFilter",
            target = {
                    "io.jmix.flowui.component.logicalfilter.GroupFilter",
                    "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration"
            },
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "defaultValue", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "hasInExpression", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "labelVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "parameterClass", type = StudioPropertyType.STRING,
                            typeParameter = "V", required = true),
                    @StudioProperty(xmlAttribute = "parameterName", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            }
    )
    void jpqlFilter();

    @StudioElement(
            name = "GroupFilter",
            classFqn = "io.jmix.flowui.component.logicalfilter.GroupFilter",
            xmlElement = "groupFilter",
            target = {
                    "io.jmix.flowui.component.logicalfilter.GroupFilter",
                    "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration"
            },
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "operation", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.logicalfilter.LogicalFilterComponent$Operation",
                            options = {"AND", "OR"}, required = true),
                    @StudioProperty(xmlAttribute = "operationTextVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "summaryText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
            }
    )
    void groupFilter();

    @StudioElement(
            name = "Configuration",
            classFqn = "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration",
            xmlElement = "configuration",
            target = {"io.jmix.flowui.component.logicalfilter.GroupFilter"},
            properties = {
                    @StudioProperty(xmlAttribute = "default", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "operation", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.logicalfilter.LogicalFilterComponent$Operation",
                            options = {"AND", "OR"}, defaultValue = "AND"),
            }
    )
    void configuration();

    @StudioElement(
            name = "Properties",
            classFqn = "io.jmix.flowui.component.genericfilter.inspector.FilterPropertiesInspector",
            xmlElement = "properties",
            target = {"io.jmix.flowui.component.genericfilter.GenericFilter"},
            properties = {
                    @StudioProperty(xmlAttribute = "include", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "exclude", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "excludeProperties", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "excludeRecursively", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    void properties();
}
