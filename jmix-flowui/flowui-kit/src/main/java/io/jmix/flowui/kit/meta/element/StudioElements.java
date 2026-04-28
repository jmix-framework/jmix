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
            propertyGroups = StudioPropertyGroups.AccordionPanelDefaultProperties.class)
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
            propertyGroups = StudioPropertyGroups.ShortcutCombinationComponent.class)
    void shortcutCombination();

    @StudioElement(
            name = "ActionItem",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.ActionItem",
            xmlElement = StudioXmlElements.ACTION_ITEM,
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html#actionItem",
            isInjectable = false,
            propertyGroups = StudioPropertyGroups.ActionItemComponent.class)
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
            })
    void dropdownButtonAction();

    @StudioElement(
            name = "AdditionalInformation",
            xmlElement = StudioXmlElements.ADDITIONAL_INFORMATION,
            target = {"com.vaadin.flow.component.login.AbstractLogin"},
            unlimitedCount = false,
            propertyGroups = {
                    StudioPropertyGroups.Message.class
            })
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
            propertyGroups = StudioPropertyGroups.EditorActionsColumnDefaultProperties.class)
    void editorActionsColumn();

    @StudioElement(
            name = "EditButton",
            xmlElement = StudioXmlElements.EDIT_BUTTON,
            target = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.EditButtonComponent.class)
    void editButton();

    @StudioElement(
            name = "SaveButton",
            xmlElement = StudioXmlElements.SAVE_BUTTON,
            target = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.SaveButtonComponent.class)
    void saveButton();

    @StudioElement(
            name = "CloseButton",
            xmlElement = StudioXmlElements.CLOSE_BUTTON,
            target = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.CloseButtonComponent.class)
    void closeButton();

    @StudioElement(
            name = "CancelButton",
            xmlElement = StudioXmlElements.CANCEL_BUTTON,
            target = "io.jmix.flowui.kit.component.grid.EditorActionsColumn",
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.CancelButtonComponent.class)
    void cancelButton();

    @StudioElement(
            name = "Aggregation",
            classFqn = "io.jmix.flowui.component.AggregationInfo",
            xmlElement = StudioXmlElements.AGGREGATION,
            target = {"com.vaadin.flow.component.grid.Grid.Column"},
            unsupportedTarget = {"io.jmix.flowui.kit.component.grid.EditorActionsColumn"},
            propertyGroups = StudioPropertyGroups.AggregationInfoComponent.class)
    void aggregationInfo();

    @StudioElement(
            name = "LocalDateRenderer",
            classFqn = "com.vaadin.flow.data.renderer.LocalDateRenderer",
            xmlElement = StudioXmlElements.LOCAL_DATE_RENDERER,
            target = {"com.vaadin.flow.component.grid.Grid.Column"},
            unsupportedTarget = {"io.jmix.flowui.kit.component.grid.EditorActionsColumn"},
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#renderers",
            propertyGroups = StudioPropertyGroups.FormatAndNullRepresentation.class)
    void localDateRenderer();

    @StudioElement(
            name = "LocalDateTimeRenderer",
            classFqn = "com.vaadin.flow.data.renderer.LocalDateTimeRenderer",
            xmlElement = StudioXmlElements.LOCAL_DATE_TIME_RENDERER,
            target = {"com.vaadin.flow.component.grid.Grid.Column"},
            unsupportedTarget = {"io.jmix.flowui.kit.component.grid.EditorActionsColumn"},
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#renderers",
            propertyGroups = StudioPropertyGroups.FormatAndNullRepresentation.class)
    void localDateTimeRenderer();

    @StudioElement(
            name = "NumberRenderer",
            classFqn = "com.vaadin.flow.data.renderer.NumberRenderer",
            xmlElement = StudioXmlElements.NUMBER_RENDERER,
            target = {"com.vaadin.flow.component.grid.Grid.Column"},
            unsupportedTarget = {"io.jmix.flowui.kit.component.grid.EditorActionsColumn"},
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html#renderers",
            propertyGroups = StudioPropertyGroups.FormatAndNullRepresentation.class)
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
            })
    DropdownButtonItem componentItem();

    @StudioElement(
            name = "ErrorMessage",
            classFqn = "com.vaadin.flow.component.login.LoginI18n.ErrorMessage",
            xmlElement = StudioXmlElements.ERROR_MESSAGE,
            unlimitedCount = false,
            target = {"com.vaadin.flow.component.login.AbstractLogin"},
            propertyGroups = StudioPropertyGroups.LoginErrorMessageDefaultProperties.class)
    LoginI18n.ErrorMessage loginErrorMessage();

    @StudioElement(
            name = "Form",
            classFqn = "io.jmix.flowui.kit.component.loginform.JmixLoginI18n.JmixForm",
            xmlElement = StudioXmlElements.FORM,
            unlimitedCount = false,
            target = {"io.jmix.flowui.kit.component.loginform.EnhancedLoginForm"},
            propertyGroups = StudioPropertyGroups.JmixLoginFormComponent.class)
    JmixLoginI18n.JmixForm jmixLoginForm();

    @StudioElement(
            name = "Form",
            classFqn = "com.vaadin.flow.component.login.LoginI18n.Form",
            xmlElement = StudioXmlElements.FORM,
            target = {"com.vaadin.flow.component.login.LoginOverlay"},
            unlimitedCount = false,
            propertyGroups = StudioPropertyGroups.LoginFormDefaultProperties.class)
    LoginI18n.Form loginForm();

    @StudioElement(
            name = "Header",
            xmlElement = StudioXmlElements.HEADER,
            target = {"com.vaadin.flow.component.login.LoginOverlay"},
            unlimitedCount = false,
            propertyGroups = {
                    StudioPropertyGroups.Title.class,
                    StudioPropertyGroups.Description.class
            })
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
            propertyGroups = StudioPropertyGroups.GenericFilterElementComponent.class)
    void genericFilter();

    @StudioElement(
            name = "Tab",
            classFqn = "com.vaadin.flow.component.tabs.Tab",
            target = {"com.vaadin.flow.component.tabs.Tabs", "io.jmix.flowui.component.tabsheet.JmixTabSheet"},
            xmlElement = StudioXmlElements.TAB,
            icon = "io/jmix/flowui/kit/meta/icon/element/tab.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/tabs.html#tab",
            visible = true,
            propertyGroups = StudioPropertyGroups.TabComponent.class)
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
            })
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
            propertyGroups = StudioPropertyGroups.TextUserItemUserMenuItemComponent.class)
    UserMenuItem textUserItemUserMenuItem();

    @StudioElement(
            name = "ActionItem",
            classFqn = "io.jmix.flowui.kit.component.usermenu.ActionUserMenuItem",
            xmlElement = StudioXmlElements.ACTION_ITEM,
            documentationLink = "%VERSION%/flow-ui/vc/components/userMenu.html#actionItem",
            isInjectable = false,
            propertyGroups = StudioPropertyGroups.ActionUserMenuItemComponent.class)
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
            })
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
            propertyGroups = StudioPropertyGroups.ComponentUserMenuItemComponent.class)
    UserMenuItem componentUserMenuItem();

    @StudioElement(
            name = "ViewItem",
            classFqn = "io.jmix.flowui.component.usermenu.ViewUserMenuItem",
            xmlElement = StudioXmlElements.VIEW_ITEM,
            isInjectable = false,
            documentationLink = "%VERSION%/flow-ui/vc/components/userMenu.html#viewItem",
            propertyGroups = StudioPropertyGroups.ViewUserItemUserMenuItemComponent.class)
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
            propertyGroups = StudioPropertyGroups.TooltipComponent.class)
    Tooltip tooltip();

    @StudioElement(
            name = "Pagination",
            classFqn = "io.jmix.flowui.facet.queryparameters.PaginationQueryParametersBinder",
            xmlElement = StudioXmlElements.PAGINATION,
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            icon = "io/jmix/flowui/kit/meta/icon/element/pagination.svg",
            propertyGroups = StudioPropertyGroups.PaginationComponent.class)
    void pagination();

    @StudioElement(
            name = "PropertyFilter",
            classFqn = "io.jmix.flowui.facet.queryparameters.PropertyFilterQueryParametersBinder",
            xmlElement = StudioXmlElements.PROPERTY_FILTER,
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            icon = "io/jmix/flowui/kit/meta/icon/element/filter.svg",
            propertyGroups = StudioPropertyGroups.FacetPropertyFilterComponent.class)
    void facetPropertyFilter();

    @StudioElement(
            name = "DataGridFilter",
            classFqn = "io.jmix.flowui.facet.urlqueryparameters.DataGridFilterUrlQueryParametersBinder",
            xmlElement = StudioXmlElements.DATA_GRID_FILTER,
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            propertyGroups = StudioPropertyGroups.DataGridFilterComponent.class)
    void dataGridFilter();

    @StudioElement(
            name = "ResponsiveStep",
            classFqn = "com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep",
            xmlElement = StudioXmlElements.RESPONSIVE_STEP,
            documentationLink = "%VERSION%/flow-ui/vc/layouts/formLayout.html#responsive-steps",
            propertyGroups = StudioPropertyGroups.FormLayoutResponsiveStepComponent.class)
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
            propertyGroups = StudioPropertyGroups.FormItemDefaultProperties.class)
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
            propertyGroups = StudioPropertyGroups.ResponsiveStepComponent.class)
    void responsiveStep();

    @StudioElement(
            name = "PropertyFilter",
            xmlElement = StudioXmlElements.PROPERTY_FILTER,
            target = {
                    "io.jmix.flowui.component.logicalfilter.GroupFilter",
                    "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration"
            },
            propertyGroups = StudioPropertyGroups.PropertyFilterDefaultProperties.class)
    void propertyFilter();

    @StudioElement(
            name = "JpqlFilter",
            classFqn = "io.jmix.flowui.component.jpqlfilter.JpqlFilter",
            xmlElement = StudioXmlElements.JPQL_FILTER,
            target = {
                    "io.jmix.flowui.component.logicalfilter.GroupFilter",
                    "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration"
            },
            propertyGroups = StudioPropertyGroups.JpqlFilterDefaultProperties.class)
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
            propertyGroups = StudioPropertyGroups.JpqlFilterConditionJpqlComponent.class)
    void jpqlFilterConditionJpql();

    @StudioElement(
            name = "GroupFilter",
            classFqn = "io.jmix.flowui.component.logicalfilter.GroupFilter",
            xmlElement = StudioXmlElements.GROUP_FILTER,
            target = {
                    "io.jmix.flowui.component.logicalfilter.GroupFilter",
                    "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration"
            },
            propertyGroups = StudioPropertyGroups.GroupFilterElementComponent.class)
    void groupFilter();

    @StudioElement(
            name = "Configuration",
            classFqn = "io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration",
            xmlElement = StudioXmlElements.CONFIGURATION,
            icon = "io/jmix/flowui/kit/meta/icon/element/configuration.svg",
            propertyGroups = StudioPropertyGroups.ConfigurationComponent.class)
    void configuration();

    @StudioElement(
            name = "Properties",
            classFqn = "io.jmix.flowui.component.genericfilter.inspector.FilterPropertiesInspector",
            xmlElement = StudioXmlElements.PROPERTIES,
            icon = "io/jmix/flowui/kit/meta/icon/element/property.svg",
            target = {"io.jmix.flowui.component.genericfilter.GenericFilter"},
            propertyGroups = StudioPropertyGroups.PropertiesComponent.class)
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
            })
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
            })
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
            })
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
            })
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
            })
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
            })
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
            isInjectable = false,
            injectionIdentifier = StudioComponent.EMPTY_INJECTION_IDENTIFIER
    )
    void settingsFacetComponent();

    @StudioElement(
            name = "MenuItem",
            xmlElement = StudioXmlElements.MENU_ITEM,
            target = {"io.jmix.flowui.component.gridcolumnvisibility.JmixGridColumnVisibility"},
            propertyGroups = StudioPropertyGroups.GridColumnVisibilityMenuItemComponent.class)
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
            })
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
            propertyGroups = StudioPropertyGroups.GridContextMenuItemComponent.class)
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
            propertyGroups = StudioPropertyGroups.RequiredStringNameAndValueAndType.class)
    void fragmentProperty();

    @StudioElement(
            name = "Property",
            classFqn = "io.jmix.flowui.kit.stub.StudioGenericComponentPropertyElement",
            icon = "io/jmix/flowui/kit/meta/icon/element/property.svg",
            xmlElement = StudioXmlElements.PROPERTY,
            propertyGroups = StudioPropertyGroups.RequiredStringNameAndValueAndType.class)
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
            })
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
