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
import io.jmix.flowui.kit.component.codeeditor.JmixCodeEditor;
import io.jmix.flowui.kit.component.combobox.ComboBoxPicker;
import io.jmix.flowui.kit.component.combobutton.ComboButton;
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
import io.jmix.flowui.kit.component.valuepicker.MultiValuePicker;
import io.jmix.flowui.kit.component.valuepicker.ValuePicker;
import io.jmix.flowui.kit.meta.*;
import io.jmix.flowui.kit.meta.component.preview.StudioGenericComponentPreview;

@StudioUiKit
public interface StudioComponents {

    @StudioComponent(
            name = "GenericComponent",
            classFqn = "com.vaadin.flow.component.Component",
            category = "Components",
            xmlElement = "component",
            properties = {
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "class", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_CLASS, useAsInjectionType = true, required = true),
            }
    )
    StudioGenericComponentPreview genericComponent();

    @StudioComponent(
            name = "Fragment",
            classFqn = "io.jmix.flowui.fragment.Fragment",
            category = "Components",
            xmlElement = "fragment",
            icon = "io/jmix/flowui/kit/meta/icon/component/fragment.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "class", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.FRAGMENT_CLASS, useAsInjectionType = true, required = true)
            }
    )
    VerticalLayout fragment();

    @StudioComponent(
            name = "Avatar",
            classFqn = "com.vaadin.flow.component.avatar.Avatar",
            category = "Components",
            xmlElement = "avatar",
            icon = "io/jmix/flowui/kit/meta/icon/component/avatar.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/avatar.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "abbreviation", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colorIndex", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "image", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "name", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"xlarge", "large", "small", "xsmall"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    Avatar avatar();

    @StudioComponent(
            name = "Icon",
            classFqn = "com.vaadin.flow.component.icon.Icon",
            category = "Components",
            xmlElement = "icon",
            icon = "io/jmix/flowui/kit/meta/icon/component/icon.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/icon.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "svgIcon"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "fontIcon"),
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clickShortcut", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "color", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ICON, required = true),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "size", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    Icon icon();

    @StudioComponent(
            name = "SvgIcon",
            classFqn = "com.vaadin.flow.component.icon.SvgIcon",
            category = "Components",
            xmlElement = "svgIcon",
            icon = "io/jmix/flowui/kit/meta/icon/component/svgIcon.svg",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "icon"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "fontIcon")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clickShortcut", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "color", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "resource", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "size", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "symbol", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    SvgIcon svgIcon();

    @StudioComponent(
            name = "FontIcon",
            classFqn = "com.vaadin.flow.component.icon.FontIcon",
            category = "Components",
            xmlElement = "fontIcon",
            icon = "io/jmix/flowui/kit/meta/icon/component/fontIcon.svg",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "icon"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "svgIcon")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "charCode", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clickShortcut", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "color", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "fontFamily", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "iconClassNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "ligature", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "size", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    FontIcon fontIcon();

    @StudioComponent(
            name = "BigDecimalField",
            classFqn = "io.jmix.flowui.component.textfield.JmixBigDecimalField",
            category = "Components",
            xmlElement = "bigDecimalField",
            icon = "io/jmix/flowui/kit/meta/icon/component/bigDecimalField.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/bigDecimalField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "numberField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "integerField"),
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
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
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            documentationLink = "%VERSION%/flow-ui/vc/components/button.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "comboButton")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "action", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ACTION_REF,
                            classFqn = "io.jmix.flowui.kit.action.Action"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "disableOnClick", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = "iconAfterText", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline", "primary", "success", "warning", "error",
                                    "contrast", "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "whiteSpace", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    JmixButton button();

    @StudioComponent(
            name = "CheckboxGroup",
            classFqn = "io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup",
            category = "Components",
            xmlElement = "checkboxGroup",
            icon = "io/jmix/flowui/kit/meta/icon/component/checkBoxGroup.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/checkBoxGroup.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "radioButtonGroup")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "itemsEnum", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUM_CLASS,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"helper-above-field", "vertical"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            xmlElement = "checkbox",
            icon = "io/jmix/flowui/kit/meta/icon/component/checkbox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/checkbox.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clickShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "indeterminate", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "value", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            documentationLink = "%VERSION%/flow-ui/vc/components/comboBox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "select"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiSelectComboBox")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "datatype", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATATYPE_ID,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "overlayClass", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayWidth", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsEnum", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUM_CLASS,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "pattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-left", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            xmlElement = "multiSelectComboBox",
            icon = "io/jmix/flowui/kit/meta/icon/component/comboBox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/multiSelectComboBox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "select"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "comboBox")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "allowedCharPattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "autoExpand", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.combobox.MultiSelectComboBox$AutoExpandMode",
                            defaultValue = "NONE",
                            options = {"VERTICAL", "HORIZONTAL", "BOTH", "NONE"}),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayClass", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayWidth", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "itemsEnum", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUM_CLASS,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "metaClass", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENTITY_NAME,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "opened", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "selectedItemsOnTop", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-left", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            xmlElement = "multiSelectComboBoxPicker",
            icon = "io/jmix/flowui/kit/meta/icon/component/comboBox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/multiSelectComboBoxPicker.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "allowedCharPattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayClass", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayWidth", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "itemsEnum", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUM_CLASS,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "metaClass", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENTITY_NAME,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "opened", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-left", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            name = "DropdownButton",
            classFqn = "io.jmix.flowui.kit.component.dropdownbutton.DropdownButton",
            category = "Components",
            xmlElement = "dropdownButton",
            icon = "io/jmix/flowui/kit/meta/icon/component/dropdownButton.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/dropdownButton.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayClass", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dropdownIndicatorVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "openOnHover", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary-inline", "primary", "success", "warning", "error", "contrast",
                                    "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "whiteSpace", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    DropdownButton dropdownButton();

    @StudioComponent(
            name = "ComboButton",
            classFqn = "io.jmix.flowui.kit.component.combobutton.ComboButton",
            category = "Components",
            xmlElement = "comboButton",
            icon = "io/jmix/flowui/kit/meta/icon/component/comboButton.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/comboButton.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "button")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "action", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ACTION_REF),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayClass", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dropdownIcon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "shortcutCombination", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "openOnHover", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary-inline", "primary", "success", "warning", "error", "contrast",
                                    "icon", "contained", "outlined"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "whiteSpace", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    ComboButton comboButton();

    @StudioComponent(
            name = "DatePicker",
            classFqn = "io.jmix.flowui.component.datepicker.TypedDatePicker",
            category = "Components",
            xmlElement = "datePicker",
            icon = "io/jmix/flowui/kit/meta/icon/component/datePicker.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/datePicker.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "timePicker"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "dateTimePicker")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "allowedCharPattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayClass", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "datatype", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.core.metamodel.datatype.Datatype", options = {"date", "dateTime",
                            "localDateTime", "offsetDateTime", "localDate"}, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "dateFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "max", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "min", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "opened", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-left", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "weekNumbersVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            icon = "io/jmix/flowui/kit/meta/icon/component/datePicker.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/dateTimePicker.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "datePicker"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "timePicker")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayClass", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "datatype", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.core.metamodel.datatype.Datatype", options = {"date", "dateTime",
                            "localDateTime", "offsetTime", "localTime", "offsetDateTime", "time", "localDate"}, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "dateFormat", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "datePlaceholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "max", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "min", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "step", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "timePlaceholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "weekNumbersVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            icon = "io/jmix/flowui/kit/meta/icon/component/drawerToggle.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/drawerToggle.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "autoFocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clickShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    DrawerToggle drawerToggle();

    @StudioComponent(
            name = "EmailField",
            classFqn = "io.jmix.flowui.component.textfield.JmixEmailField",
            category = "Components",
            xmlElement = "emailField",
            icon = "io/jmix/flowui/kit/meta/icon/component/emailField.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/emailField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "allowedCharPattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
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
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxLength", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minLength", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "pattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            documentationLink = "%VERSION%/flow-ui/vc/components/entityComboBox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "comboBox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "entityPicker"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiSelectComboBoxPicker")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "allowedCharPattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayClass", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayWidth", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "metaClass", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENTITY_NAME,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "opened", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "pattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-left", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            xmlElement = "entityPicker",
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
                    }),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiSelectComboBoxPicker")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "metaClass", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENTITY_NAME,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            documentationLink = "%VERSION%/flow-ui/vc/components/dataGrid.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "treeDataGrid")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "aggregationPosition", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.grid.EnhancedDataGrid$AggregationPosition",
                            defaultValue = "BOTTOM", options = {"TOP", "BOTTOM"}),
                    @StudioProperty(xmlAttribute = "allRowsVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "columnReorderingAllowed", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, required = true,
                            typeParameter = "E"),
                    @StudioProperty(xmlAttribute = "detailsVisibleOnClick", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dropMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.dnd.GridDropMode", options = {"BETWEEN", "ON_TOP",
                            "ON_TOP_OR_BETWEEN", "ON_GRID"}),
                    @StudioProperty(xmlAttribute = "emptyStateText", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "metaClass", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENTITY_NAME, typeParameter = "E"),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE,
                            type = StudioPropertyType.SIZE, options = {"AUTO", "100%"},
                            initialValue = "100px"),
                    @StudioProperty(xmlAttribute = "multiSort", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "multiSortOnShiftClickOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "multiSortPriority", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.Grid.MultiSortPriority", defaultValue = "PREPEND",
                            options = {"APPEND", "PREPEND"}),
                    @StudioProperty(xmlAttribute = "nestedNullBehavior", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.Grid.NestedNullBehavior", defaultValue = "THROW",
                            options = {"THROW", "ALLOW_NULLS"}),
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER, defaultValue = "50"),
                    @StudioProperty(xmlAttribute = "rowsDraggable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "selectionMode", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.Grid.SelectionMode", defaultValue = "SINGLE",
                            options = {"SINGLE", "MULTI", "NONE"}),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"no-border", "no-row-borders", "column-borders", "row-stripes",
                                    "compact", "wrap-cell-content", "column-dividers"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, initialValue = "100%"),
                    @StudioProperty(xmlAttribute = "editorBuffered", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "columnRendering", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.ColumnRendering", defaultValue = "EAGER",
                            options = {"EAGER", "LAZY"})
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
            documentationLink = "%VERSION%/flow-ui/vc/components/treeDataGrid.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "dataGrid")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "allRowsVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "columnReorderingAllowed", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF,
                            required = true, typeParameter = "E"),
                    @StudioProperty(xmlAttribute = "detailsVisibleOnClick", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "dropMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.dnd.GridDropMode", options = {"BETWEEN", "ON_TOP",
                            "ON_TOP_OR_BETWEEN", "ON_GRID"}),
                    @StudioProperty(xmlAttribute = "emptyStateText", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "hierarchyProperty", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.PROPERTY_REF,
                            required = true, typeParameter = "E"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "metaClass", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENTITY_NAME,
                            typeParameter = "E"),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE,
                            type = StudioPropertyType.SIZE, options = {"AUTO", "100%"},
                            initialValue = "100px"),
                    @StudioProperty(xmlAttribute = "multiSort", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "multiSortOnShiftClickOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "multiSortPriority", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.Grid.MultiSortPriority", defaultValue = "PREPEND",
                            options = {"APPEND", "PREPEND"}),
                    @StudioProperty(xmlAttribute = "nestedNullBehavior", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.Grid.NestedNullBehavior", defaultValue = "THROW",
                            options = {"THROW", "ALLOW_NULLS"}),
                    @StudioProperty(xmlAttribute = "pageSize", type = StudioPropertyType.INTEGER, defaultValue = "50"),
                    @StudioProperty(xmlAttribute = "rowsDraggable", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "selectionMode", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.Grid.SelectionMode", defaultValue = "SINGLE",
                            options = {"SINGLE", "MULTI", "NONE"}),
                    @StudioProperty(xmlAttribute = "showOrphans", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"no-border", "no-row-borders", "column-borders", "row-stripes",
                                    "compact", "wrap-cell-content", "column-dividers"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, initialValue = "100%"),
                    @StudioProperty(xmlAttribute = "editorBuffered", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "columnRendering", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.grid.ColumnRendering", defaultValue = "EAGER",
                            options = {"EAGER", "LAZY"})
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
            name = "IntegerField",
            classFqn = "io.jmix.flowui.component.textfield.JmixIntegerField",
            category = "Components",
            xmlElement = "integerField",
            icon = "io/jmix/flowui/kit/meta/icon/component/integerField.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/integerField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "bigDecimalField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "numberField")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
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
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "max", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "min", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "step", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "stepButtonsVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            xmlElement = "numberField",
            icon = "io/jmix/flowui/kit/meta/icon/component/numberField.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/numberField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "integerField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "bigDecimalField")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "allowedCharPattern", type = StudioPropertyType.LOCALIZED_STRING),
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
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "max", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "min", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "step", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "stepButtonsVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            classFqn = "io.jmix.flowui.component.textfield.JmixPasswordField",
            category = "Components",
            xmlElement = "passwordField",
            icon = "io/jmix/flowui/kit/meta/icon/component/passwordField.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/passwordField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "allowedCharPattern", type = StudioPropertyType.LOCALIZED_STRING),
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
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "pattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "revealButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            documentationLink = "%VERSION%/flow-ui/vc/components/progressBar.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "indeterminate", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "max", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "min", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.DOUBLE),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"contrast", "error", "success"}),
                    @StudioProperty(xmlAttribute = "value", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    ProgressBar progressBar();

    @StudioComponent(
            name = "RadioButtonGroup",
            classFqn = "io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup",
            category = "Components",
            xmlElement = "radioButtonGroup",
            icon = "io/jmix/flowui/kit/meta/icon/component/radioButtonGroup.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/radioButtonGroup.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "checkBoxGroup")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "datatype", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATATYPE_ID,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "itemsEnum", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUM_CLASS, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"vertical", "helper-above-field"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            xmlElement = "select",
            icon = "io/jmix/flowui/kit/meta/icon/component/select.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/select.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "comboBox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "entityComboBox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiSelectComboBox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiSelectComboBoxPicker")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "datatype", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATATYPE_ID,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "overlayClass", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayWidth", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "noVerticalOverlap", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "emptySelectionAllowed", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "emptySelectionCaption", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "itemsEnum", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUM_CLASS),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-left", "align-center", "align-right", "helper-above-field"}),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 0
                    )
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
            classFqn = "com.vaadin.flow.component.tabs.Tabs",
            category = "Components",
            xmlElement = "tabs",
            icon = "io/jmix/flowui/kit/meta/icon/component/tabs.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/tabs.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "orientation", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.tabs.Tabs$Orientation",
                            setMethod = "setOrientation", defaultValue = "HORIZONTAL",
                            options = {"VERTICAL", "HORIZONTAL"}),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"icon-on-top", "centered", "small", "minimal",
                                    "hide-scroll-buttons", "equal-width-tabs"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    Tabs tabs();

    @StudioComponent(
            name = "ListBox",
            classFqn = "io.jmix.flowui.component.listbox.JmixListBox",
            category = "Components",
            xmlElement = "listBox",
            icon = "io/jmix/flowui/kit/meta/icon/component/listBox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/listBox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiSelectListBox")
            }),
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 0
                    )
            ),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "itemsEnum", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUM_CLASS,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            xmlElement = "multiSelectListBox",
            icon = "io/jmix/flowui/kit/meta/icon/component/listBox.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/multiSelectListBox.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "listBox")
            }),
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 0
                    )
            ),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "itemsEnum", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUM_CLASS,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            xmlElement = "textArea",
            icon = "io/jmix/flowui/kit/meta/icon/component/textArea.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/textArea.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "codeEditor"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "richTextEditor")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "allowedCharPattern", type = StudioPropertyType.LOCALIZED_STRING),
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
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxLength", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxRows", category = StudioProperty.Category.LOOK_AND_FEEL,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minLength", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minRows", category = StudioProperty.Category.LOOK_AND_FEEL,
                            type = StudioPropertyType.INTEGER, defaultValue = "2"),
                    @StudioProperty(xmlAttribute = "pattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "trimEnabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true",
                            defaultValueRef = "application_property:jmix.ui.component.default-trim-enabled"),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "value", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "allowedCharPattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
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
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "datatype", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATATYPE_ID,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxLength", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minLength", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "pattern", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "trimEnabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true",
                            defaultValueRef = "application_property:jmix.ui.component.default-trim-enabled"),
                    @StudioProperty(xmlAttribute = "value", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            icon = "io/jmix/flowui/kit/meta/icon/component/timePicker.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/timePicker.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "datePicker"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "dateTimePicker"),
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "allowedCharPattern", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "autoOpen", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlayClass", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "datatype", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.core.metamodel.datatype.Datatype",
                            options = {"offsetTime", "localTime", "time"}, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "max", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "min", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "step", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            documentationLink = "%VERSION%/flow-ui/vc/components/valuePicker.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            documentationLink = "%VERSION%/flow-ui/vc/components/multiValuePicker.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "allowCustomValue", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
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
            icon = "io/jmix/flowui/kit/meta/icon/component/loginForm.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/loginForm.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "forgotPasswordButtonVisible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "rememberMeVisible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "localesVisible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }
    )
    EnhancedLoginForm loginForm();

    @StudioComponent(
            name = "LoginOverlay",
            classFqn = "com.vaadin.flow.component.login.LoginOverlay",
            category = "Components",
            xmlElement = "loginOverlay",
            icon = "io/jmix/flowui/kit/meta/icon/component/loginOverlay.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "forgotPasswordButtonVisible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "opened", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true")
            }

    )
    LoginOverlay loginOverlay();

    @StudioComponent(
            name = "SimplePagination",
            classFqn = "io.jmix.flowui.component.pagination.SimplePagination",
            category = "Components",
            xmlElement = "simplePagination",
            icon = "io/jmix/flowui/kit/meta/icon/component/simplePagination.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/simplePagination.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "dataLoader", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATA_LOADER_REF),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsPerPageDefaultValue", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "itemsPerPageItems", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "itemsPerPageVisible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "itemsPerPageUnlimitedItemVisible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "autoLoad", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
            }
    )
    JmixSimplePagination simplePagination();

    @StudioComponent(
            name = "Upload",
            classFqn = "io.jmix.flowui.component.upload.JmixUpload",
            category = "Components",
            xmlElement = "upload",
            icon = "io/jmix/flowui/kit/meta/icon/component/upload.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/upload.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "acceptedFileTypes", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "autoUpload", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dropAllowed", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "dropLabel", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "dropLabelIcon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxFiles", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "maxFileSize", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "receiverFqn", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "receiverType", type = StudioPropertyType.ENUMERATION,
                            options = {"MEMORY_BUFFER", "MULTI_FILE_MEMORY_BUFFER", "FILE_TEMPORARY_STORAGE_BUFFER",
                                    "MULTI_FILE_TEMPORARY_STORAGE_BUFFER"}, defaultValue = "MEMORY_BUFFER"),
                    @StudioProperty(xmlAttribute = "uploadText", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "uploadIcon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    Upload upload();

    @StudioComponent(
            name = "FileUploadField",
            classFqn = "io.jmix.flowui.component.upload.FileUploadField",
            category = "Components",
            xmlElement = "fileUploadField",
            icon = "io/jmix/flowui/kit/meta/icon/component/upload.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/fileUploadField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "fileStorageUploadField")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "acceptedFileTypes", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "clearButtonAriaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "connectingStatusText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "dropAllowed", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "fileName", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "fileNameVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "fileNotSelectedText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "fileTooBigText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "incorrectFileTypeText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxFileSize", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "processingStatusText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "remainingTimeText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "remainingTimeUnknownText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "uploadDialogCancelText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "uploadDialogTitle", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "uploadIcon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = "uploadText", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            xmlElement = "fileStorageUploadField",
            icon = "io/jmix/flowui/kit/meta/icon/component/upload.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/fileStorageUploadField.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "fileUploadField")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "acceptedFileTypes", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "clearButtonAriaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "connectingStatusText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "dropAllowed", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "fileNameVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "fileNotSelectedText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "fileStorageName", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "fileStoragePutMode", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            options = {"MANUAL", "IMMEDIATE"}, defaultValue = "IMMEDIATE"),
                    @StudioProperty(xmlAttribute = "fileTooBigText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "incorrectFileTypeText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxFileSize", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "processingStatusText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "remainingTimeText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "remainingTimeUnknownText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "uploadDialogCancelText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "uploadDialogTitle", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "uploadIcon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON),
                    @StudioProperty(xmlAttribute = "uploadText", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            xmlElement = "genericFilter",
            icon = "io/jmix/flowui/kit/meta/icon/component/genericFilter.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/genericFilter.html",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "autoApply", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true", defaultValueRef = "application_property:jmix.ui.component.filter-auto-apply"),
                    @StudioProperty(xmlAttribute = "applyShortcut", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataLoader", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATA_LOADER_REF,
                            required = true),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "opened", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "propertyHierarchyDepth", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "summaryText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "Filter"),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"filled", "reverse", "small"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    Details genericFilter();

    @StudioComponent(
            name = "PropertyFilter",
            classFqn = "io.jmix.flowui.component.propertyfilter.PropertyFilter",
            category = "Components",
            xmlElement = "propertyFilter",
            icon = "io/jmix/flowui/kit/meta/icon/component/propertyFilter.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/genericFilter-components.html#property-filter",
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = StudioAvailableChildrenInfo.ANY_TAG,
                            maxCount = 1
                    )
            ),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "autoApply", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataLoader", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATA_LOADER_REF,
                            required = true),
                    @StudioProperty(xmlAttribute = "defaultValue", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "labelPosition", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.SupportsLabelPosition$LabelPosition",
                            options = {"ASIDE", "TOP"}, defaultValue = "ASIDE"),
                    @StudioProperty(xmlAttribute = "labelVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "labelWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "operation", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.propertyfilter.PropertyFilter$Operation",
                            options = {"EQUAL", "NOT_EQUAL", "GREATER",
                                    "GREATER_OR_EQUAL", "LESS", "LESS_OR_EQUAL", "CONTAINS", "NOT_CONTAINS",
                                    "STARTS_WITH", "ENDS_WITH", "IS_SET", "IN_LIST", "NOT_IN_LIST", "IN_INTERVAL",
                                    "IS_COLLECTION_EMPTY", "MEMBER_OF_COLLECTION",
                                    "NOT_MEMBER_OF_COLLECTION"}, required = true),
                    @StudioProperty(xmlAttribute = "operationEditable", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "operationTextVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "parameterName", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
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
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    HorizontalLayout propertyFilter();

    @StudioComponent(
            name = "JpqlFilter",
            classFqn = "io.jmix.flowui.component.jpqlfilter.JpqlFilter",
            category = "Components",
            xmlElement = "jpqlFilter",
            icon = "io/jmix/flowui/kit/meta/icon/component/jpqlFilter.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/genericFilter-components.html#jpql-filter",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "autoApply", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataLoader", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATA_LOADER_REF,
                            required = true),
                    @StudioProperty(xmlAttribute = "defaultValue", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "hasInExpression", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "labelPosition", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.SupportsLabelPosition$LabelPosition",
                            options = {"ASIDE", "TOP"}, defaultValue = "ASIDE"),
                    @StudioProperty(xmlAttribute = "labelVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "labelWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "parameterClass", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING,
                            typeParameter = "V", required = true),
                    @StudioProperty(xmlAttribute = "parameterName", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
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
    HorizontalLayout jpqlFilter();

    @StudioComponent(
            name = "GroupFilter",
            classFqn = "io.jmix.flowui.component.logicalfilter.GroupFilter",
            category = "Components",
            xmlElement = "groupFilter",
            icon = "io/jmix/flowui/kit/meta/icon/component/groupFilter.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/genericFilter-components.html#group-filter",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "autoApply", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataLoader", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATA_LOADER_REF,
                            required = true),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "operation", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.logicalfilter.LogicalFilterComponent$Operation",
                            options = {"AND", "OR"}, required = true),
                    @StudioProperty(xmlAttribute = "operationTextVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "summaryText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
            }

    )
    HorizontalLayout groupFilter();

    @StudioComponent(
            name = "Layout",
            xmlElement = "layout",
            icon = "io/jmix/flowui/kit/meta/icon/view/layout.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "100%"),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}, defaultValue = "100%"),
                    @StudioProperty(xmlAttribute = "margin", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "padding", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "spacing", category = StudioProperty.Category.POSITION, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "expand", category = StudioProperty.Category.POSITION, type = StudioPropertyType.COMPONENT_REF),
                    @StudioProperty(xmlAttribute = "alignItems", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "START",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "boxSizing", category = StudioProperty.Category.SIZE, type = StudioPropertyType.ENUMERATION,
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
                            classFqn = "com.vaadin.flow.component.orderedlayout.BoxSizing",
                            defaultValue = "UNDEFINED",
                            options = {"CONTENT_BOX", "BORDER_BOX", "UNDEFINED"}),
                    @StudioProperty(xmlAttribute = "justifyContent", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            setMethod = "setJustifyContentMode",
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
                            setParameterFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$JustifyContentMode",
                            defaultValue = "START",
                            options = {"START", "END", "CENTER", "BETWEEN", "AROUND", "EVENLY"}),

            }
    )
    VerticalLayout layout();

    @StudioComponent(
            name = "View",
            xmlElement = "view",
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
            properties = {
                    @StudioProperty(xmlAttribute = "focusComponent", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_REF),
                    @StudioProperty(xmlAttribute = "focusMode", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.VALUES_LIST,
                            options = {"NO_FOCUS", "AUTO"}, defaultValue = "AUTO"),
                    @StudioProperty(xmlAttribute = "extends", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "messagesGroup", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "title", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    VerticalLayout view();

    @StudioComponent(
            name = "CodeEditor",
            classFqn = "io.jmix.flowui.component.codeeditor.CodeEditor",
            category = "Components",
            xmlElement = "codeEditor",
            icon = "io/jmix/flowui/kit/meta/icon/component/codeEditor.svg",
            documentationLink = "%VERSION%/flow-ui/vc/components/codeEditor.html",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textArea"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "richTextEditor")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "fontSize", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.SIZE,
                            defaultValue = "1rem"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "highlightActiveLine", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "highlightGutterLine", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "mode", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.codeeditor.CodeEditorMode",
                            options = {"ABAP", "ABC", "ACTIONSCRIPT", "ADA", "ALDA", "APACHE_CONF", "APEX",
                                    "APPLESCRIPT", "AQL", "ASCIIDOC", "ASL", "ASSEMBLY_X_86", "AUTOHOTKEY", "BATCHFILE",
                                    "BIBTEX", "C_9_SEARCH", "C_CPP", "CIRRU", "CLOJURE", "COBOL", "COFFEE",
                                    "COLDFUSION", "CRYSTAL", "CSHARP", "CSOUND_DOCUMENT", "CSOUND_ORCHESTRA",
                                    "CSOUND_SCORE", "CSP", "CSS", "CURLY", "D", "DART", "DIFF", "DJANGO", "DOCKERFILE",
                                    "DOT", "DROOLS", "EDIFACT", "EIFFEL", "EJS", "ELIXIR", "ELM", "ERLANG", "FORTH",
                                    "FORTRAN", "FSHARP", "FSL", "FTL", "GCODE", "GHERKIN", "GITIGNORE", "GLSL",
                                    "GOBSTONES", "GOLANG", "GRAPHQLSCHEMA", "GROOVY", "HAML", "HANDLEBARS", "HASKELL",
                                    "HASKELL_CABAL", "HAXE", "HJSON", "HTML", "HTML_ELIXIR", "HTML_RUBY", "INI", "IO",
                                    "ION", "JACK", "JADE", "JAVA", "JAVASCRIPT", "JEXL", "JSON", "JSON_5", "JSONIQ",
                                    "JSP", "JSSM", "JSX", "JULIA", "KOTLIN", "LATEX", "LATTE", "LESS", "LIQUID", "LISP",
                                    "LIVESCRIPT", "LOGIQL", "LOGTALK", "LSL", "LUA", "LUAPAGE", "LUCENE", "MAKEFILE",
                                    "MARKDOWN", "MASK", "MATLAB", "MAZE", "MEDIAWIKI", "MEL", "MIPS", "MIXAL",
                                    "MUSHCODE", "MYSQL", "NGINX", "NIM", "NIX", "NSIS", "NUNJUCKS", "OBJECTIVEC",
                                    "OCAML", "PARTIQL", "PASCAL", "PERL", "PGSQL", "PHP", "PHP_LARAVEL_BLADE", "PIG",
                                    "PLAIN_TEXT", "PLSQL", "POWERSHELL", "PRAAT", "PRISMA", "PROLOG", "PROPERTIES",
                                    "PROTOBUF", "PUPPET", "PYTHON", "QML", "R", "RAKU", "RAZOR", "RDOC", "RED",
                                    "REDSHIFT", "RHTML", "ROBOT", "RST", "RUBY", "RUST", "SAC", "SASS", "SCAD",
                                    "SCALA", "SCHEME", "SCRYPT", "SCSS", "SH", "SJS", "SLIM", "SMARTY", "SMITHY",
                                    "SNIPPETS", "SOY_TEMPLATE", "SPACE", "SPARQL", "SQL", "SQLSERVER", "STYLUS",
                                    "SVG", "SWIFT", "TCL", "TERRAFORM", "TEX", "TEXT", "TEXTILE", "TOML", "TSX",
                                    "TURTLE", "TWIG", "TYPESCRIPT", "VALA", "VBSCRIPT", "VELOCITY", "VERILOG", "VHDL",
                                    "VISUALFORCE", "WOLLOK", "XML", "XQUERY", "YAML", "ZEEK"}),
                    @StudioProperty(xmlAttribute = "printMarginColumn", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.INTEGER,
                            defaultValue = "80"),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "showGutter", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "showLineNumbers", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "showPrintMargin", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "textWrap", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "useSoftTabs", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "theme", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.codeeditor.CodeEditorTheme",
                            options = {"AMBIANCE", "CHAOS", "CHROME", "CLOUD_9_DAY", "CLOUD_9_NIGHT",
                                    "CLOUD_9_NIGHT_LOW_COLOR", "CLOUDS", "CLOUDS_MIDNIGHT", "COBALT", "CRIMSON_EDITOR",
                                    "DAWN", "DRACULA", "DREAMWEAVER", "ECLIPSE", "GITHUB", "GOB", "GRUVBOX",
                                    "GRUVBOX_DARK_HARD", "GRUVBOX_LIGHT_HARD", "IDLE_FINGERS", "IPLASTIC",
                                    "KATZENMILCH", "KR_THEME", "KUROIR", "MERBIVORE", "MERBIVORE_SOFT",
                                    "MONO_INDUSTRIAL", "MONOKAI", "NORD_DARK", "ONE_DARK", "PASTEL_ON_DARK",
                                    "SOLARIZED_DARK", "SOLARIZED_LIGHT", "SQLSERVER", "TERMINAL", "TEXTMATE",
                                    "TOMORROW", "TOMORROW_NIGHT", "TOMORROW_NIGHT_BLUE", "TOMORROW_NIGHT_BRIGHT",
                                    "TOMORROW_NIGHT_EIGHTIES", "TWILIGHT", "VIBRANT_INK", "XCODE"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            xmlElement = "virtualList",
            icon = "io/jmix/flowui/kit/meta/icon/component/virtualList.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer", category = StudioProperty.Category.DATA_BINDING,
                            type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF, typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "itemsEnum", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENUM_CLASS,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            },
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
            xmlElement = "twinColumn",
            icon = "io/jmix/flowui/kit/meta/icon/component/twinColumn.svg",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "checkboxGroup"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiSelectComboBox"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "multiValuePicker")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO", options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "itemsContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "itemsColumnLabel", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "reorderable", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "selectAllButtonsVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "selectedItemsColumnLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"no-border", "no-row-border", "checkmarks", "no-space-between-actions"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
            },
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
            xmlElement = "gridColumnVisibility",
            icon = "io/jmix/flowui/kit/meta/icon/component/gridColumnVisibility.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataGrid", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_REF,
                            componentRefTags = {"dataGrid", "treeDataGrid"}, required = true),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "exclude", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "hideAllEnabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ICON, initialValue = "COG", defaultValue = "COG"),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "include", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "showAllEnabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "text", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline", "primary", "success", "warning", "error",
                                    "contrast", "icon", "contained", "outlined"}, initialValue = "icon"),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "whiteSpace", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.HasText$WhiteSpace", defaultValue = "NORMAL",
                            options = {"NORMAL", "NOWRAP", "PRE", "PRE_WRAP", "PRE_LINE", "BREAK_SPACES", "INHERIT",
                                    "INITIAL"}),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    JmixMenuBar gridColumnVisibility();

    @StudioComponent(
            name = "MenuFilterField",
            classFqn = "io.jmix.flowui.component.menufilterfield.MenuFilterField",
            category = "Components",
            xmlElement = "menuFilterField",
            icon = "io/jmix/flowui/kit/meta/icon/component/menufilterfield.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoselect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "menu", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_REF,
                            componentRefTags = {"listMenu", "horizontalMenu"}, required = true),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "filterMode", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.menufilterfield.MenuFilterField$FilterMode",
                            defaultValue = "CASE_INSENSITIVE", options = {"CASE_INSENSITIVE", "CASE_SENSITIVE"}),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    TextField menuFilterField();

    @StudioComponent(
            name = "RichTextEditor",
            classFqn = "io.jmix.flowui.component.richtexteditor.RichTextEditor",
            category = "Components",
            xmlElement = "richTextEditor",
            icon = "io/jmix/flowui/kit/meta/icon/component/richTextEditor.svg",
            convertStrategy = @StudioConvertStrategy(tagsToConvertInto = {
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textArea"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "textField"),
                    @StudioConvertStrategy.TagInfo(qualifiedName = "codeEditor")
            }),
            properties = {
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"no-border", "compact"}),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "dataContainer", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "property", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.PROPERTY_REF)
            }
    )
    JmixRichTextEditor richTextEditor();

    @StudioComponent(
            name = "HorizontalMenu",
            classFqn = "io.jmix.flowui.component.horizontalmenu.HorizontalMenu",
            category = "Components",
            xmlElement = "horizontalMenu",
            icon = "io/jmix/flowui/kit/meta/icon/component/horizontalMenu.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "loadMenuConfig", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "tabNavigation", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    JmixMenuBar horizontalMenu();

    @StudioComponent(
            name = "ListMenu",
            classFqn = "io.jmix.flowui.component.main.JmixListMenu",
            category = "Components",
            xmlElement = "listMenu",
            icon = "io/jmix/flowui/kit/meta/icon/mainview/listMenu.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "loadMenuConfig", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "metaClass", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.ENTITY_NAME),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"toggle-reverse"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    ListMenu listMenu();

    @StudioComponent(
            name = "Html",
            classFqn = "com.vaadin.flow.component.Html",
            category = "Components",
            xmlElement = "html",
            icon = "io/jmix/flowui/kit/meta/icon/html/htmlComponent.svg",
            documentationLink = "%VERSION%/flow-ui/vc/html.html",
            availableChildren = @StudioAvailableChildrenInfo(
                    availableTags = @StudioAvailableChildrenInfo.TagInfo(
                            qualifiedName = "content",
                            maxCount = 1
                    )
            ),
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "content", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "file", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER)
            }
    )
    Html html();
}
