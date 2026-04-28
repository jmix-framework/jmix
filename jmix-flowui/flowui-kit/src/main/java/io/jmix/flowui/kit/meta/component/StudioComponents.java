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

package io.jmix.flowui.kit.meta.component;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.icon.FontIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.virtuallist.VirtualList;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.checkbox.JmixSwitch;
import io.jmix.flowui.kit.component.codeeditor.JmixCodeEditor;
import io.jmix.flowui.kit.component.combobox.ComboBoxPicker;
import io.jmix.flowui.kit.component.combobutton.ComboButton;
import io.jmix.flowui.kit.component.sidepanellayout.JmixSidePanelLayoutCloser;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.kit.component.grid.JmixTreeGrid;
import io.jmix.flowui.kit.component.loginform.EnhancedLoginForm;
import io.jmix.flowui.kit.component.main.ListMenu;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.multiselectcomboboxpicker.MultiSelectComboBoxPicker;
import io.jmix.flowui.kit.component.pagination.JmixSimplePagination;
import io.jmix.flowui.kit.component.richtexteditor.JmixRichTextEditor;
import io.jmix.flowui.kit.component.twincolumn.JmixTwinColumn;
import io.jmix.flowui.kit.component.upload.JmixFileStorageUploadField;
import io.jmix.flowui.kit.component.upload.JmixFileUploadField;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenu;
import io.jmix.flowui.kit.component.valuepicker.MultiValuePicker;
import io.jmix.flowui.kit.component.valuepicker.ValuePicker;
import io.jmix.flowui.kit.meta.*;
import io.jmix.flowui.kit.meta.component.preview.StudioGenericComponentPreview;

@StudioUiKit
@SuppressWarnings("rawtypes")
interface StudioComponents {

    @StudioComponent(
            name = "GenericComponent",
            classFqn = "com.vaadin.flow.component.Component",
            category = "Components",
            xmlElement = StudioXmlElements.COMPONENT,
            propertyGroups = StudioPropertyGroups.GenericComponentComponent.class)
    StudioGenericComponentPreview genericComponent();

    @StudioComponent(
            name = "Fragment",
            classFqn = "io.jmix.flowui.fragment.Fragment",
            category = "Components",
            xmlElement = StudioXmlElements.FRAGMENT,
            icon = "io/jmix/flowui/kit/meta/icon/component/fragment.svg",
            propertyGroups = StudioPropertyGroups.FragmentComponent.class)
    VerticalLayout fragment();

    @StudioComponent(
            name = "Avatar",
            classFqn = "com.vaadin.flow.component.avatar.Avatar",
            category = "Components",
            xmlElement = StudioXmlElements.AVATAR,
            icon = "io/jmix/flowui/kit/meta/icon/component/avatar.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/avatar.html",
            propertyGroups = StudioPropertyGroups.AvatarComponent.class)
    Avatar avatar();

    @StudioComponent(
            name = "Icon",
            classFqn = "com.vaadin.flow.component.icon.Icon",
            category = "Components",
            xmlElement = StudioXmlElements.ICON,
            icon = "io/jmix/flowui/kit/meta/icon/component/icon.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/icon.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "svgIcon"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "fontIcon"),
            }),
            propertyGroups = {
                    StudioPropertyGroups.IconDefaultProperties.class
            })
    Icon icon();

    @StudioComponent(
            name = "SvgIcon",
            classFqn = "com.vaadin.flow.component.icon.SvgIcon",
            category = "Components",
            xmlElement = StudioXmlElements.SVG_ICON,
            icon = "io/jmix/flowui/kit/meta/icon/component/svgIcon.svg",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "icon"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "fontIcon")
            }),
            propertyGroups = {
                    StudioPropertyGroups.SvgIconDefaultProperties.class
            })
    SvgIcon svgIcon();

    @StudioComponent(
            name = "FontIcon",
            classFqn = "com.vaadin.flow.component.icon.FontIcon",
            category = "Components",
            xmlElement = StudioXmlElements.FONT_ICON,
            icon = "io/jmix/flowui/kit/meta/icon/component/fontIcon.svg",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "icon"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "svgIcon")
            }),
            propertyGroups = {
                    StudioPropertyGroups.FontIconDefaultProperties.class
            })
    FontIcon fontIcon();

    @StudioComponent(
            name = "BigDecimalField",
            classFqn = "io.jmix.flowui.component.textfield.JmixBigDecimalField",
            category = "Components",
            xmlElement = StudioXmlElements.BIG_DECIMAL_FIELD,
            icon = "io/jmix/flowui/kit/meta/icon/component/bigDecimalField.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/bigDecimalField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "numberField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "integerField"),
            }),
            propertyGroups = {
                    StudioPropertyGroups.TextInputFieldDefaultProperties.class,
                    StudioPropertyGroups.Property.class
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    BigDecimalField bigDecimalField();

    @StudioComponent(
            name = "Button",
            classFqn = "io.jmix.flowui.kit.component.button.JmixButton",
            category = "Components",
            xmlElement = StudioXmlElements.BUTTON,
            icon = "io/jmix/flowui/kit/meta/icon/component/button.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/button.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "comboButton")
            }),
            propertyGroups = StudioPropertyGroups.ButtonComponent.class)
    JmixButton button();

    @StudioComponent(
            name = "CheckboxGroup",
            classFqn = "io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup",
            category = "Components",
            xmlElement = StudioXmlElements.CHECKBOX_GROUP,
            icon = "io/jmix/flowui/kit/meta/icon/component/checkBoxGroup.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/checkBoxGroup.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "radioButtonGroup")
            }),
            propertyGroups = StudioPropertyGroups.CheckboxGroupComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            },
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.ComponentRenderer"
                    )
            }
    )
    CheckboxGroup checkboxGroup();

    @StudioComponent(
            name = "Checkbox",
            classFqn = "io.jmix.flowui.component.checkbox.JmixCheckbox",
            category = "Components",
            xmlElement = StudioXmlElements.CHECKBOX,
            icon = "io/jmix/flowui/kit/meta/icon/component/checkbox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/checkbox.html",
            propertyGroups = StudioPropertyGroups.CheckboxComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Checkbox checkbox();

    @StudioComponent(
            name = "Switch",
            classFqn = "io.jmix.flowui.component.checkbox.Switch",
            category = "Components",
            xmlElement = StudioXmlElements.SWITCH,
            icon = "io/jmix/flowui/kit/meta/icon/component/checkbox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/switch.html",
            propertyGroups = StudioPropertyGroups.SwitchComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    JmixSwitch switch_();

    @StudioComponent(
            name = "ComboBox",
            classFqn = "io.jmix.flowui.component.combobox.JmixComboBox",
            category = "Components",
            xmlElement = StudioXmlElements.COMBO_BOX,
            icon = "io/jmix/flowui/kit/meta/icon/component/comboBox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/comboBox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "select"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiSelectComboBox")
            }),
            propertyGroups = StudioPropertyGroups.ComboBoxComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            },
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.Renderer"
                    )
            }
    )
    ComboBox comboBox();

    @StudioComponent(
            name = "MultiSelectComboBox",
            classFqn = "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
            category = "Components",
            xmlElement = StudioXmlElements.MULTI_SELECT_COMBO_BOX,
            icon = "io/jmix/flowui/kit/meta/icon/component/comboBox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/multiSelectComboBox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "select"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "comboBox")
            }),
            propertyGroups = StudioPropertyGroups.MultiSelectComboBoxComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            },
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.Renderer"
                    )
            }
    )
    MultiSelectComboBox multiSelectComboBox();

    @StudioComponent(
            name = "MultiSelectComboBoxPicker",
            classFqn = "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
            category = "Components",
            xmlElement = StudioXmlElements.MULTI_SELECT_COMBO_BOX_PICKER,
            icon = "io/jmix/flowui/kit/meta/icon/component/comboBox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/multiSelectComboBoxPicker.html",
            propertyGroups = StudioPropertyGroups.MultiSelectComboBoxDefaultProperties.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            },
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.Renderer"
                    )
            }
    )
    MultiSelectComboBoxPicker multiSelectComboBoxPicker();

    @StudioComponent(
            name = "UserMenu",
            classFqn = "io.jmix.flowui.component.usermenu.UserMenu",
            category = "Components",
            xmlElement = StudioXmlElements.USER_MENU,
            icon = "io/jmix/flowui/kit/meta/icon/mainview/userIndicator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/userMenu.html",
            propertyGroups = StudioPropertyGroups.UserMenuComponent.class)
    JmixUserMenu<?> userMenu();

    @StudioComponent(
            name = "DropdownButton",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.DropdownButton",
            category = "Components",
            xmlElement = StudioXmlElements.DROPDOWN_BUTTON,
            icon = "io/jmix/flowui/kit/meta/icon/component/dropdownButton.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html",
            propertyGroups = StudioPropertyGroups.DropdownButtonComponent.class)
    DropdownButton dropdownButton();

    @StudioComponent(
            name = "ComboButton",
            classFqn = "io.jmix.flowui.kit.component.combobutton.ComboButton",
            category = "Components",
            xmlElement = StudioXmlElements.COMBO_BUTTON,
            icon = "io/jmix/flowui/kit/meta/icon/component/comboButton.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/comboButton.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "button")
            }),
            propertyGroups = StudioPropertyGroups.ComboButtonComponent.class)
    ComboButton comboButton();

    @StudioComponent(
            name = "DatePicker",
            classFqn = "io.jmix.flowui.component.datepicker.TypedDatePicker",
            category = "Components",
            xmlElement = StudioXmlElements.DATE_PICKER,
            icon = "io/jmix/flowui/kit/meta/icon/component/datePicker.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/datePicker.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "timePicker"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "dateTimePicker")
            }),
            propertyGroups = StudioPropertyGroups.DatePickerComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    DatePicker datePicker();

    @StudioComponent(
            name = "DateTimePicker",
            classFqn = "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
            category = "Components",
            xmlElement = StudioXmlElements.DATE_TIME_PICKER,
            icon = "io/jmix/flowui/kit/meta/icon/component/datePicker.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/dateTimePicker.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "datePicker"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "timePicker")
            }),
            propertyGroups = StudioPropertyGroups.DateTimePickerComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    DateTimePicker dateTimePicker();

    @StudioComponent(
            name = "DrawerToggle",
            classFqn = "com.vaadin.flow.component.applayout.DrawerToggle",
            category = "Components",
            xmlElement = StudioXmlElements.DRAWER_TOGGLE,
            icon = "io/jmix/flowui/kit/meta/icon/component/drawerToggle.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/drawerToggle.html",
            propertyGroups = StudioPropertyGroups.DrawerToggleComponent.class)
    DrawerToggle drawerToggle();

    @StudioComponent(
            name = "SidePanelLayoutCloser",
            classFqn = "io.jmix.flowui.component.sidepanellayout.SidePanelLayoutCloser",
            category = "Components",
            xmlElement = StudioXmlElements.SIDE_PANEL_LAYOUT_CLOSER,
            icon = "io/jmix/flowui/kit/meta/icon/component/sidePanelLayoutCloser.svg",
            propertyGroups = StudioPropertyGroups.SidePanelLayoutCloserComponent.class)
    JmixSidePanelLayoutCloser sidePanelLayoutCloser();

    @StudioComponent(
            name = "EmailField",
            classFqn = "io.jmix.flowui.component.textfield.JmixEmailField",
            category = "Components",
            xmlElement = StudioXmlElements.EMAIL_FIELD,
            icon = "io/jmix/flowui/kit/meta/icon/component/emailField.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/emailField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField")
            }),
            propertyGroups = StudioPropertyGroups.EmailFieldComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    EmailField emailField();

    @StudioComponent(
            name = "EntityComboBox",
            classFqn = "io.jmix.flowui.component.combobox.EntityComboBox",
            category = "Components",
            xmlElement = StudioXmlElements.ENTITY_COMBO_BOX,
            icon = "io/jmix/flowui/kit/meta/icon/component/entityComboBox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/entityComboBox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "comboBox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "entityPicker")
            }),
            propertyGroups = StudioPropertyGroups.EntityComboBoxComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            },
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.Renderer"
                    )
            }
    )
    ComboBoxPicker entityComboBox();

    @StudioComponent(
            name = "EntityPicker",
            classFqn = "io.jmix.flowui.component.valuepicker.EntityPicker",
            category = "Components",
            xmlElement = StudioXmlElements.ENTITY_PICKER,
            icon = "io/jmix/flowui/kit/meta/icon/component/entityPicker.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/entityPicker.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "comboBox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "entityComboBox", attributeConvertStrategy = {
                            @StudioConvertStrategy.AttributeConvertStrategy(
                                    type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
                                    qualifiedName = "itemsContainer",
                                    value = ""
                            )
                    })
            }),
            propertyGroups = StudioPropertyGroups.EntityPickerComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    ValuePicker entityPicker();

    @StudioComponent(
            name = "DataGrid",
            classFqn = "io.jmix.flowui.component.grid.DataGrid",
            category = "Components",
            xmlElement = StudioXmlElements.DATA_GRID,
            icon = "io/jmix/flowui/kit/meta/icon/component/dataGrid.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "treeDataGrid")
            }),
            propertyGroups = StudioPropertyGroups.DataGridComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    JmixGrid dataGrid();

    @StudioComponent(
            name = "TreeDataGrid",
            classFqn = "io.jmix.flowui.component.grid.TreeDataGrid",
            category = "Components",
            xmlElement = StudioXmlElements.TREE_DATA_GRID,
            icon = "io/jmix/flowui/kit/meta/icon/component/treeDataGrid.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/treeDataGrid.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "dataGrid")
            }),
            propertyGroups = StudioPropertyGroups.TreeDataGridComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "hierarchyProperty"
                    ),
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    JmixTreeGrid treeDataGrid();

    @StudioComponent(
            name = "IntegerField",
            classFqn = "io.jmix.flowui.component.textfield.JmixIntegerField",
            category = "Components",
            xmlElement = StudioXmlElements.INTEGER_FIELD,
            icon = "io/jmix/flowui/kit/meta/icon/component/integerField.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/integerField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "bigDecimalField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "numberField")
            }),
            propertyGroups = StudioPropertyGroups.IntegerFieldComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    IntegerField integerField();

    @StudioComponent(
            name = "NumberField",
            classFqn = "io.jmix.flowui.component.textfield.JmixNumberField",
            category = "Components",
            xmlElement = StudioXmlElements.NUMBER_FIELD,
            icon = "io/jmix/flowui/kit/meta/icon/component/numberField.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/numberField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "integerField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "bigDecimalField")
            }),
            propertyGroups = StudioPropertyGroups.NumberFieldComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    NumberField numberField();

    @StudioComponent(
            name = "PasswordField",
            classFqn = "io.jmix.flowui.component.textfield.JmixPasswordField",
            category = "Components",
            xmlElement = StudioXmlElements.PASSWORD_FIELD,
            icon = "io/jmix/flowui/kit/meta/icon/component/passwordField.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/passwordField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField")
            }),
            propertyGroups = StudioPropertyGroups.PasswordFieldComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    PasswordField passwordField();

    @StudioComponent(
            name = "ProgressBar",
            classFqn = "com.vaadin.flow.component.progressbar.ProgressBar",
            category = "Components",
            xmlElement = StudioXmlElements.PROGRESS_BAR,
            icon = "io/jmix/flowui/kit/meta/icon/component/progressBar.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/progressBar.html",
            propertyGroups = StudioPropertyGroups.ProgressBarComponent.class)
    ProgressBar progressBar();

    @StudioComponent(
            name = "RadioButtonGroup",
            classFqn = "io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup",
            category = "Components",
            xmlElement = StudioXmlElements.RADIO_BUTTON_GROUP,
            icon = "io/jmix/flowui/kit/meta/icon/component/radioButtonGroup.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/radioButtonGroup.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "checkBoxGroup")
            }),
            propertyGroups = StudioPropertyGroups.RadioButtonGroupComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            },
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.ComponentRenderer"
                    )
            }
    )
    RadioButtonGroup radioButtonGroup();

    @StudioComponent(
            name = "Select",
            classFqn = "io.jmix.flowui.component.select.JmixSelect",
            category = "Components",
            xmlElement = StudioXmlElements.SELECT,
            icon = "io/jmix/flowui/kit/meta/icon/component/select.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/select.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "comboBox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "entityComboBox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiSelectComboBox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiSelectComboBoxPicker")
            }),
            propertyGroups = StudioPropertyGroups.SelectComponent.class,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = {
                            @StudioAvailableChildrenInfo.TagInfo(
                                    qualifiedName = "prefix",
                                    maxCount = 1
                            ),
                            @StudioAvailableChildrenInfo.TagInfo(
                                    qualifiedName = "tooltip",
                                    maxCount = 1
                            ),
                            @StudioAvailableChildrenInfo.TagInfo(
                                    qualifiedName = "fragmentRenderer",
                                    maxCount = 1
                            )
                    }
            ),
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            },
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.ComponentRenderer"
                    )
            }
    )
    Select select();

    @StudioComponent(
            name = "Tabs",
            classFqn = "io.jmix.flowui.component.tabsheet.JmixTabs",
            category = "Components",
            xmlElement = StudioXmlElements.TABS,
            icon = "io/jmix/flowui/kit/meta/icon/component/tabs.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/tabs.html",
            propertyGroups = StudioPropertyGroups.TabsComponent.class)
    Tabs tabs();

    @StudioComponent(
            name = "ListBox",
            classFqn = "io.jmix.flowui.component.listbox.JmixListBox",
            category = "Components",
            xmlElement = StudioXmlElements.LIST_BOX,
            icon = "io/jmix/flowui/kit/meta/icon/component/listBox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/listBox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiSelectListBox")
            }),
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = {
                            @StudioAvailableChildrenInfo.TagInfo(
                                    qualifiedName = "tooltip",
                                    maxCount = 1
                            ),
                            @StudioAvailableChildrenInfo.TagInfo(
                                    qualifiedName = "fragmentRenderer",
                                    maxCount = 1
                            )
                    }
            ),
            propertyGroups = StudioPropertyGroups.ListBoxComponent.class,
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.ComponentRenderer"
                    )
            }
    )
    ListBox listBox();

    @StudioComponent(
            name = "MultiSelectListBox",
            classFqn = "io.jmix.flowui.component.listbox.JmixMultiSelectListBox",
            category = "Components",
            xmlElement = StudioXmlElements.MULTI_SELECT_LIST_BOX,
            icon = "io/jmix/flowui/kit/meta/icon/component/listBox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/multiSelectListBox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "listBox")
            }),
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = {
                            @StudioAvailableChildrenInfo.TagInfo(
                                    qualifiedName = "tooltip",
                                    maxCount = 1
                            ),
                            @StudioAvailableChildrenInfo.TagInfo(
                                    qualifiedName = "fragmentRenderer",
                                    maxCount = 1
                            )
                    }
            ),
            propertyGroups = StudioPropertyGroups.ListBoxComponent.class,
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.ComponentRenderer"
                    )
            }
    )
    MultiSelectListBox multiSelectListBox();

    @StudioComponent(
            name = "TextArea",
            classFqn = "io.jmix.flowui.component.textarea.JmixTextArea",
            category = "Components",
            xmlElement = StudioXmlElements.TEXT_AREA,
            icon = "io/jmix/flowui/kit/meta/icon/component/textArea.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/textArea.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "codeEditor"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "richTextEditor")
            }),
            propertyGroups = StudioPropertyGroups.TextAreaComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    TextArea textArea();

    @StudioComponent(
            name = "TextField",
            classFqn = "io.jmix.flowui.component.textfield.TypedTextField",
            category = "Components",
            xmlElement = StudioXmlElements.TEXT_FIELD,
            icon = "io/jmix/flowui/kit/meta/icon/component/textField.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/textField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textArea"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "richTextEditor"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "codeEditor"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "emailField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "passwordField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "numberField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "integerField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "bigDecimalField")
            }),
            propertyGroups = StudioPropertyGroups.TextFieldComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    TextField textField();

    @StudioComponent(
            name = "TimePicker",
            classFqn = "io.jmix.flowui.component.timepicker.TypedTimePicker",
            category = "Components",
            xmlElement = StudioXmlElements.TIME_PICKER,
            icon = "io/jmix/flowui/kit/meta/icon/component/timePicker.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/timePicker.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "datePicker"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "dateTimePicker"),
            }),
            propertyGroups = StudioPropertyGroups.TimePickerComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    TimePicker timePicker();

    @StudioComponent(
            name = "ValuePicker",
            classFqn = "io.jmix.flowui.component.valuepicker.JmixValuePicker",
            category = "Components",
            xmlElement = StudioXmlElements.VALUE_PICKER,
            icon = "io/jmix/flowui/kit/meta/icon/component/valuePicker.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/valuePicker.html",
            propertyGroups = StudioPropertyGroups.ValuePickerComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    ValuePicker valuePicker();

    @StudioComponent(
            name = "MultiValuePicker",
            classFqn = "io.jmix.flowui.component.valuepicker.JmixMultiValuePicker",
            category = "Components",
            xmlElement = StudioXmlElements.MULTI_VALUE_PICKER,
            icon = "io/jmix/flowui/kit/meta/icon/component/multiValuePicker.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/multiValuePicker.html",
            propertyGroups = StudioPropertyGroups.ValuePickerComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    MultiValuePicker multiValuePicker();

    @StudioComponent(
            name = "LoginForm",
            classFqn = "io.jmix.flowui.component.loginform.JmixLoginForm",
            category = "Components",
            xmlElement = StudioXmlElements.LOGIN_FORM,
            icon = "io/jmix/flowui/kit/meta/icon/component/loginForm.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/loginForm.html",
            propertyGroups = StudioPropertyGroups.LoginFormComponent.class)
    EnhancedLoginForm loginForm();

    @StudioComponent(
            name = "LoginOverlay",
            classFqn = "com.vaadin.flow.component.login.LoginOverlay",
            category = "Components",
            xmlElement = StudioXmlElements.LOGIN_OVERLAY,
            icon = "io/jmix/flowui/kit/meta/icon/component/loginOverlay.svg",
            propertyGroups = StudioPropertyGroups.LoginOverlayComponent.class)
    LoginOverlay loginOverlay();

    @StudioComponent(
            name = "SimplePagination",
            classFqn = "io.jmix.flowui.component.pagination.SimplePagination",
            category = "Components",
            xmlElement = StudioXmlElements.SIMPLE_PAGINATION,
            icon = "io/jmix/flowui/kit/meta/icon/component/simplePagination.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/simplePagination.html",
            propertyGroups = StudioPropertyGroups.SimplePaginationComponent.class)
    JmixSimplePagination simplePagination();

    @StudioComponent(
            name = "Upload",
            classFqn = "io.jmix.flowui.component.upload.JmixUpload",
            category = "Components",
            xmlElement = StudioXmlElements.UPLOAD,
            icon = "io/jmix/flowui/kit/meta/icon/component/upload.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/upload.html",
            propertyGroups = StudioPropertyGroups.UploadComponent.class)
    Upload upload();

    @StudioComponent(
            name = "FileUploadField",
            classFqn = "io.jmix.flowui.component.upload.FileUploadField",
            category = "Components",
            xmlElement = StudioXmlElements.FILE_UPLOAD_FIELD,
            icon = "io/jmix/flowui/kit/meta/icon/component/upload.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/fileUploadField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "fileStorageUploadField")
            }),
            propertyGroups = StudioPropertyGroups.FileUploadFieldComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    JmixFileUploadField fileUploadField();

    @StudioComponent(
            name = "FileStorageUploadField",
            classFqn = "io.jmix.flowui.component.upload.FileStorageUploadField",
            category = "Components",
            xmlElement = StudioXmlElements.FILE_STORAGE_UPLOAD_FIELD,
            icon = "io/jmix/flowui/kit/meta/icon/component/upload.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/fileStorageUploadField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "fileUploadField")
            }),
            propertyGroups = StudioPropertyGroups.FileStorageUploadFieldComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    JmixFileStorageUploadField fileStorageUploadField();

    @StudioComponent(
            name = "GenericFilter",
            classFqn = "io.jmix.flowui.component.genericfilter.GenericFilter",
            category = "Components",
            xmlElement = StudioXmlElements.GENERIC_FILTER,
            icon = "io/jmix/flowui/kit/meta/icon/component/genericFilter.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/genericFilter.html",
            propertyGroups = StudioPropertyGroups.GenericFilterComponentComponent.class)
    Details genericFilter();

    @StudioComponent(
            name = "PropertyFilter",
            classFqn = "io.jmix.flowui.component.propertyfilter.PropertyFilter",
            category = "Components",
            xmlElement = StudioXmlElements.PROPERTY_FILTER,
            icon = "io/jmix/flowui/kit/meta/icon/component/propertyFilter.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/genericFilter-components.html#property-filter",
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 1
                    )
            ),
            propertyGroups = StudioPropertyGroups.PropertyFilterComponent.class)
    HorizontalLayout propertyFilter();

    @StudioComponent(
            name = "JpqlFilter",
            classFqn = "io.jmix.flowui.component.jpqlfilter.JpqlFilter",
            category = "Components",
            xmlElement = StudioXmlElements.JPQL_FILTER,
            icon = "io/jmix/flowui/kit/meta/icon/component/jpqlFilter.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/genericFilter-components.html#jpql-filter",
            propertyGroups = StudioPropertyGroups.JpqlFilterComponent.class)
    HorizontalLayout jpqlFilter();

    @StudioComponent(
            name = "GroupFilter",
            classFqn = "io.jmix.flowui.component.logicalfilter.GroupFilter",
            category = "Components",
            xmlElement = StudioXmlElements.GROUP_FILTER,
            icon = "io/jmix/flowui/kit/meta/icon/component/groupFilter.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/genericFilter-components.html#group-filter",
            propertyGroups = StudioPropertyGroups.GroupFilterComponentComponent.class)
    HorizontalLayout groupFilter();

    @StudioComponent(
            name = "Layout",
            xmlElement = StudioXmlElements.LAYOUT,
            icon = "io/jmix/flowui/kit/meta/icon/view/layout.svg",
            propertyGroups = StudioPropertyGroups.LayoutComponent.class)
    VerticalLayout layout();

    @StudioComponent(
            name = "View",
            xmlElement = StudioXmlElements.VIEW,
            classFqn = "io.jmix.flowui.view.View",
            icon = "io/jmix/flowui/kit/meta/icon/view/view.svg",
            availablePlaceRegExp = "",
            documentationLink = "%VERSION%/flow-ui/views.html",
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = {
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "data", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "facets", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "actions", maxCount = 1),
                            @StudioAvailableChildrenInfo.TagInfo(qualifiedName = "layout", maxCount = 1)
                    }
            ),
            propertyGroups = StudioPropertyGroups.ViewComponent.class)
    VerticalLayout view();

    @StudioComponent(
            name = "CodeEditor",
            classFqn = "io.jmix.flowui.component.codeeditor.CodeEditor",
            category = "Components",
            xmlElement = StudioXmlElements.CODE_EDITOR,
            icon = "io/jmix/flowui/kit/meta/icon/component/codeEditor.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/codeEditor.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textArea"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "richTextEditor")
            }),
            propertyGroups = StudioPropertyGroups.CodeEditorComponent.class,
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    JmixCodeEditor codeEditor();

    @StudioComponent(
            name = "VirtualList",
            classFqn = "io.jmix.flowui.component.virtuallist.JmixVirtualList",
            category = "Components",
            xmlElement = StudioXmlElements.VIRTUAL_LIST,
            icon = "io/jmix/flowui/kit/meta/icon/component/virtualList.svg",
            propertyGroups = StudioPropertyGroups.VirtualListComponent.class,
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setRenderer",
                            parameterType = "com.vaadin.flow.data.renderer.Renderer"
                    )
            }
    )
    VirtualList virtualList();

    @StudioComponent(
            name = "TwinColumn",
            classFqn = "io.jmix.flowui.component.twincolumn.TwinColumn",
            category = "Components",
            xmlElement = StudioXmlElements.TWIN_COLUMN,
            icon = "io/jmix/flowui/kit/meta/icon/component/twinColumn.svg",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "checkboxGroup"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiSelectComboBox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiValuePicker")
            }),
            propertyGroups = StudioPropertyGroups.TwinColumnComponent.class,
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = {
                            @StudioAvailableChildrenInfo.TagInfo(
                                    qualifiedName = "tooltip",
                                    maxCount = 1
                            )
                    }
            ),
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    JmixTwinColumn twinColumn();

    @StudioComponent(
            name = "GridColumnVisibility",
            classFqn = "io.jmix.flowui.component.gridcolumnvisibility.JmixGridColumnVisibility",
            category = "Components",
            xmlElement = StudioXmlElements.GRID_COLUMN_VISIBILITY,
            icon = "io/jmix/flowui/kit/meta/icon/component/gridColumnVisibility.svg",
            propertyGroups = StudioPropertyGroups.GridColumnVisibilityComponent.class)
    JmixMenuBar gridColumnVisibility();

    @StudioComponent(
            name = "MenuFilterField",
            classFqn = "io.jmix.flowui.component.menufilterfield.MenuFilterField",
            category = "Components",
            xmlElement = StudioXmlElements.MENU_FILTER_FIELD,
            icon = "io/jmix/flowui/kit/meta/icon/component/menufilterfield.svg",
            propertyGroups = StudioPropertyGroups.MenuFilterFieldComponent.class)
    TextField menuFilterField();

    @StudioComponent(
            name = "RichTextEditor",
            classFqn = "io.jmix.flowui.component.richtexteditor.RichTextEditor",
            category = "Components",
            xmlElement = StudioXmlElements.RICH_TEXT_EDITOR,
            icon = "io/jmix/flowui/kit/meta/icon/component/richTextEditor.svg",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textArea"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "codeEditor")
            }),
            propertyGroups = StudioPropertyGroups.RichTextEditorComponent.class)
    JmixRichTextEditor richTextEditor();

    @StudioComponent(
            name = "HorizontalMenu",
            classFqn = "io.jmix.flowui.component.horizontalmenu.HorizontalMenu",
            category = "Components",
            xmlElement = StudioXmlElements.HORIZONTAL_MENU,
            icon = "io/jmix/flowui/kit/meta/icon/component/horizontalMenu.svg",
            propertyGroups = StudioPropertyGroups.HorizontalMenuComponent.class)
    JmixMenuBar horizontalMenu();

    @StudioComponent(
            name = "ListMenu",
            classFqn = "io.jmix.flowui.component.main.JmixListMenu",
            category = "Components",
            xmlElement = StudioXmlElements.LIST_MENU,
            icon = "io/jmix/flowui/kit/meta/icon/mainview/listMenu.svg",
            propertyGroups = StudioPropertyGroups.ListMenuComponent.class)
    ListMenu listMenu();

    @StudioComponent(
            name = "Html",
            classFqn = "com.vaadin.flow.component.Html",
            category = "Components",
            xmlElement = StudioXmlElements.HTML,
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = "content",
                            maxCount = 1
                    )
            ),
            propertyGroups = StudioPropertyGroups.HtmlComponent.class)
    Html html();

    @StudioComponent(
            name = "Markdown",
            classFqn = "com.vaadin.flow.component.markdown.Markdown",
            category = "Components",
            xmlElement = StudioXmlElements.MARKDOWN,
            icon = "io/jmix/flowui/kit/meta/icon/component/markdown.svg",
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = "content",
                            maxCount = 1
                    )
            ),
            propertyGroups = {
                    StudioPropertyGroups.BaseSizedComponentWithClassNames.class,
                    StudioPropertyGroups.Content.class
            })
    Markdown markdown();
}
