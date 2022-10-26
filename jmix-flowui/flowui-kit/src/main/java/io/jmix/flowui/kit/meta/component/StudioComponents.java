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

import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.combobox.ComboBoxPicker;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.kit.component.grid.JmixTreeGrid;
import io.jmix.flowui.kit.component.loginform.EnhancedLoginForm;
import io.jmix.flowui.kit.component.pagination.JmixSimplePagination;
import io.jmix.flowui.kit.component.valuepicker.ValuePicker;
import io.jmix.flowui.kit.component.valuepicker.MultiValuePicker;
import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioPropertiesBinding;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioComponents {

    @StudioComponent(
            name = "Avatar",
            classFqn = "com.vaadin.flow.component.avatar.Avatar",
            category = "Components",
            xmlElement = "avatar",
            icon = "io/jmix/flowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "abbreviation", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colorIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "image", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"xlarge", "large", "small", "xsmall"}),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            }
    )
    Avatar avatar();

    @StudioComponent(
            name = "BigDecimalField",
            classFqn = "io.jmix.flowui.component.textfield.JmixBigDecimalField",
            category = "Components",
            xmlElement = "bigDecimalField",
            icon = "io/jmix/flowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "autocapitalize", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocapitalize", defaultValue = "NONE",
                            options = {"NONE", "SENTENCES", "WORDS", "CHARACTERS"}),
                    @StudioProperty(xmlAttribute = "autocomplete", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocomplete", defaultValue = "OFF",
                            options = {"OFF", "ON", "NAME", "HONORIFIC_PREFIX", "GIVEN_NAME", "ADDITIONAL_NAME",
                                    "FAMILY_NAME", "HONORIFIC_SUFFIX", "NICKNAME", "EMAIL", "USERNAME", "NEW_PASSWORD",
                                    "CURRENT_PASSWORD", "ORGANIZATION_TITLE", "ORGANIZATION", "STREET_ADDRESS",
                                    "ADDRESS_LINE1", "ADDRESS_LINE2", "ADDRESS_LINE3", "ADDRESS_LEVEL1",
                                    "ADDRESS_LEVEL2", "ADDRESS_LEVEL3", "ADDRESS_LEVEL4", "COUNTRY", "COUNTRY_NAME",
                                    "POSTAL_CODE", "CC_NAME", "CC_GIVEN_NAME", "CC_ADDITIONAL_NAME", "CC_FAMILY_NAME",
                                    "CC_NUMBER", "CC_EXP", "CC_EXP_MONTH", "CC_EXP_YEAR", "CC_CSC", "CC_TYPE",
                                    "TRANSACTION_CURRENCY", "TRANSACTION_AMOUNT", "LANGUAGE", "BDAY", "BDAY_DAY",
                                    "BDAY_MONTH", "BDAY_YEAR", "SEX", "TEL", "TEL_COUNTRY_CODE", "TEL_NATIONAL",
                                    "TEL_AREA_CODE", "TEL_LOCAL", "TEL_LOCAL_PREFIX", "TEL_LOCAL_SUFFIX",
                                    "TEL_EXTENSION", "URL", "PHOTO",}),
                    @StudioProperty(xmlAttribute = "autocorrect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoselect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
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
            xmlElement = "button",
            icon = "io/jmix/flowui/kit/meta/icon/component/button.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "action", type = StudioPropertyType.ACTION_REF,
                            classFqn = "io.jmix.flowui.kit.action.Action"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "disableOnClick", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = "iconAfterText", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline", "primary", "success", "error",
                                    "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "whiteSpace", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            }
    )
    JmixButton button();

    @StudioComponent(
            name = "CheckboxGroup",
            classFqn = "com.vaadin.flow.component.checkbox.CheckboxGroup",
            category = "Components",
            xmlElement = "checkboxGroup",
            icon = "io/jmix/flowui/kit/meta/icon/component/checkBoxGroup.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "itemsContainer",
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "itemsEnum", type = StudioPropertyType.ENUM_CLASS,
                            typeParameter = "T"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "T"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"helper-above-field", "vertical"}),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    CheckboxGroup checkboxGroup();

    @StudioComponent(
            name = "Checkbox",
            classFqn = "com.vaadin.flow.component.checkbox.Checkbox",
            category = "Components",
            xmlElement = "checkbox",
            icon = "io/jmix/flowui/kit/meta/icon/component/checkbox.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "indeterminate", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Checkbox checkbox();

    @StudioComponent(
            name = "ComboBox",
            classFqn = "io.jmix.flowui.component.combobox.JmixComboBox",
            category = "Components",
            xmlElement = "comboBox",
            icon = "io/jmix/flowui/kit/meta/icon/component/comboBox.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsEnum", type = StudioPropertyType.ENUM_CLASS,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "pattern", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "preventInvalidInput", type = StudioPropertyType.BOOLEAN
                            , defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-left", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    ComboBox comboBox();

    @StudioComponent(
            name = "DropdownButton",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.DropdownButton",
            category = "Components",
            xmlElement = "dropdownButton",
            icon = "io/jmix/flowui/kit/meta/icon/component/dropdownButton.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "openOnHover", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary-inline", "primary", "success", "error", "contrast",
                                    "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "whiteSpace", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            }
    )
    DropdownButton dropdownButton();

    @StudioComponent(
            name = "ComboButton",
            classFqn = "io.jmix.flowui.kit.component.combobutton.ComboButton",
            category = "Components",
            xmlElement = "comboButton",
            icon = "io/jmix/flowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "action", type = StudioPropertyType.ACTION_REF),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "openOnHover", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary-inline", "primary", "success", "error", "contrast",
                                    "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "whiteSpace", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            }
    )
    MenuBar comboButton();

    @StudioComponent(
            name = "ComboBoxPicker",
            classFqn = "io.jmix.flowui.kit.component.combobox.ComboBoxPicker",
            category = "Components",
            xmlElement = "comboBoxPicker",
            icon = "io/jmix/flowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "pattern", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "preventInvalidInput", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-left", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            }
    )
    ComboBoxPicker comboBoxPicker();

    @StudioComponent(
            name = "DatePicker",
            classFqn = "io.jmix.flowui.component.datepicker.TypedDatePicker",
            category = "Components",
            xmlElement = "datePicker",
            icon = "io/jmix/flowui/kit/meta/icon/component/datePicker.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "datatype", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.core.metamodel.datatype.Datatype", options = {"date", "dateTime",
                            "localDateTime", "offsetDateTime", "localDate"}, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "opened", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-left", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
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
            xmlElement = "dateTimePicker",
            icon = "io/jmix/flowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "datatype", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.core.metamodel.datatype.Datatype", options = {"date", "dateTime",
                            "localDateTime", "offsetTime", "localTime", "offsetDateTime", "time", "localDate"}),
                    @StudioProperty(xmlAttribute = "datePlaceholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "timePlaceholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "weekNumbersVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
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
            xmlElement = "drawerToggle",
            icon = "io/jmix/flowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "autoFocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            }
    )
    DrawerToggle drawerToggle();

    @StudioComponent(
            name = "EmailField",
            classFqn = "com.vaadin.flow.component.textfield.EmailField",
            category = "Components",
            xmlElement = "emailField",
            icon = "io/jmix/flowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "autocapitalize", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocapitalize", defaultValue = "NONE",
                            options = {"NONE", "SENTENCES", "WORDS", "CHARACTERS"}),
                    @StudioProperty(xmlAttribute = "autocomplete", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocomplete", defaultValue = "OFF",
                            options = {"OFF", "ON", "NAME", "HONORIFIC_PREFIX", "GIVEN_NAME", "ADDITIONAL_NAME",
                                    "FAMILY_NAME", "HONORIFIC_SUFFIX", "NICKNAME", "EMAIL", "USERNAME", "NEW_PASSWORD",
                                    "CURRENT_PASSWORD", "ORGANIZATION_TITLE", "ORGANIZATION", "STREET_ADDRESS",
                                    "ADDRESS_LINE1", "ADDRESS_LINE2", "ADDRESS_LINE3", "ADDRESS_LEVEL1",
                                    "ADDRESS_LEVEL2", "ADDRESS_LEVEL3", "ADDRESS_LEVEL4", "COUNTRY", "COUNTRY_NAME",
                                    "POSTAL_CODE", "CC_NAME", "CC_GIVEN_NAME", "CC_ADDITIONAL_NAME", "CC_FAMILY_NAME",
                                    "CC_NUMBER", "CC_EXP", "CC_EXP_MONTH", "CC_EXP_YEAR", "CC_CSC", "CC_TYPE",
                                    "TRANSACTION_CURRENCY", "TRANSACTION_AMOUNT", "LANGUAGE", "BDAY", "BDAY_DAY",
                                    "BDAY_MONTH", "BDAY_YEAR", "SEX", "TEL", "TEL_COUNTRY_CODE", "TEL_NATIONAL",
                                    "TEL_AREA_CODE", "TEL_LOCAL", "TEL_LOCAL_PREFIX", "TEL_LOCAL_SUFFIX",
                                    "TEL_EXTENSION", "URL", "PHOTO",}),
                    @StudioProperty(xmlAttribute = "autocorrect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoselect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "pattern", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "preventInvalidInput", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
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
            xmlElement = "entityComboBox",
            icon = "io/jmix/flowui/kit/meta/icon/component/entityComboBox.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer",
                            type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer",
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "metaClass", type = StudioPropertyType.ENTITY_NAME),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "opened", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "pattern", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "preventInvalidInput", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-left", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    ComboBoxPicker entityComboBox();

    @StudioComponent(
            name = "EntityPicker",
            classFqn = "io.jmix.flowui.component.valuepicker.EntityPicker",
            category = "Components",
            xmlElement = "entityPicker",
            icon = "io/jmix/flowui/kit/meta/icon/component/entityPicker.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "metaClass", type = StudioPropertyType.ENTITY_NAME,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
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
            xmlElement = "dataGrid",
            icon = "io/jmix/flowui/kit/meta/icon/component/dataGrid.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "allRowsVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "columnReorderingAllowed", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer",
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, required = true,
                            typeParameter = "E"),
                    @StudioProperty(xmlAttribute = "detailsVisibleOnClick", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dropMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.dnd.GridDropMode", options = {"BETWEEN", "ON_TOP",
                            "ON_TOP_OR_BETWEEN", "ON_GRID"}),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "metaClass", type = StudioPropertyType.ENTITY_NAME, typeParameter = "E"),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "multiSort", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "nestedNullBehavior", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.Grid.NestedNullBehavior", defaultValue = "THROW",
                            options = {"THROW", "ALLOW_NULLS"}),
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER, defaultValue = "50"),
                    @StudioProperty(xmlAttribute = "rowDraggable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "selectionMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.Grid.SelectionMode", defaultValue = "MULTI",
                            options = {"SINGLE", "MULTI"}),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"no-border", "no-row-borders", "column-borders", "row-stripes",
                                    "compact", "wrap-cell-content", "column-dividers"}),
                    @StudioProperty(xmlAttribute = "verticalScrollingEnabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE, initialValue = "100%")
            },
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
            xmlElement = "treeDataGrid",
            icon = "io/jmix/flowui/kit/meta/icon/component/treeDataGrid.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "allRowsVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "columnReorderingAllowed", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
                            required = true, typeParameter = "T"),
                    @StudioProperty(xmlAttribute = "detailsVisibleOnClick", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dropMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.dnd.GridDropMode", options = {"BETWEEN", "ON_TOP",
                            "ON_TOP_OR_BETWEEN", "ON_GRID"}),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "hierarchyProperty", type = StudioPropertyType.PROPERTY_REF,
                            required = true, typeParameter = "T"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "metaClass", type = StudioPropertyType.ENTITY_NAME,
                            typeParameter = "T"),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "multiSort", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "nestedNullBehavior", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.Grid.NestedNullBehavior", defaultValue = "THROW",
                            options = {"THROW", "ALLOW_NULLS"}),
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER, defaultValue = "50"),
                    @StudioProperty(xmlAttribute = "rowDraggable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "selectionMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.Grid.SelectionMode", defaultValue = "MULTI",
                            options = {"SINGLE", "MULTI"}),
                    @StudioProperty(xmlAttribute = "showOrphans", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"no-border", "no-row-borders", "column-borders", "row-stripes",
                                    "compact", "wrap-cell-content", "column-dividers"}),
                    @StudioProperty(xmlAttribute = "verticalScrollingEnabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE, initialValue = "100%")
            },
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
            name = "NumberField",
            classFqn = "com.vaadin.flow.component.textfield.NumberField",
            category = "Components",
            xmlElement = "numberField",
            icon = "io/jmix/flowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "autocapitalize", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocapitalize", defaultValue = "NONE",
                            options = {"NONE", "SENTENCES", "WORDS", "CHARACTERS"}),
                    @StudioProperty(xmlAttribute = "autocomplete", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocomplete", defaultValue = "OFF",
                            options = {"OFF", "ON", "NAME", "HONORIFIC_PREFIX", "GIVEN_NAME", "ADDITIONAL_NAME",
                                    "FAMILY_NAME", "HONORIFIC_SUFFIX", "NICKNAME", "EMAIL", "USERNAME", "NEW_PASSWORD",
                                    "CURRENT_PASSWORD", "ORGANIZATION_TITLE", "ORGANIZATION", "STREET_ADDRESS",
                                    "ADDRESS_LINE1", "ADDRESS_LINE2", "ADDRESS_LINE3", "ADDRESS_LEVEL1",
                                    "ADDRESS_LEVEL2", "ADDRESS_LEVEL3", "ADDRESS_LEVEL4", "COUNTRY", "COUNTRY_NAME",
                                    "POSTAL_CODE", "CC_NAME", "CC_GIVEN_NAME", "CC_ADDITIONAL_NAME", "CC_FAMILY_NAME",
                                    "CC_NUMBER", "CC_EXP", "CC_EXP_MONTH", "CC_EXP_YEAR", "CC_CSC", "CC_TYPE",
                                    "TRANSACTION_CURRENCY", "TRANSACTION_AMOUNT", "LANGUAGE", "BDAY", "BDAY_DAY",
                                    "BDAY_MONTH", "BDAY_YEAR", "SEX", "TEL", "TEL_COUNTRY_CODE", "TEL_NATIONAL",
                                    "TEL_AREA_CODE", "TEL_LOCAL", "TEL_LOCAL_PREFIX", "TEL_LOCAL_SUFFIX",
                                    "TEL_EXTENSION", "URL", "PHOTO",}),
                    @StudioProperty(xmlAttribute = "autocorrect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoselect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "hasControls", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "max", type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "min", type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "step", type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
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
            classFqn = "com.vaadin.flow.component.textfield.PasswordField",
            category = "Components",
            xmlElement = "passwordField",
            icon = "io/jmix/flowui/kit/meta/icon/component/passwordField.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "autocapitalize", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocapitalize", defaultValue = "NONE",
                            options = {"NONE", "SENTENCES", "WORDS", "CHARACTERS"}),
                    @StudioProperty(xmlAttribute = "autocomplete", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocomplete", defaultValue = "OFF",
                            options = {"OFF", "ON", "NAME", "HONORIFIC_PREFIX", "GIVEN_NAME", "ADDITIONAL_NAME",
                                    "FAMILY_NAME", "HONORIFIC_SUFFIX", "NICKNAME", "EMAIL", "USERNAME", "NEW_PASSWORD",
                                    "CURRENT_PASSWORD", "ORGANIZATION_TITLE", "ORGANIZATION", "STREET_ADDRESS",
                                    "ADDRESS_LINE1", "ADDRESS_LINE2", "ADDRESS_LINE3", "ADDRESS_LEVEL1",
                                    "ADDRESS_LEVEL2", "ADDRESS_LEVEL3", "ADDRESS_LEVEL4", "COUNTRY", "COUNTRY_NAME",
                                    "POSTAL_CODE", "CC_NAME", "CC_GIVEN_NAME", "CC_ADDITIONAL_NAME", "CC_FAMILY_NAME",
                                    "CC_NUMBER", "CC_EXP", "CC_EXP_MONTH", "CC_EXP_YEAR", "CC_CSC", "CC_TYPE",
                                    "TRANSACTION_CURRENCY", "TRANSACTION_AMOUNT", "LANGUAGE", "BDAY", "BDAY_DAY",
                                    "BDAY_MONTH", "BDAY_YEAR", "SEX", "TEL", "TEL_COUNTRY_CODE", "TEL_NATIONAL",
                                    "TEL_AREA_CODE", "TEL_LOCAL", "TEL_LOCAL_PREFIX", "TEL_LOCAL_SUFFIX",
                                    "TEL_EXTENSION", "URL", "PHOTO",}),
                    @StudioProperty(xmlAttribute = "autocorrect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoselect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "pattern", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "preventInvalidInput", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
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
            xmlElement = "progressBar",
            icon = "io/jmix/flowui/kit/meta/icon/component/progressBar.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "indeterminate", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "max", type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "min", type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"contrast", "error", "success"}),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            }
    )
    ProgressBar progressBar();

    @StudioComponent(
            name = "RadioButtonGroup",
            classFqn = "com.vaadin.flow.component.radiobutton.RadioButtonGroup",
            category = "Components",
            xmlElement = "radioButtonGroup",
            icon = "io/jmix/flowui/kit/meta/icon/component/radioButtonGroup.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer",
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, typeParameter = "T"),
                    @StudioProperty(xmlAttribute = "itemsEnum", type = StudioPropertyType.ENUM_CLASS, typeParameter = "T"),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "T"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"vertical", "helper-above-field"}),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    RadioButtonGroup radioButtonGroup();

    @StudioComponent(
            name = "Select",
            classFqn = "com.vaadin.flow.component.select.Select",
            category = "Components",
            xmlElement = "select",
            icon = "io/jmix/flowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "emptySelectionAllowed", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "emptySelectionCaption", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer",
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "itemsEnum", type = StudioPropertyType.ENUM_CLASS),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "pattern", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "preventInvalidInput", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "T"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
            }
    )
    Select select();

    @StudioComponent(
            name = "Tabs",
            classFqn = "com.vaadin.flow.component.tabs.Tabs",
            category = "Components",
            xmlElement = "tabs",
            icon = "io/jmix/flowui/kit/meta/icon/component/tabs.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "orientation", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.tabs.Tabs$Orientation",
                            setMethod = "setOrientation", defaultValue = "HORIZONTAL",
                            options = {"VERTICAL", "HORIZONTAL"}),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"icon-on-top", "centered", "small", "minimal",
                                    "hide-scroll-buttons", "equal-width-tabs", "fixed"}),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE, defaultValue = "100%")
            }
    )
    Tabs tabs();

    @StudioComponent(
            name = "TextArea",
            classFqn = "com.vaadin.flow.component.textfield.TextArea",
            category = "Components",
            xmlElement = "textArea",
            icon = "io/jmix/flowui/kit/meta/icon/component/textArea.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "autocapitalize", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocapitalize", defaultValue = "NONE",
                            options = {"NONE", "SENTENCES", "WORDS", "CHARACTERS"}),
                    @StudioProperty(xmlAttribute = "autocomplete", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocomplete", defaultValue = "OFF",
                            options = {"OFF", "ON", "NAME", "HONORIFIC_PREFIX", "GIVEN_NAME", "ADDITIONAL_NAME",
                                    "FAMILY_NAME", "HONORIFIC_SUFFIX", "NICKNAME", "EMAIL", "USERNAME", "NEW_PASSWORD",
                                    "CURRENT_PASSWORD", "ORGANIZATION_TITLE", "ORGANIZATION", "STREET_ADDRESS",
                                    "ADDRESS_LINE1", "ADDRESS_LINE2", "ADDRESS_LINE3", "ADDRESS_LEVEL1",
                                    "ADDRESS_LEVEL2", "ADDRESS_LEVEL3", "ADDRESS_LEVEL4", "COUNTRY", "COUNTRY_NAME",
                                    "POSTAL_CODE", "CC_NAME", "CC_GIVEN_NAME", "CC_ADDITIONAL_NAME", "CC_FAMILY_NAME",
                                    "CC_NUMBER", "CC_EXP", "CC_EXP_MONTH", "CC_EXP_YEAR", "CC_CSC", "CC_TYPE",
                                    "TRANSACTION_CURRENCY", "TRANSACTION_AMOUNT", "LANGUAGE", "BDAY", "BDAY_DAY",
                                    "BDAY_MONTH", "BDAY_YEAR", "SEX", "TEL", "TEL_COUNTRY_CODE", "TEL_NATIONAL",
                                    "TEL_AREA_CODE", "TEL_LOCAL", "TEL_LOCAL_PREFIX", "TEL_LOCAL_SUFFIX",
                                    "TEL_EXTENSION", "URL", "PHOTO",}),
                    @StudioProperty(xmlAttribute = "autocorrect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoselect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "pattern", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
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
            xmlElement = "textField",
            icon = "io/jmix/flowui/kit/meta/icon/component/textField.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "autocapitalize", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocapitalize", defaultValue = "NONE",
                            options = {"NONE", "SENTENCES", "WORDS", "CHARACTERS"}),
                    @StudioProperty(xmlAttribute = "autocomplete", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocomplete", defaultValue = "OFF",
                            options = {"OFF", "ON", "NAME", "HONORIFIC_PREFIX", "GIVEN_NAME", "ADDITIONAL_NAME",
                                    "FAMILY_NAME", "HONORIFIC_SUFFIX", "NICKNAME", "EMAIL", "USERNAME", "NEW_PASSWORD",
                                    "CURRENT_PASSWORD", "ORGANIZATION_TITLE", "ORGANIZATION", "STREET_ADDRESS",
                                    "ADDRESS_LINE1", "ADDRESS_LINE2", "ADDRESS_LINE3", "ADDRESS_LEVEL1",
                                    "ADDRESS_LEVEL2", "ADDRESS_LEVEL3", "ADDRESS_LEVEL4", "COUNTRY", "COUNTRY_NAME",
                                    "POSTAL_CODE", "CC_NAME", "CC_GIVEN_NAME", "CC_ADDITIONAL_NAME", "CC_FAMILY_NAME",
                                    "CC_NUMBER", "CC_EXP", "CC_EXP_MONTH", "CC_EXP_YEAR", "CC_CSC", "CC_TYPE",
                                    "TRANSACTION_CURRENCY", "TRANSACTION_AMOUNT", "LANGUAGE", "BDAY", "BDAY_DAY",
                                    "BDAY_MONTH", "BDAY_YEAR", "SEX", "TEL", "TEL_COUNTRY_CODE", "TEL_NATIONAL",
                                    "TEL_AREA_CODE", "TEL_LOCAL", "TEL_LOCAL_PREFIX", "TEL_LOCAL_SUFFIX",
                                    "TEL_EXTENSION", "URL", "PHOTO",}),
                    @StudioProperty(xmlAttribute = "autocorrect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoselect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "datatype", type = StudioPropertyType.DATATYPE_ID,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "pattern", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
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
            xmlElement = "timePicker",
            icon = "io/jmix/flowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "datatype", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
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
            xmlElement = "valuePicker",
            icon = "io/jmix/flowui/kit/meta/icon/component/valuePicker.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
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
            xmlElement = "multiValuePicker",
            icon = "io/jmix/flowui/kit/meta/icon/component/multiValuePicker.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
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
            xmlElement = "loginForm",
            icon = "io/jmix/flowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "forgotPasswordButtonVisible", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "rememberMeVisible", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "localesVisible", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
            }
    )
    EnhancedLoginForm loginForm();

    @StudioComponent(
            name = "SimplePagination",
            classFqn = "io.jmix.flowui.component.pagination.SimplePagination",
            category = "Components",
            xmlElement = "simplePagination",
            icon = "io/jmix/flowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "dataLoader", type = StudioPropertyType.DATA_LOADER_REF),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsPerPageDefaultValue", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "itemsPerPageItems", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "itemsPerPageVisible", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "itemsPerPageUnlimitedItemVisible", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
            }
    )
    JmixSimplePagination simplePagination();
}
