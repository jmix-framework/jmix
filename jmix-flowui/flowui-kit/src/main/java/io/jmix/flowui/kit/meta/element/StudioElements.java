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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.FontIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.tabs.Tab;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem;
import io.jmix.flowui.kit.component.loginform.JmixLoginI18n;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem;
import io.jmix.flowui.kit.meta.*;
import io.jmix.flowui.kit.meta.GenericResolvingInfo.ResolvingStrategy;
import io.jmix.flowui.kit.meta.GenericResolvingInfo.ResolvingStrategy.ClassFqnStrategy;

import static io.jmix.flowui.kit.meta.StudioMetaConstants.IDENTIFIER_PREFIX;

@StudioUiKit
interface StudioElements {

    @StudioElement(
            name = "AccordionPanel",
            classFqn = "com.vaadin.flow.component.accordion.AccordionPanel",
            target = {"com.vaadin.flow.component.accordion.Accordion"},
            xmlElement = StudioXmlElements.ACCORDION_PANEL,
            icon = "io/jmix/flowui/kit/meta/icon/element/tab.svg",
            documentationLink = "%VERSION%/flow-ui/vc/layouts/accordion.html#_accordionpanel",
            visible = true,
            propertyGroups = StudioPropertyGroups.AccordionPanelDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SUMMARY_TEXT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OPENED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"filled", "reverse", "small"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "100%")
            }
    )
    AccordionPanel accordionPanel();

    @StudioElement(
            name = "ShortcutCombination",
            xmlElement = StudioXmlElements.SHORTCUT_COMBINATION,
            icon = "io/jmix/flowui/kit/meta/icon/element/shortcutCombination.svg",
            target = {"io.jmix.flowui.kit.action.Action"},
            unsupportedTarget = {
                    IDENTIFIER_PREFIX + DROPDOWN_ACTION_ITEM_ACTION_IDENTIFIER,
                    IDENTIFIER_PREFIX + USER_MENU_ACTION_ITEM_ACTION_IDENTIFIER,
            },
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.ShortcutCombinationComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.KEY_COMBINATION, type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RESET_FOCUS_ON_ACTIVE_ELEMENT, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    void shortcutCombination();

    @StudioElement(
            name = "ActionItem",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.ActionItem",
            xmlElement = StudioXmlElements.ACTION_ITEM,
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#actionItem",
            isInjectable = false,
            propertyGroups = StudioPropertyGroups.ActionItemComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.REF, type = StudioPropertyType.ACTION_REF)
            }
    )
    DropdownButtonItem actionItem();

    String DROPDOWN_ACTION_ITEM_ACTION_IDENTIFIER = "jmix_dropdown_action_item_action";

    @StudioElement(
            identifier = DROPDOWN_ACTION_ITEM_ACTION_IDENTIFIER,
            name = "Action",
            xmlElement = StudioXmlElements.ACTION,
            classFqn = "io.jmix.flowui.kit.action.BaseAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            target = {"io.jmix.flowui.kit.component.dropdownbutton.ActionItem"},
            unlimitedCount = false,
            propertyGroups = {
                    StudioPropertyGroups.DropdownActionItem.class,
                    StudioPropertyGroups.StringType.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TYPE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void dropdownButtonAction();

    @StudioElement(
            name = "AdditionalInformation",
            xmlElement = StudioXmlElements.ADDITIONAL_INFORMATION,
            target = {"com.vaadin.flow.component.login.AbstractLogin"},
            unlimitedCount = false,
            propertyGroups = {
                    StudioPropertyGroups.Message.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MESSAGE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void additionalInformation();

    @StudioElement(
            name = "Column",
            classFqn = "io.jmix.flowui.component.grid.DataGridColumn",
            xmlElement = StudioXmlElements.COLUMN,
            icon = "io/jmix/flowui/kit/meta/icon/element/column.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#column",
            visible = true,
            isInjectable = false,
            propertyGroups = StudioPropertyGroups.ColumnComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.AUTO_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FILTERABLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValueRef = "parent:filterable"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FLEX_GROW, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FOOTER, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FROZEN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEADER, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.KEY, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "T", required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RESIZABLE, category = StudioProperty.Category.SIZE, type = StudioPropertyType.BOOLEAN,
                            defaultValueRef = "parent:resizable"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SORTABLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValueRef = "parent:sortable"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT_ALIGN, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.ColumnTextAlign", defaultValue = "START",
                            options = {"CENTER", "END", "START"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "UNDEFINED"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EDITABLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
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
            xmlElement = StudioXmlElements.EDITOR_ACTIONS_COLUMN,
            icon = "io/jmix/flowui/kit/meta/icon/element/column.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#editorActionsColumn",
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.EditorActionsColumnDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.AUTO_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FLEX_GROW, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FOOTER, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEADER, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.KEY, type = StudioPropertyType.STRING,
                            initialValue = "editorActionsColumn"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RESIZABLE, category = StudioProperty.Category.SIZE, type = StudioPropertyType.BOOLEAN,
                            defaultValueRef = "parent:resizable"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"},
                            defaultValue = "UNDEFINED"),
            }
    )
    void editorActionsColumn();

    @StudioElement(
            name = "EditButton",
            xmlElement = StudioXmlElements.EDIT_BUTTON,
            target = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.EditButtonComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            initialValue = "PENCIL",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            initialValue = "msg:///actions.Edit"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "warning", "error", "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON_AFTER_TEXT, type = StudioPropertyType.BOOLEAN, category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "false"),
            }
    )
    void editButton();

    @StudioElement(
            name = "SaveButton",
            xmlElement = StudioXmlElements.SAVE_BUTTON,
            target = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.SaveButtonComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            initialValue = "CHECK",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "warning", "error", "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON_AFTER_TEXT, type = StudioPropertyType.BOOLEAN, category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "false"),
            }
    )
    void saveButton();

    @StudioElement(
            name = "CloseButton",
            xmlElement = StudioXmlElements.CLOSE_BUTTON,
            target = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.CloseButtonComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            initialValue = "BAN",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "warning", "error", "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON_AFTER_TEXT, type = StudioPropertyType.BOOLEAN, category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "false"),
            }
    )
    void closeButton();

    @StudioElement(
            name = "CancelButton",
            xmlElement = StudioXmlElements.CANCEL_BUTTON,
            target = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.CancelButtonComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            initialValue = "BAN",
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            initialValue = "msg:///actions.Cancel"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHORTCUT_COMBINATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline",
                                    "primary", "success", "warning", "error", "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON_AFTER_TEXT, type = StudioPropertyType.BOOLEAN, category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "false"),
            }
    )
    void cancelButton();

    @StudioElement(
            name = "Aggregation",
            classFqn = "io.jmix.flowui.component.AggregationInfo",
            xmlElement = StudioXmlElements.AGGREGATION,
            target = {"com.vaadin.flow.component.grid.Grid.Column"},
            unsupportedTarget = {"io.jmix.flowui.kit.component.grid.EditorActionsColumn"},
            propertyGroups = StudioPropertyGroups.AggregationInfoComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CELL_TITLE, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.STRATEGY_CLASS, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TYPE, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.AggregationInfo$Type",
                            options = {"SUM", "COUNT", "AVG", "MIN", "MAX"})
            }
    )
    void aggregationInfo();

    @StudioElement(
            name = "LocalDateRenderer",
            classFqn = "com.vaadin.flow.data.renderer.LocalDateRenderer",
            xmlElement = StudioXmlElements.LOCAL_DATE_RENDERER,
            target = {"com.vaadin.flow.component.grid.Grid.Column"},
            unsupportedTarget = {"io.jmix.flowui.kit.component.grid.EditorActionsColumn"},
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#renderers",
            propertyGroups = StudioPropertyGroups.FormatAndNullRepresentation.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FORMAT,
                            type = StudioPropertyType.LOCALIZED_STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.NULL_REPRESENTATION,
                            type = StudioPropertyType.LOCALIZED_STRING)

            }
    )
    void localDateRenderer();

    @StudioElement(
            name = "LocalDateTimeRenderer",
            classFqn = "com.vaadin.flow.data.renderer.LocalDateTimeRenderer",
            xmlElement = StudioXmlElements.LOCAL_DATE_TIME_RENDERER,
            target = {"com.vaadin.flow.component.grid.Grid.Column"},
            unsupportedTarget = {"io.jmix.flowui.kit.component.grid.EditorActionsColumn"},
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#renderers",
            propertyGroups = StudioPropertyGroups.FormatAndNullRepresentation.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FORMAT,
                            type = StudioPropertyType.LOCALIZED_STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.NULL_REPRESENTATION,
                            type = StudioPropertyType.LOCALIZED_STRING)

            }
    )
    void localDateTimeRenderer();

    @StudioElement(
            name = "NumberRenderer",
            classFqn = "com.vaadin.flow.data.renderer.NumberRenderer",
            xmlElement = StudioXmlElements.NUMBER_RENDERER,
            target = {"com.vaadin.flow.component.grid.Grid.Column"},
            unsupportedTarget = {"io.jmix.flowui.kit.component.grid.EditorActionsColumn"},
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#renderers",
            propertyGroups = StudioPropertyGroups.FormatAndNullRepresentation.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FORMAT,
                            type = StudioPropertyType.LOCALIZED_STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.NULL_REPRESENTATION,
                            type = StudioPropertyType.LOCALIZED_STRING)

            }
    )
    void numberRenderer();

    @StudioElement(
            name = "ComponentItem",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.ComponentItem",
            xmlElement = StudioXmlElements.COMPONENT_ITEM,
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#componentItem",
            isInjectable = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            ),
            propertyGroups = {
                    StudioPropertyGroups.RequiredId.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true)
            }
    )
    DropdownButtonItem componentItem();

    @StudioElement(
            name = "ErrorMessage",
            classFqn = "com.vaadin.flow.component.login.LoginI18n.ErrorMessage",
            xmlElement = StudioXmlElements.ERROR_MESSAGE,
            unlimitedCount = false,
            target = {"com.vaadin.flow.component.login.AbstractLogin"},
            propertyGroups = StudioPropertyGroups.LoginErrorMessageDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MESSAGE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.USERNAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PASSWORD, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    LoginI18n.ErrorMessage loginErrorMessage();

    @StudioElement(
            name = "Form",
            classFqn = "io.jmix.flowui.kit.component.loginform.JmixLoginI18n.JmixForm",
            xmlElement = StudioXmlElements.FORM,
            unlimitedCount = false,
            target = {"io.jmix.flowui.kit.component.loginform.EnhancedLoginForm"},
            propertyGroups = StudioPropertyGroups.JmixLoginFormComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FORGOT_PASSWORD, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PASSWORD, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.REMEMBER_ME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SUBMIT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.USERNAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    JmixLoginI18n.JmixForm jmixLoginForm();

    @StudioElement(
            name = "Form",
            classFqn = "com.vaadin.flow.component.login.LoginI18n.Form",
            xmlElement = StudioXmlElements.FORM,
            target = {"com.vaadin.flow.component.login.LoginOverlay"},
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.LoginFormDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FORGOT_PASSWORD, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PASSWORD, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SUBMIT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.USERNAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    LoginI18n.Form loginForm();

    @StudioElement(
            name = "Header",
            xmlElement = StudioXmlElements.HEADER,
            target = {"com.vaadin.flow.component.login.LoginOverlay"},
            unlimitedCount = false,
            propertyGroups = {
                    StudioPropertyGroups.Title.class,
                    StudioPropertyGroups.Description.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void loginHeader();

    @StudioElement(
            name = "Footer",
            xmlElement = StudioXmlElements.FOOTER,
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
            xmlElement = StudioXmlElements.CUSTOM_FORM_AREA,
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
            xmlElement = StudioXmlElements.GENERIC_FILTER,
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            icon = "io/jmix/flowui/kit/meta/icon/element/filter.svg",
            propertyGroups = StudioPropertyGroups.GenericFilterElementComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COMPONENT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_REF,
                            componentRefTags = "genericFilter", required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CONFIGURATION_PARAM, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CONDITION_PARAM, type = StudioPropertyType.STRING)
            }
    )
    void genericFilter();

    @StudioElement(
            name = "Tab",
            classFqn = "com.vaadin.flow.component.tabs.Tab",
            target = {"com.vaadin.flow.component.tabs.Tabs", "io.jmix.flowui.component.tabsheet.JmixTabSheet"},
            xmlElement = StudioXmlElements.TAB,
            icon = "io/jmix/flowui/kit/meta/icon/element/tab.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/tabs.html#tab",
            visible = true,
            propertyGroups = StudioPropertyGroups.TabComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FLEX_GROW, category = StudioProperty.Category.POSITION, type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABEL, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LAZY, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"icon-on-top"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
            }
    )
    Tab tab();

    @StudioElement(
            name = "TextItem",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.TextItem",
            xmlElement = StudioXmlElements.TEXT_ITEM,
            isInjectable = false,
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#textItem",
            propertyGroups = {
                    StudioPropertyGroups.RequiredId.class,
                    StudioPropertyGroups.Text.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    DropdownButtonItem textItem();

    @StudioElement(
            name = "Separator",
            classFqn = "io.jmix.flowui.kit.component.stub.DropdownButtonStubSeparator",
            xmlElement = StudioXmlElements.SEPARATOR,
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#separator"
    )
    DropdownButtonItem separator();


    //region UserMenu

    @StudioElement(
            name = "TextItem",
            classFqn = "io.jmix.flowui.kit.component.usermenu.TextUserMenuItem",
            xmlElement = StudioXmlElements.TEXT_ITEM,
            isInjectable = false,
            documentationLink = "%VERSION%/flow-ui/vc/components/userMenu.html#textItem",
            propertyGroups = StudioPropertyGroups.TextUserItemUserMenuItemComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CHECKABLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CHECKED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"non-checkable"})
            }
    )
    UserMenuItem textUserItemUserMenuItem();

    @StudioElement(
            name = "ActionItem",
            classFqn = "io.jmix.flowui.kit.component.usermenu.ActionUserMenuItem",
            xmlElement = StudioXmlElements.ACTION_ITEM,
            documentationLink = "%VERSION%/flow-ui/vc/components/userMenu.html#actionItem",
            isInjectable = false,
            propertyGroups = StudioPropertyGroups.ActionUserMenuItemComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.REF, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ACTION_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CHECKABLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CHECKED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"non-checkable"})
            }
    )
    UserMenuItem actionUserMenuItem();

    String USER_MENU_ACTION_ITEM_ACTION_IDENTIFIER = "jmix_user_menu_action_item_action";

    @StudioElement(
            identifier = USER_MENU_ACTION_ITEM_ACTION_IDENTIFIER,
            name = "Action",
            xmlElement = StudioXmlElements.ACTION,
            classFqn = "io.jmix.flowui.kit.action.BaseAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            target = {"io.jmix.flowui.kit.component.usermenu.ActionUserMenuItem"},
            unlimitedCount = false,
            propertyGroups = {
                    StudioPropertyGroups.DropdownActionItem.class,
                    StudioPropertyGroups.StringType.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TYPE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION_VARIANT, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon")
            }
    )
    void actionUserMenuItemAction();

    @StudioElement(
            name = "ComponentItem",
            classFqn = "io.jmix.flowui.kit.component.usermenu.ComponentUserMenuItem",
            xmlElement = StudioXmlElements.COMPONENT_ITEM,
            documentationLink = "%VERSION%/flow-ui/vc/components/userMenu.html#componentItem",
            isInjectable = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            ),
            propertyGroups = StudioPropertyGroups.ComponentUserMenuItemComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CHECKABLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CHECKED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"non-checkable"})
            }
    )
    UserMenuItem componentUserMenuItem();

    @StudioElement(
            name = "ViewItem",
            classFqn = "io.jmix.flowui.component.usermenu.ViewUserMenuItem",
            xmlElement = StudioXmlElements.VIEW_ITEM,
            isInjectable = false,
            documentationLink = "%VERSION%/flow-ui/vc/components/userMenu.html#viewItem",
            propertyGroups = StudioPropertyGroups.ViewUserItemUserMenuItemComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VIEW_ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VIEW_CLASS, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OPEN_MODE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setOpenMode", classFqn = "io.jmix.flowui.view.OpenMode",
                            defaultValue = "NAVIGATION", options = {"NAVIGATION", "DIALOG"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CHECKABLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CHECKED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"non-checkable"})
            }
    )
    UserMenuItem viewUserItemUserMenuItem();

    @StudioElement(
            name = "Separator",
            classFqn = "io.jmix.flowui.kit.component.stub.UserMenuStubSeparatorItem",
            xmlElement = StudioXmlElements.SEPARATOR,
            documentationLink = "%VERSION%/flow-ui/vc/components/userMenu.html#separator"
    )
    UserMenuItem userMenuSeparator();
    //endregion

    @StudioElement(
            name = "Tooltip",
            classFqn = "com.vaadin.flow.component.shared.Tooltip",
            icon = "io/jmix/flowui/kit/meta/icon/element/tooltip.svg",
            xmlElement = StudioXmlElements.TOOLTIP,
            documentationLink = "%VERSION%/flow-ui/vc/components/tooltip.html",
            unlimitedCount = false,
            target = {
                    "com.vaadin.flow.component.tabs.Tab",
                    "com.vaadin.flow.component.icon.Icon",
                    "com.vaadin.flow.component.icon.SvgIcon",
                    "com.vaadin.flow.component.icon.FontIcon",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.kit.component.button.JmixButton",
                    "io.jmix.flowui.kit.component.twincolumn.JmixTwinColumn",
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
                    "io.jmix.flowui.component.menufilterfield.MenuFilterField"},
            propertyGroups = StudioPropertyGroups.TooltipComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FOCUS_DELAY, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HIDE_DELAY, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HOVER_DELAY, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MANUAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OPENED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.POSITION, type = StudioPropertyType.ENUMERATION,
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
            xmlElement = StudioXmlElements.PAGINATION,
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            icon = "io/jmix/flowui/kit/meta/icon/element/pagination.svg",
            propertyGroups = StudioPropertyGroups.PaginationComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COMPONENT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_REF,
                            componentRefTags = {"simplePagination"}, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FIRST_RESULT_PARAM, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_RESULTS_PARAM, type = StudioPropertyType.STRING)
            }
    )
    void pagination();

    @StudioElement(
            name = "PropertyFilter",
            classFqn = "io.jmix.flowui.facet.queryparameters.PropertyFilterQueryParametersBinder",
            xmlElement = StudioXmlElements.PROPERTY_FILTER,
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            icon = "io/jmix/flowui/kit/meta/icon/element/filter.svg",
            propertyGroups = StudioPropertyGroups.FacetPropertyFilterComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COMPONENT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_REF,
                            componentRefTags = "propertyFilter", required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PARAM, type = StudioPropertyType.STRING),
            }
    )
    void facetPropertyFilter();

    @StudioElement(
            name = "DataGridFilter",
            classFqn = "io.jmix.flowui.facet.urlqueryparameters.DataGridFilterUrlQueryParametersBinder",
            xmlElement = StudioXmlElements.DATA_GRID_FILTER,
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            propertyGroups = StudioPropertyGroups.DataGridFilterComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COMPONENT, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_REF, componentRefTags = {"dataGrid", "treeDataGrid", "groupDataGrid"}, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PARAM, type = StudioPropertyType.STRING),
            }
    )
    void dataGridFilter();

    @StudioElement(
            name = "ResponsiveStep",
            classFqn = "com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep",
            xmlElement = StudioXmlElements.RESPONSIVE_STEP,
            documentationLink = "%VERSION%/flow-ui/vc/layouts/formLayout.html#responsive-steps",
            propertyGroups = StudioPropertyGroups.FormLayoutResponsiveStepComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLUMNS, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABELS_POSITION, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.formlayout.FormLayout$ResponsiveStep$LabelsPosition",
                            options = {"ASIDE", "TOP"}, defaultValue = "TOP")
            }
    )
    FormLayout.ResponsiveStep formLayoutResponsiveStep();

    @StudioElement(
            name = "FormItem",
            classFqn = "com.vaadin.flow.component.formlayout.FormLayout.FormItem",
            xmlElement = StudioXmlElements.FORM_ITEM,
            target = {"com.vaadin.flow.component.formlayout.FormLayout"},
            visible = true,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            ),
            propertyGroups = StudioPropertyGroups.FormItemDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABEL, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    FormLayout.FormItem formItem();

    @StudioElement(
            name = "FormRow",
            classFqn = "com.vaadin.flow.component.formlayout.FormLayout.FormRow",
            xmlElement = StudioXmlElements.FORM_ROW,
            target = {"com.vaadin.flow.component.formlayout.FormLayout"},
            visible = true
    )
    FormLayout.FormRow formRow();

    @StudioElement(
            name = "ResponsiveStep",
            classFqn = "io.jmix.flowui.component.SupportsResponsiveSteps.ResponsiveStep",
            xmlElement = StudioXmlElements.RESPONSIVE_STEP,
            propertyGroups = StudioPropertyGroups.ResponsiveStepComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLUMNS, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABELS_POSITION, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.SupportsResponsiveSteps$ResponsiveStep$LabelsPosition",
                            options = {"ASIDE", "TOP"}, defaultValue = "TOP")
            }
    )
    void responsiveStep();

    @StudioElement(
            name = "PropertyFilter",
            xmlElement = StudioXmlElements.PROPERTY_FILTER,
            target = {
                    "io.jmix.flowui.component.logicalfilter.GroupFilter",
                    "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration"
            },
            propertyGroups = StudioPropertyGroups.PropertyFilterDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DEFAULT_VALUE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ERROR_MESSAGE, category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HELPER_TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABEL, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABEL_VISIBLE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OPERATION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.propertyfilter.PropertyFilter$Operation",
                            options = {"EQUAL", "NOT_EQUAL", "GREATER",
                                    "GREATER_OR_EQUAL", "LESS", "LESS_OR_EQUAL", "CONTAINS", "NOT_CONTAINS",
                                    "STARTS_WITH", "ENDS_WITH", "IS_SET", "IN_LIST", "NOT_IN_LIST", "IN_INTERVAL",
                                    "DATE_EQUALS", "IS_COLLECTION_EMPTY", "MEMBER_OF_COLLECTION",
                                    "NOT_MEMBER_OF_COLLECTION"}, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OPERATIONS_LIST, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.VALUES_LIST,
                            options = {"EQUAL", "NOT_EQUAL", "GREATER",
                                    "GREATER_OR_EQUAL", "LESS", "LESS_OR_EQUAL", "CONTAINS", "NOT_CONTAINS",
                                    "STARTS_WITH", "ENDS_WITH", "IS_SET", "IN_LIST", "NOT_IN_LIST", "IN_INTERVAL",
                                    "DATE_EQUALS", "IS_COLLECTION_EMPTY", "MEMBER_OF_COLLECTION",
                                    "NOT_MEMBER_OF_COLLECTION"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OPERATION_EDITABLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OPERATION_TEXT_VISIBLE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PARAMETER_NAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V", required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.READ_ONLY, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.REQUIRED, category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.REQUIRED_MESSAGE, category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TAB_INDEX, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FOCUS_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    void propertyFilter();

    @StudioElement(
            name = "JpqlFilter",
            classFqn = "io.jmix.flowui.component.jpqlfilter.JpqlFilter",
            xmlElement = StudioXmlElements.JPQL_FILTER,
            target = {
                    "io.jmix.flowui.component.logicalfilter.GroupFilter",
                    "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration"
            },
            propertyGroups = StudioPropertyGroups.JpqlFilterDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DEFAULT_VALUE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ERROR_MESSAGE, category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HAS_IN_EXPRESSION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HELPER_TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABEL, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABEL_VISIBLE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PARAMETER_CLASS, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING,
                            typeParameter = "V", required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PARAMETER_NAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.READ_ONLY, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.REQUIRED, category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.REQUIRED_MESSAGE, category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TAB_INDEX, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FOCUS_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    void jpqlFilter();

    @StudioElement(
            name = "Condition",
            classFqn = "io.jmix.flowui.kit.component.stub.JpqlFilterCondition",
            xmlElement = StudioXmlElements.CONDITION,
            target = {"io.jmix.flowui.component.jpqlfilter.JpqlFilter"},
            unlimitedCount = false
    )
    void jpqlFilterCondition();

    @StudioElement(
            name = "Jpql",
            classFqn = "io.jmix.flowui.kit.component.stub.JpqlFilterJpql",
            xmlns = "http://jmix.io/schema/flowui/jpql-condition",
            xmlElement = StudioXmlElements.JPQL,
            xmlnsAlias = "c",
            target = {"io.jmix.flowui.kit.component.stub.JpqlFilterCondition"},
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.JpqlFilterConditionJpqlComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JOIN, type = StudioPropertyType.JPQL_FILTER_JOIN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHERE, type = StudioPropertyType.JPQL_FILTER_WHERE),
            }
    )
    void jpqlFilterConditionJpql();

    @StudioElement(
            name = "GroupFilter",
            classFqn = "io.jmix.flowui.component.logicalfilter.GroupFilter",
            xmlElement = StudioXmlElements.GROUP_FILTER,
            target = {
                    "io.jmix.flowui.component.logicalfilter.GroupFilter",
                    "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration"
            },
            propertyGroups = StudioPropertyGroups.GroupFilterElementComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OPERATION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.logicalfilter.LogicalFilterComponent$Operation",
                            options = {"AND", "OR"}, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OPERATION_TEXT_VISIBLE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SUMMARY_TEXT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
            }
    )
    void groupFilter();

    @StudioElement(
            name = "Configuration",
            classFqn = "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration",
            xmlElement = StudioXmlElements.CONFIGURATION,
            icon = "io/jmix/flowui/kit/meta/icon/element/configuration.svg",
            propertyGroups = StudioPropertyGroups.ConfigurationComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DEFAULT, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.NAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OPERATION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.logicalfilter.LogicalFilterComponent$Operation",
                            options = {"AND", "OR"}, defaultValue = "AND"),
            }
    )
    void configuration();

    @StudioElement(
            name = "Properties",
            classFqn = "io.jmix.flowui.component.genericfilter.inspector.FilterPropertiesInspector",
            xmlElement = StudioXmlElements.PROPERTIES,
            icon = "io/jmix/flowui/kit/meta/icon/element/property.svg",
            target = {"io.jmix.flowui.component.genericfilter.GenericFilter"},
            propertyGroups = StudioPropertyGroups.PropertiesComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.INCLUDE, type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EXCLUDE, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EXCLUDE_PROPERTIES, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EXCLUDE_RECURSIVELY, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false")
            }
    )
    void properties();

    @StudioElement(
            name = "ItemsQuery",
            xmlElement = StudioXmlElements.ITEMS_QUERY,
            icon = "io/jmix/flowui/kit/meta/icon/element/itemsQuery.svg",
            unlimitedCount = false,
            target = {"io.jmix.flowui.component.combobox.JmixComboBox"},
            documentationLink = "2.1/whats-new/index.html#fetching-items-in-dropdowns",
            unsupportedTarget = {
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker"
            },
            propertyGroups = {
                    StudioPropertyGroups.BaseComboBoxItemsQuery.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SEARCH_STRING_FORMAT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ESCAPE_VALUE_FOR_LIKE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.QUERY, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.JPA_QUERY)
            }
    )
    void valueItemsQuery();

    @StudioElement(
            name = "ItemsQuery",
            xmlElement = StudioXmlElements.ITEMS_QUERY,
            icon = "io/jmix/flowui/kit/meta/icon/element/itemsQuery.svg",
            unlimitedCount = false,
            target = {"io.jmix.flowui.component.combobox.EntityComboBox"},
            documentationLink = "2.1/whats-new/index.html#fetching-items-in-dropdowns",
            unsupportedTarget = {
                    "io.jmix.flowui.component.combobox.JmixComboBox",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker"
            },
            propertyGroups = {
                    StudioPropertyGroups.RequiredEntityClass.class,
                    StudioPropertyGroups.BaseComboBoxItemsQuery.class,
                    StudioPropertyGroups.FetchPlan.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENTITY_CLASS, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SEARCH_STRING_FORMAT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ESCAPE_VALUE_FOR_LIKE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.QUERY, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.JPA_QUERY),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FETCH_PLAN, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.FETCH_PLAN)
            }
    )
    void entityItemsQuery();

    @StudioElement(
            name = "ItemsQuery",
            xmlElement = StudioXmlElements.ITEMS_QUERY,
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
            propertyGroups = {
                    StudioPropertyGroups.EntityClass.class,
                    StudioPropertyGroups.BaseComboBoxItemsQuery.class,
                    StudioPropertyGroups.FetchPlan.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENTITY_CLASS),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SEARCH_STRING_FORMAT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ESCAPE_VALUE_FOR_LIKE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.QUERY, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.JPA_QUERY),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FETCH_PLAN, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.FETCH_PLAN)
            }
    )
    void itemsQuery();

    @StudioElement(
            name = "Prefix",
            xmlElement = StudioXmlElements.PREFIX,
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
            name = "DropdownIcon",
            xmlElement = StudioXmlElements.DROPDOWN_ICON,
            classFqn = "io.jmix.flowui.kit.component.stub.DropdownIconElement",
            target = "io.jmix.flowui.kit.component.combobutton.ComboButton",
            unlimitedCount = false,
            isInjectable = false,
            injectionIdentifier = StudioComponent.EMPTY_INJECTION_IDENTIFIER,
            availableChildren = @StudioAvailableChildrenInfo(
                    totalChildrenCount = 1,
                    availableTags = {
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "image", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "icon", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "svgIcon", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "fontIcon", maxCount = 1)
                    }
            )
    )
    void dropdownIcon();

    @StudioElement(
            name = "UploadIcon",
            xmlElement = StudioXmlElements.UPLOAD_ICON,
            classFqn = "io.jmix.flowui.kit.component.stub.UploadIconElement",
            target = {
                    "io.jmix.flowui.component.upload.JmixUpload",
                    "io.jmix.flowui.component.upload.FileUploadField",
                    "io.jmix.flowui.component.upload.FileStorageUploadField",
                    "io.jmix.webdavflowui.component.WebdavDocumentUploadField"},
            unlimitedCount = false,
            isInjectable = false,
            injectionIdentifier = StudioComponent.EMPTY_INJECTION_IDENTIFIER,
            availableChildren = @StudioAvailableChildrenInfo(
                    totalChildrenCount = 1,
                    availableTags = {
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "image", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "icon", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "svgIcon", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "fontIcon", maxCount = 1)
                    }
            )
    )
    void uploadIcon();

    @StudioElement(
            name = "DropLabelIcon",
            xmlElement = StudioXmlElements.DROP_LABEL_ICON,
            classFqn = "io.jmix.flowui.kit.component.stub.DropLabelIconElement",
            target = "io.jmix.flowui.component.upload.JmixUpload",
            unlimitedCount = false,
            isInjectable = false,
            injectionIdentifier = StudioComponent.EMPTY_INJECTION_IDENTIFIER,
            availableChildren = @StudioAvailableChildrenInfo(
                    totalChildrenCount = 1,
                    availableTags = {
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "image", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "icon", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "svgIcon", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "fontIcon", maxCount = 1)
                    }
            )
    )
    void dropLabelIcon();

    @StudioElement(
            name = "Icon",
            xmlElement = StudioXmlElements.ICON,
            classFqn = "io.jmix.flowui.kit.component.stub.IconElement",
            target = {
                    "io.jmix.flowui.kit.action.BaseAction",

                    "com.vaadin.flow.component.applayout.DrawerToggle",

                    "io.jmix.flowui.kit.component.button.JmixButton",
                    "io.jmix.flowui.kit.component.combobutton.ComboButton",
                    "io.jmix.flowui.kit.component.dropdownbutton.DropdownButton"},
            unlimitedCount = false,
            isInjectable = false,
            injectionIdentifier = StudioComponent.EMPTY_INJECTION_IDENTIFIER,
            availableChildren = @StudioAvailableChildrenInfo(
                    totalChildrenCount = 1,
                    availableTags = {
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "image", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "icon", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "svgIcon", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "fontIcon", maxCount = 1)
                    }
            )
    )
    void icon();

    @StudioElement(
            name = "Icon",
            classFqn = "com.vaadin.flow.component.icon.Icon",
            xmlElement = StudioXmlElements.ICON,
            target = {
                    "io.jmix.flowui.kit.component.stub.IconElement",
                    "io.jmix.flowui.kit.component.stub.UploadIconElement",
                    "io.jmix.flowui.kit.component.stub.DropdownIconElement",
                    "io.jmix.flowui.kit.component.stub.DropLabelIconElement",
                    "io.jmix.flowui.kit.component.stub.ClearButtonIconElement",
                    "io.jmix.flowui.kit.component.stub.DownloadButtonIconElement"},
            icon = "io/jmix/flowui/kit/meta/icon/component/icon.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/icon.html",
            isInjectable = false,
            propertyGroups = {
                    StudioPropertyGroups.IconDefaultProperties.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLOR, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SIZE, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    Icon nestedIcon();

    @StudioElement(
            name = "SvgIcon",
            classFqn = "com.vaadin.flow.component.icon.SvgIcon",
            xmlElement = StudioXmlElements.SVG_ICON,
            target = {
                    "io.jmix.flowui.kit.component.stub.IconElement",
                    "io.jmix.flowui.kit.component.stub.UploadIconElement",
                    "io.jmix.flowui.kit.component.stub.DropdownIconElement",
                    "io.jmix.flowui.kit.component.stub.DropLabelIconElement",
                    "io.jmix.flowui.kit.component.stub.ClearButtonIconElement",
                    "io.jmix.flowui.kit.component.stub.DownloadButtonIconElement"},
            icon = "io/jmix/flowui/kit/meta/icon/component/svgIcon.svg",
            isInjectable = false,
            propertyGroups = {
                    StudioPropertyGroups.SvgIconDefaultProperties.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLOR, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RESOURCE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SIZE, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SYMBOL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    SvgIcon svgIcon();

    @StudioElement(
            name = "FontIcon",
            classFqn = "com.vaadin.flow.component.icon.FontIcon",
            xmlElement = StudioXmlElements.FONT_ICON,
            target = {
                    "io.jmix.flowui.kit.component.stub.IconElement",
                    "io.jmix.flowui.kit.component.stub.UploadIconElement",
                    "io.jmix.flowui.kit.component.stub.DropdownIconElement",
                    "io.jmix.flowui.kit.component.stub.DropLabelIconElement",
                    "io.jmix.flowui.kit.component.stub.ClearButtonIconElement",
                    "io.jmix.flowui.kit.component.stub.DownloadButtonIconElement"},
            icon = "io/jmix/flowui/kit/meta/icon/component/fontIcon.svg",
            isInjectable = false,
            propertyGroups = {
                    StudioPropertyGroups.FontIconDefaultProperties.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CHAR_CODE, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLOR, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FONT_FAMILY, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON_CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIGATURE, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SIZE, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    FontIcon fontIcon();

    @StudioElement(
            name = "Image",
            classFqn = "io.jmix.flowui.component.image.JmixImage",
            xmlElement = StudioXmlElements.IMAGE,
            target = {
                    "io.jmix.flowui.kit.component.stub.IconElement",
                    "io.jmix.flowui.kit.component.stub.UploadIconElement",
                    "io.jmix.flowui.kit.component.stub.DropdownIconElement",
                    "io.jmix.flowui.kit.component.stub.DropLabelIconElement",
                    "io.jmix.flowui.kit.component.stub.ClearButtonIconElement",
                    "io.jmix.flowui.kit.component.stub.DownloadButtonIconElement"},
            icon = "io/jmix/flowui/kit/meta/icon/html/image.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html-components/image.html",
            isInjectable = false,
            propertyGroups = StudioPropertyGroups.ImageHtmlComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALTERNATE_TEXT, type = StudioPropertyType.LOCALIZED_STRING,
                            setMethod = "setAlt"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLICK_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PROPERTY, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RESOURCE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING,
                            setMethod = "setSrc"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"fill", "contain", "cover", "scale-down"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Image image();

    @StudioElement(
            name = "Suffix",
            xmlElement = StudioXmlElements.SUFFIX,
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
            xmlElement = StudioXmlElements.COMPONENT,
            target = "io.jmix.flowui.facet.SettingsFacet",
            propertyGroups = StudioPropertyGroups.SettingsFacetComponentComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COMPONENT_ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN)
            },
            isInjectable = false,
            injectionIdentifier = StudioComponent.EMPTY_INJECTION_IDENTIFIER
    )
    void settingsFacetComponent();

    @StudioElement(
            name = "MenuItem",
            xmlElement = StudioXmlElements.MENU_ITEM,
            target = {"io.jmix.flowui.component.gridcolumnvisibility.JmixGridColumnVisibility"},
            propertyGroups = StudioPropertyGroups.GridColumnVisibilityMenuItemComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.REF_COLUMN, type = StudioPropertyType.COMPONENT_REF,
                            componentRefTags = {"column"}, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    void gridColumnVisibilityMenuItem();

    @StudioElement(
            name = "ContextMenu",
            icon = "io/jmix/flowui/kit/meta/icon/element/contextMenu.svg",
            xmlElement = StudioXmlElements.CONTEXT_MENU,
            classFqn = "io.jmix.flowui.kit.component.grid.JmixGridContextMenu",
            unlimitedCount = false,
            target = {"io.jmix.flowui.component.grid.DataGrid", "io.jmix.flowui.component.grid.TreeDataGrid"},
            propertyGroups = {
                    StudioPropertyGroups.ClassNamesAndCss.class,
                    StudioPropertyGroups.IdAndVisible.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    void gridContextMenu();

    @StudioElement(
            name = "EmptyStateComponent",
            xmlElement = StudioXmlElements.EMPTY_STATE_COMPONENT,
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
            xmlElement = StudioXmlElements.ITEM,
            classFqn = "com.vaadin.flow.component.grid.contextmenu.GridMenuItem",
            target = {"io.jmix.flowui.kit.component.grid.JmixGridContextMenu",
                    "com.vaadin.flow.component.grid.contextmenu.GridMenuItem"},
            propertyGroups = StudioPropertyGroups.GridContextMenuItemComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ACTION, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ACTION_REF,
                            classFqn = "io.jmix.flowui.kit.action.Action"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ICON, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WHITE_SPACE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"})
            }
    )
    void gridContextMenuItem();

    @StudioElement(
            name = "Separator",
            icon = "io/jmix/flowui/kit/meta/icon/element/contextMenuSeparator.svg",
            xmlElement = StudioXmlElements.SEPARATOR,
            target = {"io.jmix.flowui.kit.component.grid.JmixGridContextMenu",
                    "com.vaadin.flow.component.grid.contextmenu.GridMenuItem"}
    )
    void gridContextMenuSeparator();

    @StudioElement(
            name = "Content",
            icon = "io/jmix/flowui/kit/meta/icon/view/layout.svg",
            xmlElement = StudioXmlElements.CONTENT,
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
            xmlElement = StudioXmlElements.PROPERTY,
            icon = "io/jmix/flowui/kit/meta/icon/element/property.svg",
            propertyGroups = StudioPropertyGroups.RequiredStringNameAndValueAndType.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.NAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TYPE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            options = {"CONTAINER_REF", "LOADER_REF", "ICON"})
            }
    )
    void fragmentProperty();

    @StudioElement(
            name = "Property",
            classFqn = "io.jmix.flowui.kit.stub.StudioGenericComponentPropertyElement",
            icon = "io/jmix/flowui/kit/meta/icon/element/property.svg",
            xmlElement = StudioXmlElements.PROPERTY,
            propertyGroups = StudioPropertyGroups.RequiredStringNameAndValueAndType.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.NAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TYPE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            options = {"CONTAINER_REF", "LOADER_REF", "ICON"})
            }
    )
    void genericComponentProperty();

    @StudioElement(
            name = "FragmentRenderer",
            classFqn = "io.jmix.flowui.kit.stub.StudioFragmentRenderer",
            icon = "io/jmix/flowui/kit/meta/icon/element/fragmentRenderer.svg",
            xmlElement = StudioXmlElements.FRAGMENT_RENDERER,
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
                    "io.jmix.flowui.component.listbox.JmixMultiSelectListBox",
                    "io.jmix.flowui.component.gridlayout.GridLayout"
            },
            unlimitedCount = false,
            isInjectable = false,
            propertyGroups = {
                    StudioPropertyGroups.RequiredFragmentClass.class
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.FRAGMENT_CLASS, required = true)
            }
    )
    void fragmentRenderer();

    @StudioElement(
            name = "StartSlot",
            icon = "io/jmix/flowui/kit/meta/icon/element/column.svg",
            xmlElement = StudioXmlElements.START_SLOT,
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
            xmlElement = StudioXmlElements.MIDDLE_SLOT,
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
            xmlElement = StudioXmlElements.END_SLOT,
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

    @StudioElement(
            name = "Title",
            xmlElement = StudioXmlElements.TITLE,
            target = "io.jmix.flowui.component.card.JmixCard",
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            )
    )
    void cardTitle();

    @StudioElement(
            name = "Subtitle",
            xmlElement = StudioXmlElements.SUBTITLE,
            target = "io.jmix.flowui.component.card.JmixCard",
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            )
    )
    void cardSubtitle();

    @StudioElement(
            name = "Media",
            xmlElement = StudioXmlElements.MEDIA,
            target = "io.jmix.flowui.component.card.JmixCard",
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            )
    )
    void cardMedia();

    @StudioElement(
            name = "Content",
            xmlElement = StudioXmlElements.CONTENT,
            target = "io.jmix.flowui.component.card.JmixCard",
            unlimitedCount = false,
            visible = true,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 99999
                    )
            )
    )
    Div cardContent();

    @StudioElement(
            name = "HeaderPrefix",
            xmlElement = StudioXmlElements.HEADER_PREFIX,
            target = "io.jmix.flowui.component.card.JmixCard",
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            )
    )
    void cardHeaderPrefix();

    @StudioElement(
            name = "Header",
            xmlElement = StudioXmlElements.HEADER,
            target = "io.jmix.flowui.component.card.JmixCard",
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            )
    )
    void cardHeader();

    @StudioElement(
            name = "HeaderSuffix",
            xmlElement = StudioXmlElements.HEADER_SUFFIX,
            target = "io.jmix.flowui.component.card.JmixCard",
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 1
                    )
            )
    )
    void cardHeaderSuffix();

    @StudioElement(
            name = "Footer",
            xmlElement = StudioXmlElements.FOOTER,
            target = "io.jmix.flowui.component.card.JmixCard",
            unlimitedCount = false,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableClasses = @StudioAvailableChildrenInfo.ClassInfo(
                            qualifiedName = StudioAvailableChildrenInfo.FLOW_COMPONENT_FQN,
                            maxCount = 9999
                    )
            )
    )
    void cardFooter();

    @StudioElement(
            name = "Markdown Content",
            xmlElement = StudioXmlElements.CONTENT,
            target = "com.vaadin.flow.component.markdown.Markdown"
    )
    void markdownContent();
}
