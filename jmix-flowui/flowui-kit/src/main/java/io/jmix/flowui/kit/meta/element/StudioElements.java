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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.tabs.Tab;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem;
import io.jmix.flowui.kit.component.loginform.JmixLoginI18n;
import io.jmix.flowui.kit.meta.*;
import io.jmix.flowui.kit.meta.GenericResolvingInfo.ResolvingStrategy;
import io.jmix.flowui.kit.meta.GenericResolvingInfo.ResolvingStrategy.ClassFqnStrategy;

@StudioUiKit
public interface StudioElements {

    @StudioElement(
            name = "AccordionPanel",
            classFqn = "com.vaadin.flow.component.accordion.AccordionPanel",
            target = {"com.vaadin.flow.component.accordion.Accordion"},
            xmlElement = "accordionPanel",
            icon = "io/jmix/flowui/kit/meta/icon/element/tab.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/accordion.html#_accordionpanel",
            visible = true,
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "summaryText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "opened", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"filled", "reverse", "small"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "100%")
            }
    )
    AccordionPanel accordionPanel();

    @StudioElement(
            name = "ShortcutCombination",
            xmlElement = "shortcutCombination",
            icon = "io/jmix/flowui/kit/meta/icon/element/shortcutCombination.svg",
            target = {"io.jmix.flowui.kit.action.Action"},
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "keyCombination", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "resetFocusOnActiveElement", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    void shortcutCombination();

    @StudioElement(
            name = "ActionItem",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.ActionItem",
            xmlElement = "actionItem",
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#actionItem",
            isInjectable = false,
            properties = {
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "ref", type = StudioPropertyType.ACTION_REF)
            }
    )
    DropdownButtonItem actionItem();

    @StudioElement(
            name = "Action",
            xmlElement = "action",
            classFqn = "io.jmix.flowui.kit.action.BaseAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            target = {"io.jmix.flowui.kit.component.dropdownbutton.ActionItem"},
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "actionVariant", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "description", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void dropdownButtonAction();

    @StudioElement(
            name = "AdditionalInformation",
            xmlElement = "additionalInformation",
            target = {"com.vaadin.flow.component.login.AbstractLogin"},
            unlimitedCount = false,
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
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#column",
            visible = true,
            isInjectable = false,
            properties = {
                    @StudioProperty(xmlAttribute = "autoWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "filterable", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "flexGrow", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "footer", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "frozen", category = StudioProperty.Category.POSITION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "header", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "key", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "T", required = true),
                    @StudioProperty(xmlAttribute = "resizable", category = StudioProperty.Category.SIZE, type = StudioPropertyType.BOOLEAN,
                            defaultValueRef = "parent:resizable"),
                    @StudioProperty(xmlAttribute = "sortable", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValueRef = "parent:sortable"),
                    @StudioProperty(xmlAttribute = "textAlign", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.ColumnTextAlign", defaultValue = "START",
                            options = {"CENTER", "END", "START"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "UNDEFINED"),
                    @StudioProperty(xmlAttribute = "editable", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            },
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.Renderer"
                    ),
                    @StudioSupplyHandler(
                            methodName = "setEditorComponent",
                            parameterType = "com.vaadin.flow.component.Component"
                    ),
                    @StudioSupplyHandler(
                            methodName = "setEditorComponent",
                            parameterType = "com.vaadin.flow.function.SerializableFunction",
                            genericResolvingInfo = {
                                    @GenericResolvingInfo(
                                            typeParameter = "T",
                                            resolvingStrategy = @ResolvingStrategy(
                                                    parentTagByDepthStrategy = @ResolvingStrategy.ParentTagByDepthStrategy(
                                                            parentTagDepth = 2, // columns -> grid
                                                            takeFromTypeParameter = "E" // E parameter from Grid<E>
                                                    )
                                            )
                                    ),
                                    @GenericResolvingInfo(
                                            typeParameter = "R",
                                            resolvingStrategy = @ResolvingStrategy(
                                                    classFqnStrategy = @ClassFqnStrategy(
                                                            classFqn = "com.vaadin.flow.component.Component"
                                                    )
                                            )
                                    )
                            }
                    )
            }
    )
    Grid.Column column();

    @StudioElement(
            name = "EditorActionsColumn",
            classFqn = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            xmlElement = "editorActionsColumn",
            icon = "io/jmix/flowui/kit/meta/icon/element/column.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#editorActionsColumn",
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "autoWidth", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "flexGrow", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "footer", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "header", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "key", type = StudioPropertyType.STRING,
                            initialValue = "editorActionsColumn"),
                    @StudioProperty(xmlAttribute = "resizable", type = StudioPropertyType.BOOLEAN,
                            defaultValueRef = "parent:resizable"),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"},
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
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            initialValue = "PENCIL",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            initialValue = "msg:///actions.Edit"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "whiteSpace", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "warning", "error", "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
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
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            initialValue = "CHECK",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "whiteSpace", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "warning", "error", "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
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
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            initialValue = "BAN",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "whiteSpace", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "warning", "error", "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
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
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            initialValue = "BAN",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            initialValue = "msg:///actions.Cancel"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "whiteSpace", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "warning", "error", "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
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
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#renderers",
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
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#renderers",
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
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#renderers",
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
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#componentItem",
            isInjectable = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            ),
            properties = {
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true)
            }
    )
    DropdownButtonItem componentItem();

    @StudioElement(
            name = "ErrorMessage",
            classFqn = "com.vaadin.flow.component.login.LoginI18n.ErrorMessage",
            xmlElement = "errorMessage",
            unlimitedCount = false,
            target = {"com.vaadin.flow.component.login.AbstractLogin"},
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
            unlimitedCount = false,
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
    JmixLoginI18n.JmixForm jmixLoginForm();

    @StudioElement(
            name = "Form",
            classFqn = "com.vaadin.flow.component.login.LoginI18n.Form",
            xmlElement = "form",
            target = {"com.vaadin.flow.component.login.LoginOverlay"},
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "forgotPassword", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "password", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "submit", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "username", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    LoginI18n.Form loginForm();

    @StudioElement(
            name = "Header",
            xmlElement = "header",
            target = {"com.vaadin.flow.component.login.LoginOverlay"},
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "description", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void loginHeader();

    @StudioElement(
            name = "Footer",
            xmlElement = "footer",
            target = {"com.vaadin.flow.component.login.LoginOverlay"},
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 99999
                    )
            )
    )
    void loginFooter();

    @StudioElement(
            name = "CustomFormArea",
            xmlElement = "customFormArea",
            target = {"com.vaadin.flow.component.login.LoginOverlay"},
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 99999
                    )
            )
    )
    void loginCustomFormArea();

    @StudioElement(
            name = "GenericFilter",
            classFqn = "io.jmix.flowui.facet.queryparameters.GenericFilterQueryParametersBinder",
            xmlElement = "genericFilter",
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            icon = "io/jmix/flowui/kit/meta/icon/element/filter.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "component", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_REF,
                            componentRefTags = "genericFilter", required = true),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
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
            documentationLink = "%VERSION%/flow-ui/vc/components/tabs.html#tab",
            visible = true,
            properties = {
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "flexGrow", category = StudioProperty.Category.POSITION, type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "lazy", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"icon-on-top"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
            }
    )
    Tab tab();

    @StudioElement(
            name = "TextItem",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.TextItem",
            xmlElement = "textItem",
            isInjectable = false,
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#textItem",
            properties = {
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    DropdownButtonItem textItem();

    @StudioElement(
            name = "Separator",
            classFqn = "io.jmix.flowui.kit.component.stub.DropdownButtonStubSeparator",
            xmlElement = "separator",
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#separator"
    )
    DropdownButtonItem separator();

    @StudioElement(
            name = "Tooltip",
            classFqn = "com.vaadin.flow.component.shared.Tooltip",
            icon = "io/jmix/flowui/kit/meta/icon/element/tooltip.svg",
            xmlElement = "tooltip",
            documentationLink = "%VERSION%/flow-ui/vc/components/tooltip.html",
            unlimitedCount = false,
            target = {"io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.kit.component.button.JmixButton",
                    "io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup",
                    "io.jmix.flowui.component.checkbox.JmixCheckbox",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.combobox.JmixComboBox",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
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
                    "com.vaadin.flow.component.tabs.Tab",
                    "io.jmix.flowui.component.menufilterfield.MenuFilterField"},
            properties = {
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING, required = true),
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
                    @StudioProperty(xmlAttribute = "component", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_REF,
                            componentRefTags = {"simplePagination"}, required = true),
                    @StudioProperty(xmlAttribute = "firstResultParam", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
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
                    @StudioProperty(xmlAttribute = "component", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_REF,
                            componentRefTags = "propertyFilter", required = true),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
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
                    @StudioProperty(xmlAttribute = "component", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_REF, componentRefTags = "dataGridFilter", required = true),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "param", type = StudioPropertyType.STRING),
            }
    )
    void dataGridFilter();

    @StudioElement(
            name = "ResponsiveStep",
            classFqn = "com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep",
            xmlElement = "responsiveStep",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/formLayout.html#responsive-steps",
            properties = {
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, required = true),
                    @StudioProperty(xmlAttribute = "columns", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER, required = true),
                    @StudioProperty(xmlAttribute = "labelsPosition", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.formlayout.FormLayout$ResponsiveStep$LabelsPosition",
                            options = {"ASIDE", "TOP"}, defaultValue = "TOP")
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
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clickShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }

    )
    FormLayout.FormItem formItem();

    @StudioElement(
            name = "ResponsiveStep",
            classFqn = "io.jmix.flowui.component.SupportsResponsiveSteps.ResponsiveStep",
            xmlElement = "responsiveStep",
            properties = {
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, required = true),
                    @StudioProperty(xmlAttribute = "columns", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER, required = true),
                    @StudioProperty(xmlAttribute = "labelsPosition", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.SupportsResponsiveSteps$ResponsiveStep$LabelsPosition",
                            options = {"ASIDE", "TOP"}, defaultValue = "TOP")
            }
    )
    void responsiveStep();

    @StudioElement(
            name = "PropertyFilter",
            xmlElement = "propertyFilter",
            target = {
                    "io.jmix.flowui.component.logicalfilter.GroupFilter",
                    "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration"
            },
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "defaultValue", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "labelVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "operation", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.propertyfilter.PropertyFilter$Operation",
                            options = {"EQUAL", "NOT_EQUAL", "GREATER",
                                    "GREATER_OR_EQUAL", "LESS", "LESS_OR_EQUAL", "CONTAINS", "NOT_CONTAINS",
                                    "STARTS_WITH", "ENDS_WITH", "IS_SET", "IN_LIST", "NOT_IN_LIST", "IN_INTERVAL",
                                    "IS_COLLECTION_EMPTY", "MEMBER_OF_COLLECTION",
                                    "NOT_MEMBER_OF_COLLECTION"}, required = true),
                    @StudioProperty(xmlAttribute = "operationEditable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "operationTextVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "parameterName", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V", required = true),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
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
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "defaultValue", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "hasInExpression", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "labelVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "parameterClass", type = StudioPropertyType.STRING,
                            typeParameter = "V", required = true),
                    @StudioProperty(xmlAttribute = "parameterName", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "operation", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.logicalfilter.LogicalFilterComponent$Operation",
                            options = {"AND", "OR"}, required = true),
                    @StudioProperty(xmlAttribute = "operationTextVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "summaryText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
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
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
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
                    @StudioProperty(xmlAttribute = "excludeProperties", type = StudioPropertyType.STRING),
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
            documentationLink = "2.1/whats-new/index.html#fetching-items-in-dropdowns",
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
            documentationLink = "2.1/whats-new/index.html#fetching-items-in-dropdowns",
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
            documentationLink = "2.1/whats-new/index.html#fetching-items-in-dropdowns",
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
            documentationLink = "2.1/whats-new/index.html#prefix-and-suffix-components",
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
            unsupportedTarget = {"com.vaadin.flow.component.applayout.DrawerToggle",
                    "io.jmix.searchflowui.component.SearchField"},
            documentationLink = "2.1/whats-new/index.html#prefix-and-suffix-components",
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
                    @StudioProperty(xmlAttribute = "componentId", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN)
            },
            isInjectable = false,
            injectionIdentifier = StudioComponent.EMPTY_INJECTION_IDENTIFIER
    )
    void settingsFacetComponent();

    @StudioElement(
            name = "MenuItem",
            xmlElement = "menuItem",
            target = {"io.jmix.flowui.component.gridcolumnvisibility.JmixGridColumnVisibility"},
            properties = {
                    @StudioProperty(xmlAttribute = "refColumn", type = StudioPropertyType.COMPONENT_REF,
                            componentRefTags = {"column"}, required = true),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void gridColumnVisibilityMenuItem();

    @StudioElement(
            name = "ContextMenu",
            icon = "io/jmix/flowui/kit/meta/icon/element/contextMenu.svg",
            xmlElement = "contextMenu",
            classFqn = "io.jmix.flowui.kit.component.grid.JmixGridContextMenu",
            unlimitedCount = false,
            target = {"io.jmix.flowui.component.grid.DataGrid", "io.jmix.flowui.component.grid.TreeDataGrid"},
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    void gridContextMenu();

    @StudioElement(
            name = "EmptyStateComponent",
            xmlElement = "emptyStateComponent",
            target = {"io.jmix.flowui.component.grid.DataGrid",
                    "io.jmix.flowui.component.grid.TreeDataGrid"},
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            )
    )
    void gridEmptyStateComponent();

    @StudioElement(
            name = "Item",
            icon = "io/jmix/flowui/kit/meta/icon/element/contextMenuItem.svg",
            xmlElement = "item",
            classFqn = "com.vaadin.flow.component.grid.contextmenu.GridMenuItem",
            target = {"io.jmix.flowui.kit.component.grid.JmixGridContextMenu",
                    "com.vaadin.flow.component.grid.contextmenu.GridMenuItem"},
            properties = {
                    @StudioProperty(xmlAttribute = "action", type = StudioPropertyType.ACTION_REF,
                            classFqn = "io.jmix.flowui.kit.action.Action"),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "whiteSpace", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"})
            }
    )
    void gridContextMenuItem();

    @StudioElement(
            name = "Separator",
            icon = "io/jmix/flowui/kit/meta/icon/element/contextMenuSeparator.svg",
            xmlElement = "separator",
            target = {"io.jmix.flowui.kit.component.grid.JmixGridContextMenu",
                    "com.vaadin.flow.component.grid.contextmenu.GridMenuItem"}
    )
    void gridContextMenuSeparator();

    @StudioElement(
            name = "Content",
            icon = "io/jmix/flowui/kit/meta/icon/view/layout.svg",
            xmlElement = "content",
            unlimitedCount = false,
            visible = true,
            target = {"io.jmix.flowui.fragment.Fragment"},
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = {
                            @StudioAvailableChildrenInfo.TagInfo(
                                    qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                                    maxCount = 1L
                            )
                    }
            )
    )
    VerticalLayout fragmentContent();

    @StudioElement(
            name = "Property",
            classFqn = "io.jmix.flowui.kit.stub.StudioFragmentPropertyElement",
            xmlElement = "property",
            icon = "io/jmix/flowui/kit/meta/icon/element/property.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.ENUMERATION,
                            options = {"CONTAINER_REF", "LOADER_REF", "ICON"})
            }
    )
    void fragmentProperty();

    @StudioElement(
            name = "Property",
            classFqn = "io.jmix.flowui.kit.stub.StudioGenericComponentPropertyElement",
            icon = "io/jmix/flowui/kit/meta/icon/element/property.svg",
            xmlElement = "property",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.ENUMERATION,
                            options = {"CONTAINER_REF", "LOADER_REF", "ICON"})
            }
    )
    void genericComponentProperty();

    @StudioElement(
            name = "FragmentRenderer",
            classFqn = "io.jmix.flowui.kit.stub.StudioFragmentRenderer",
            icon = "io/jmix/flowui/kit/meta/icon/element/fragmentRenderer.svg",
            xmlElement = "fragmentRenderer",
            target = {
                    "io.jmix.flowui.component.virtuallist.JmixVirtualList",
                    "io.jmix.flowui.component.grid.DataGridColumn",
                    "io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup",
                    "io.jmix.flowui.component.select.JmixSelect",
                    "io.jmix.flowui.component.listbox.JmixListBox",
                    "io.jmix.flowui.component.listbox.JmixMultiSelectListBox"
            },
            unlimitedCount = false,
            properties = {
                    @StudioProperty(xmlAttribute = "class", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.FRAGMENT_CLASS, required = true)
            }
    )
    void fragmentRenderer();

    @StudioElement(
            name = "StartSlot",
            icon = "io/jmix/flowui/kit/meta/icon/element/column.svg",
            xmlElement = "startSlot",
            target = "com.vaadin.flow.component.orderedlayout.HorizontalLayout",
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 99999
                    )
            )
    )
    void startSlot();

    @StudioElement(
            name = "MiddleSlot",
            icon = "io/jmix/flowui/kit/meta/icon/element/column.svg",
            xmlElement = "middleSlot",
            target = "com.vaadin.flow.component.orderedlayout.HorizontalLayout",
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 99999
                    )
            )
    )
    void middleSlot();

    @StudioElement(
            name = "EndSlot",
            icon = "io/jmix/flowui/kit/meta/icon/element/column.svg",
            xmlElement = "endSlot",
            target = "com.vaadin.flow.component.orderedlayout.HorizontalLayout",
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 99999
                    )
            )
    )
    void endSlot();

}
