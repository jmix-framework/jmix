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
import io.jmix.flowui.kit.meta.*;

@StudioUiKit
public interface StudioElements {

    @StudioElement(
            name = "AccordionPanel",
            classFqn = "com.vaadin.flow.component.accordion.AccordionPanel",
            target = {"com.vaadin.flow.component.accordion.Accordion"},
            xmlElement = "accordionPanel",
            icon = "io/jmix/flowui/kit/meta/icon/element/tab.svg",
            visible = true,
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "summaryText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "opened", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
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
            classFqn = "io.jmix.flowui.component.grid.DataGridColumn",
            xmlElement = "column",
            icon = "io/jmix/flowui/kit/meta/icon/element/column.svg",
            visible = true,
            properties = {
                    @StudioProperty(xmlAttribute = "autoWidth", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "filterable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "flexGrow", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "footer", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "frozen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "header", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "key", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "T", required = true),
                    @StudioProperty(xmlAttribute = "resizable", type = StudioPropertyType.BOOLEAN,
                            defaultValueRef = "parent:resizable"),
                    @StudioProperty(xmlAttribute = "sortable", type = StudioPropertyType.BOOLEAN,
                            defaultValueRef = "parent:sortable"),
                    @StudioProperty(xmlAttribute = "textAlign", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.ColumnTextAlign", defaultValue = "START",
                            options = {"CENTER", "END", "START"}),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE, defaultValue = "UNDEFINED"),
                    @StudioProperty(xmlAttribute = "editable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            },
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.Renderer"
                    )
            }
    )
    Grid.Column column();


    @StudioElement(
            name = "EditorActionsColumn",
            classFqn = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            xmlElement = "editorActionsColumn",
            icon = "io/jmix/flowui/kit/meta/icon/element/column.svg",
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "autoWidth", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "flexGrow", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "footer", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "header", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "key", type = StudioPropertyType.STRING,
                            initialValue = "editorActionsColumn"),
                    @StudioProperty(xmlAttribute = "resizable", type = StudioPropertyType.BOOLEAN,
                            defaultValueRef = "parent:resizable"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE,
                            defaultValue = "UNDEFINED"),
            }
    )
    void editorActionsColumn();

    @StudioElement(
            name = "EditButton",
            xmlElement = "editButton",
            target = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "PENCIL",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Edit"),
                    @StudioProperty(xmlAttribute = "whiteSpace", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "error", "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "iconAfterText", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
            }
    )
    void editButton();

    @StudioElement(
            name = "SaveButton",
            xmlElement = "saveButton",
            target = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "CHECK",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "whiteSpace", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "error", "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "iconAfterText", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
            }
    )
    void saveButton();

    @StudioElement(
            name = "CloseButton",
            xmlElement = "closeButton",
            target = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "BAN",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "whiteSpace", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "error", "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "iconAfterText", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
            }
    )
    void closeButton();

    @StudioElement(
            name = "CancelButton",
            xmlElement = "cancelButton",
            target = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, defaultValue = "BAN",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Cancel"),
                    @StudioProperty(xmlAttribute = "whiteSpace", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "error", "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "iconAfterText", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
            }
    )
    void cancelButton();

    @StudioElement(
            name = "Aggregation",
            classFqn = "io.jmix.flowui.component.AggregationInfo",
            xmlElement = "aggregation",
            target = {"com.vaadin.flow.component.grid.Grid.Column"},
            unsupportedTarget = {"io.jmix.flowui.kit.component.grid.EditorActionsColumn"},
            properties = {
                    @StudioProperty(xmlAttribute = "cellTitle", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "strategyClass", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.AggregationInfo$Type",
                            options = {"SUM", "COUNT", "AVG", "MIN", "MAX"})
            }
    )
    void aggregationInfo();

    @StudioElement(
            name = "LocalDateRenderer",
            classFqn = "com.vaadin.flow.data.renderer.LocalDateRenderer",
            xmlElement = "localDateRenderer",
            target = {"com.vaadin.flow.component.grid.Grid.Column"},
            unsupportedTarget = {"io.jmix.flowui.kit.component.grid.EditorActionsColumn"},
            properties = {
                    @StudioProperty(xmlAttribute = "format",
                            type = StudioPropertyType.LOCALIZED_STRING, required = true),
                    @StudioProperty(xmlAttribute = "nullRepresentation",
                            type = StudioPropertyType.LOCALIZED_STRING)

            }
    )
    void localDateRenderer();

    @StudioElement(
            name = "LocalDateTimeRenderer",
            classFqn = "com.vaadin.flow.data.renderer.LocalDateTimeRenderer",
            xmlElement = "localDateTimeRenderer",
            target = {"com.vaadin.flow.component.grid.Grid.Column"},
            unsupportedTarget = {"io.jmix.flowui.kit.component.grid.EditorActionsColumn"},
            properties = {
                    @StudioProperty(xmlAttribute = "format",
                            type = StudioPropertyType.LOCALIZED_STRING, required = true),
                    @StudioProperty(xmlAttribute = "nullRepresentation",
                            type = StudioPropertyType.LOCALIZED_STRING)

            }
    )
    void localDateTimeRenderer();

    @StudioElement(
            name = "NumberRenderer",
            classFqn = "com.vaadin.flow.data.renderer.NumberRenderer",
            xmlElement = "numberRenderer",
            target = {"com.vaadin.flow.component.grid.Grid.Column"},
            unsupportedTarget = {"io.jmix.flowui.kit.component.grid.EditorActionsColumn"},
            properties = {
                    @StudioProperty(xmlAttribute = "format",
                            type = StudioPropertyType.LOCALIZED_STRING, required = true),
                    @StudioProperty(xmlAttribute = "nullRepresentation",
                            type = StudioPropertyType.LOCALIZED_STRING)

            }
    )
    void numberRenderer();

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
                    @StudioProperty(xmlAttribute = "message", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "username", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "password", type = StudioPropertyType.LOCALIZED_STRING)
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
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            icon = "io/jmix/flowui/kit/meta/icon/element/filter.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "component", type = StudioPropertyType.COMPONENT_REF, required = true),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "configurationParam", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "conditionParam", type = StudioPropertyType.STRING)
            }
    )
    void genericFilter();

    @StudioElement(
            name = "Tab",
            classFqn = "com.vaadin.flow.component.tabs.Tab",
            target = {"com.vaadin.flow.component.tabs.Tabs", "io.jmix.flowui.component.tabsheet.JmixTabSheet"},
            xmlElement = "tab",
            icon = "io/jmix/flowui/kit/meta/icon/element/tab.svg",
            visible = true,
            properties = {
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "flexGrow", type = StudioPropertyType.DOUBLE),
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
            unlimitedCount = false,
            target = {"io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.kit.component.button.JmixButton",
                    "io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup",
                    "io.jmix.flowui.component.checkbox.JmixCheckbox",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.combobox.JmixComboBox",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "com.vaadin.flow.component.applayout.DrawerToggle",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.upload.FileStorageUploadField",
                    "io.jmix.flowui.component.upload.FileUploadField",
                    "com.vaadin.flow.component.icon.Icon",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.listbox.JmixListBox",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.listbox.JmixMultiSelectListBox",
                    "io.jmix.flowui.component.valuepicker.JmixMultiValuePicker",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup",
                    "io.jmix.flowui.component.select.JmixSelect",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.TypedTextField",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.valuepicker.JmixValuePicker",
                    "io.jmix.flowui.component.accordion.JmixAccordionPanel",
                    "io.jmix.flowui.component.details.JmixDetails",
                    "io.jmix.flowui.component.genericfilter.GenericFilter",
                    "io.jmix.flowui.component.propertyfilter.PropertyFilter",
                    "io.jmix.flowui.component.jpqlfilter.JpqlFilter",
                    "com.vaadin.flow.component.tabs.Tab"},
            properties = {
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING, required = true),
                    @StudioProperty(xmlAttribute = "focusDelay", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "hideDelay", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "hoverDelay", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "manual", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "opened", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "position", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.shared.Tooltip$TooltipPosition",
                            setParameterFqn = "com.vaadin.flow.component.shared.Tooltip$TooltipPosition",
                            options = {"TOP_START", "TOP", "TOP_END", "BOTTOM_START", "BOTTOM", "BOTTOM_END",
                                    "START_TOP", "START", "START_BOTTOM", "END_TOP", "END", "END_BOTTOM"})
            }
    )
    Tooltip tooltip();

    @StudioElement(
            name = "Pagination",
            classFqn = "io.jmix.flowui.facet.queryparameters.PaginationQueryParametersBinder",
            xmlElement = "pagination",
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            icon = "io/jmix/flowui/kit/meta/icon/element/pagination.svg",
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
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            icon = "io/jmix/flowui/kit/meta/icon/element/filter.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "component", type = StudioPropertyType.COMPONENT_REF, required = true),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "param", type = StudioPropertyType.STRING),
            }
    )
    void facetPropertyFilter();

    @StudioElement(
            name = "DataGridFilter",
            classFqn = "io.jmix.flowui.facet.urlqueryparameters.DataGridFilterUrlQueryParametersBinder",
            xmlElement = "dataGridFilter",
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            properties = {
                    @StudioProperty(xmlAttribute = "component", type = StudioPropertyType.COMPONENT_REF, required = true),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "param", type = StudioPropertyType.STRING),
            }
    )
    void dataGridFilter();

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
            name = "FormItem",
            classFqn = "com.vaadin.flow.component.formlayout.FormLayout.FormItem",
            xmlElement = "formItem",
            target = {"com.vaadin.flow.component.formlayout.FormLayout"},
            visible = true,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            ),
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }

    )
    FormLayout.FormItem formItem();

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
                    @StudioProperty(xmlAttribute = "css", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "defaultValue", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
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
                    @StudioProperty(xmlAttribute = "css", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "defaultValue", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "hasInExpression", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
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
            name = "Condition",
            classFqn = "io.jmix.flowui.kit.component.stub.JpqlFilterCondition",
            xmlElement = "condition",
            target = {"io.jmix.flowui.component.jpqlfilter.JpqlFilter"},
            unlimitedCount = false
    )
    void jpqlFilterCondition();

    @StudioElement(
            name = "Jpql",
            classFqn = "io.jmix.flowui.kit.component.stub.JpqlFilterJpql",
            xmlns = "http://jmix.io/schema/flowui/jpql-condition",
            xmlElement = "jpql",
            xmlnsAlias = "c",
            target = {"io.jmix.flowui.kit.component.stub.JpqlFilterCondition"},
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "join", type = StudioPropertyType.JPQL_FILTER_JOIN),
                    @StudioProperty(xmlAttribute = "where", type = StudioPropertyType.JPQL_FILTER_WHERE),
            }
    )
    void jpqlFilterConditionJpql();

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
                    @StudioProperty(xmlAttribute = "css", type = StudioPropertyType.STRING),
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
            icon = "io/jmix/flowui/kit/meta/icon/element/configuration.svg",
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
            icon = "io/jmix/flowui/kit/meta/icon/element/property.svg",
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

    @StudioElement(
            name = "ItemsQuery",
            xmlElement = "itemsQuery",
            icon = "io/jmix/flowui/kit/meta/icon/element/itemsQuery.svg",
            unlimitedCount = false,
            target = {"io.jmix.flowui.component.combobox.JmixComboBox"},
            unsupportedTarget = {
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker"
            },
            properties = {
                    @StudioProperty(xmlAttribute = "searchStringFormat", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "escapeValueForLike", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "query", type = StudioPropertyType.JPA_QUERY)
            }
    )
    void valueItemsQuery();

    @StudioElement(
            name = "ItemsQuery",
            xmlElement = "itemsQuery",
            icon = "io/jmix/flowui/kit/meta/icon/element/itemsQuery.svg",
            unlimitedCount = false,
            target = {"io.jmix.flowui.component.combobox.EntityComboBox"},
            unsupportedTarget = {
                    "io.jmix.flowui.component.combobox.JmixComboBox",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker"
            },
            properties = {
                    @StudioProperty(xmlAttribute = "class", type = StudioPropertyType.ENTITY_CLASS, required = true),
                    @StudioProperty(xmlAttribute = "searchStringFormat", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "escapeValueForLike", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "query", type = StudioPropertyType.JPA_QUERY),
                    @StudioProperty(xmlAttribute = "fetchPlan", type = StudioPropertyType.FETCH_PLAN)
            }
    )
    void entityItemsQuery();

    @StudioElement(
            name = "ItemsQuery",
            xmlElement = "itemsQuery",
            icon = "io/jmix/flowui/kit/meta/icon/element/itemsQuery.svg",
            unlimitedCount = false,
            target = {
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker"
            },
            unsupportedTarget = {
                    "io.jmix.flowui.component.combobox.JmixComboBox",
                    "io.jmix.flowui.component.combobox.EntityComboBox"
            },
            properties = {
                    @StudioProperty(xmlAttribute = "class", type = StudioPropertyType.ENTITY_CLASS),
                    @StudioProperty(xmlAttribute = "searchStringFormat", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "escapeValueForLike", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "query", type = StudioPropertyType.JPA_QUERY),
                    @StudioProperty(xmlAttribute = "fetchPlan", type = StudioPropertyType.FETCH_PLAN)
            }
    )
    void itemsQuery();

    @StudioElement(
            name = "Prefix",
            xmlElement = "prefix",
            target = {"com.vaadin.flow.component.shared.HasPrefix"},
            unsupportedTarget = {"com.vaadin.flow.component.applayout.DrawerToggle"},
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            )
    )
    void prefix();

    @StudioElement(
            name = "Suffix",
            xmlElement = "suffix",
            target = {"com.vaadin.flow.component.shared.HasSuffix"},
            unsupportedTarget = {"com.vaadin.flow.component.applayout.DrawerToggle"},
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            )
    )
    void suffix();

    @StudioElement(
            name = "Component",
            xmlElement = "component",
            target = "io.jmix.flowui.facet.SettingsFacet",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN)
            }
    )
    void settingsFacetComponent();

    @StudioElement(
            name = "MenuItem",
            xmlElement = "menuItem",
            target = {"io.jmix.flowui.component.gridcolumnvisibility.JmixGridColumnVisibility"},
            properties = {
                    @StudioProperty(xmlAttribute = "refColumn", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void gridColumnVisibilityMenuItem();
}
