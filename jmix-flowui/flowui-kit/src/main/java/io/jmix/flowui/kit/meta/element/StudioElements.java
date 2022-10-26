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
import com.vaadin.flow.component.tabs.Tab;
import io.jmix.flowui.kit.component.loginform.JmixLoginI18n;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem;
import io.jmix.flowui.kit.meta.StudioElement;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioElements {

    @StudioElement(
            name = "CollectionFormatter",
            classFqn = "io.jmix.flowui.component.formatter.CollectionFormatter",
            xmlElement = "collection",
            icon = "io/jmix/flowui/kit/meta/icon/element/formatter.svg"
    )
    void collectionFormatter();

    @StudioElement(
            name = "CustomFormatter",
            classFqn = "io.jmix.flowui.component.formatter.CustomFormatter",
            xmlElement = "custom",
            icon = "io/jmix/flowui/kit/meta/icon/element/formatter.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "bean", type = StudioPropertyType.STRING, required = true)
            }
    )
    void customFormatter();

    @StudioElement(
            name = "DateFormatter",
            classFqn = "io.jmix.flowui.component.formatter.DateFormatter",
            xmlElement = "date",
            icon = "io/jmix/flowui/kit/meta/icon/element/formatter.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "format", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.ENUMERATION,
                            options = {"DATE", "DATETIME"}),
                    @StudioProperty(xmlAttribute = "useUserTimeZone", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
            }
    )
    void dateFormatter();

    @StudioElement(
            name = "NumberFormatter",
            classFqn = "io.jmix.flowui.component.formatter.NumberFormatter",
            xmlElement = "number",
            icon = "io/jmix/flowui/kit/meta/icon/element/formatter.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "format", type = StudioPropertyType.STRING)
            }
    )
    void numberFormatter();

    @StudioElement(
            name = "Tab",
            classFqn = "com.vaadin.flow.component.tabs.Tab",
            target = {"com.vaadin.flow.component.tabs.Tabs"},
            xmlElement = "tab",
            visible = true,
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "flewGrow", type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"icon-on-top"}),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
            }
    )
    Tab tab();

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
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE, defaultValue = "100px")
            }
    )
    Grid.Column column();

    @StudioElement(
            name = "ResponsiveStep",
            classFqn = "com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep",
            xmlElement = "responsiveStep",
            properties = {
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.INTEGER, required = true),
                    @StudioProperty(xmlAttribute = "columns", type = StudioPropertyType.INTEGER, required = true),
                    @StudioProperty(xmlAttribute = "labelsPosition", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.formlayout.FormLayout$ResponsiveStep$LabelsPosition",
                            options = {"ASIDE", "TOP"})
            }
    )
    FormLayout.ResponsiveStep responsiveStep();

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
            name = "AdditionalInformation",
            xmlElement = "additionalInformation",
            target = {"io.jmix.flowui.kit.component.loginform.EnhancedLoginForm"},
            properties = {
                    @StudioProperty(xmlAttribute = "message", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void additionalInformation();

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
            name = "ComponentItem",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.ComponentItem",
            xmlElement = "componentItem",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true)
            }
    )
    DropdownButtonItem componentItem();

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
            name = "Pagination",
            classFqn = "io.jmix.flowui.facet.queryparameters.PaginationQueryParametersBinder",
            xmlElement = "pagination",
            target = {"io.jmix.flowui.facet.QueryParametersFacet"},
            properties = {
                    @StudioProperty(xmlAttribute = "component", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "firstResultParam", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxResultsParam", type = StudioPropertyType.STRING)
            }
    )
    void pagination();
}
