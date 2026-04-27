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

import io.jmix.flowui.kit.meta.StudioElement;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
interface StudioValidatorsElements {

    @StudioElement(
            name = "Custom",
            classFqn = "io.jmix.flowui.component.validation.Validator",
            xmlElement = StudioXmlElements.CUSTOM,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#custom-validator",
            propertyGroups = {
                    StudioPropertyGroups.Bean.class,
                    StudioPropertyGroups.Message.class
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.BEAN,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.STRING,
                            required = true
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void custom();

    @StudioElement(
            name = "DecimalMax",
            classFqn = "io.jmix.flowui.component.validation.DecimalMaxValidator",
            xmlElement = StudioXmlElements.DECIMAL_MAX,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#DecimalMaxValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = StudioPropertyGroups.MessageAndInclusiveRequiredBigDecimalValue.class,
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.VALUE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.BIG_DECIMAL,
                            required = true
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.INCLUSIVE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.BOOLEAN
                    )
            }
    )
    void decimalMax();

    @StudioElement(
            name = "DecimalMin",
            classFqn = "io.jmix.flowui.component.validation.DecimalMinValidator",
            xmlElement = StudioXmlElements.DECIMAL_MIN,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#DecimalMinValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = StudioPropertyGroups.MessageAndInclusiveRequiredBigDecimalValue.class,
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.VALUE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.BIG_DECIMAL,
                            required = true
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.INCLUSIVE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.BOOLEAN
                    )
            }
    )
    void decimalMin();

    @StudioElement(
            name = "Digits",
            classFqn = "io.jmix.flowui.component.validation.DigitsValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            xmlElement = StudioXmlElements.DIGITS,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#DigitsValidator",
            propertyGroups = StudioPropertyGroups.DigitsComponent.class,
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.INTEGER,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.INTEGER,
                            required = true
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FRACTION,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.INTEGER,
                            required = true
                    )
            }
    )
    void digits();

    @StudioElement(
            name = "DoubleMax",
            classFqn = "io.jmix.flowui.component.validation.DoubleMaxValidator",
            xmlElement = StudioXmlElements.DOUBLE_MAX,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#DoubleMaxValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = StudioPropertyGroups.MessageAndInclusiveRequiredDoubleValue.class,
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.VALUE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.DOUBLE,
                            required = true
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.INCLUSIVE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.BOOLEAN
                    )
            }
    )
    void doubleMax();

    @StudioElement(
            name = "DoubleMin",
            classFqn = "io.jmix.flowui.component.validation.DoubleMinValidator",
            xmlElement = StudioXmlElements.DOUBLE_MIN,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#DoubleMinValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = StudioPropertyGroups.MessageAndInclusiveRequiredDoubleValue.class,
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.VALUE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.DOUBLE,
                            required = true
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.INCLUSIVE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.BOOLEAN
                    )
            }
    )
    void doubleMin();

    @StudioElement(
            name = "Email",
            classFqn = "io.jmix.flowui.component.validation.EmailValidator",
            xmlElement = StudioXmlElements.EMAIL,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#EmailValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = {
                    StudioPropertyGroups.Message.class
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void email();

    @StudioElement(
            name = "Future",
            classFqn = "io.jmix.flowui.component.validation.FutureValidator",
            xmlElement = StudioXmlElements.FUTURE,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#FutureValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.textfield.TypedTextField",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.combobox.JmixComboBox",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.PickerComponent",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = StudioPropertyGroups.MessageAndCheckSeconds.class,
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CHECK_SECONDS,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"
                    )
            }
    )
    void future();

    @StudioElement(
            name = "FutureOrPresent",
            classFqn = "io.jmix.flowui.component.validation.FutureOrPresentValidator",
            xmlElement = StudioXmlElements.FUTURE_OR_PRESENT,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#FutureOrPresentValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.textfield.TypedTextField",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.combobox.JmixComboBox",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.PickerComponent",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = StudioPropertyGroups.MessageAndCheckSeconds.class,
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CHECK_SECONDS,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"
                    )
            }
    )
    void futureOrPresent();

    @StudioElement(
            name = "Max",
            classFqn = "io.jmix.flowui.component.validation.MaxValidator",
            xmlElement = StudioXmlElements.MAX,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#MaxValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = StudioPropertyGroups.MessageAndRequiredIntegerValue.class,
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.VALUE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.INTEGER,
                            required = true
                    )
            }
    )
    void max();

    @StudioElement(
            name = "Min",
            classFqn = "io.jmix.flowui.component.validation.MinValidator",
            xmlElement = StudioXmlElements.MIN,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#MinValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = StudioPropertyGroups.MessageAndRequiredIntegerValue.class,
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.VALUE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.INTEGER,
                            required = true
                    )
            }
    )
    void min();

    @StudioElement(
            name = "NegativeOrZero",
            classFqn = "io.jmix.flowui.component.validation.NegativeOrZeroValidator",
            xmlElement = StudioXmlElements.NEGATIVE_OR_ZERO,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#NegativeOrZeroValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = {
                    StudioPropertyGroups.Message.class
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void negativeOrZero();

    @StudioElement(
            name = "Negative",
            classFqn = "io.jmix.flowui.component.validation.NegativeValidator",
            xmlElement = StudioXmlElements.NEGATIVE,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#NegativeValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = {
                    StudioPropertyGroups.Message.class
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void negative();

    @StudioElement(
            name = "NotBlank",
            classFqn = "io.jmix.flowui.component.validation.NotBlankValidator",
            xmlElement = StudioXmlElements.NOT_BLANK,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#NotBlankValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = {
                    StudioPropertyGroups.Message.class
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void notBlank();

    @StudioElement(
            name = "NotEmpty",
            classFqn = "io.jmix.flowui.component.validation.NotEmptyValidator",
            xmlElement = StudioXmlElements.NOT_EMPTY,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#NotEmptyValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = {
                    StudioPropertyGroups.Message.class
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void notEmpty();

    @StudioElement(
            name = "NotNull",
            classFqn = "io.jmix.flowui.component.validation.NotNullValidator",
            xmlElement = StudioXmlElements.NOT_NULL,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#NotNullValidator",
            unsupportedTarget = "io.jmix.flowui.component.twincolumn.TwinColumn",
            propertyGroups = {
                    StudioPropertyGroups.Message.class
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void notNull();

    @StudioElement(
            name = "PastOrPresent",
            classFqn = "io.jmix.flowui.component.validation.PastOrPresentValidator",
            xmlElement = StudioXmlElements.PAST_OR_PRESENT,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#PastOrPresentValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.textfield.TypedTextField",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.combobox.JmixComboBox",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.PickerComponent",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = StudioPropertyGroups.MessageAndCheckSeconds.class,
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CHECK_SECONDS,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"
                    )
            }
    )
    void pastOrPresent();

    @StudioElement(
            name = "Past",
            classFqn = "io.jmix.flowui.component.validation.PastValidator",
            xmlElement = StudioXmlElements.PAST,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#PastValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.textfield.TypedTextField",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.combobox.JmixComboBox",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.PickerComponent",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = StudioPropertyGroups.MessageAndCheckSeconds.class,
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CHECK_SECONDS,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"
                    )
            }
    )
    void past();

    @StudioElement(
            name = "PositiveOrZero",
            classFqn = "io.jmix.flowui.component.validation.PositiveOrZeroValidator",
            xmlElement = StudioXmlElements.POSITIVE_OR_ZERO,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#PositiveOrZeroValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = {
                    StudioPropertyGroups.Message.class
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void positiveOrZero();

    @StudioElement(
            name = "Positive",
            classFqn = "io.jmix.flowui.component.validation.PositiveValidator",
            xmlElement = StudioXmlElements.POSITIVE,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#PositiveValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixEmailField",
                    "io.jmix.flowui.component.textarea.JmixTextArea",
                    "io.jmix.flowui.component.textfield.JmixPasswordField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = {
                    StudioPropertyGroups.Message.class
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void positive();

    @StudioElement(
            name = "Regexp",
            classFqn = "io.jmix.flowui.component.validation.RegexpValidator",
            xmlElement = StudioXmlElements.REGEXP,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#RegexpValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox",
                    "io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = StudioPropertyGroups.RegexpComponent.class,
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.REGEXP,
                            type = StudioPropertyType.STRING,
                            required = true
                    )
            }
    )
    void regexp();

    @StudioElement(
            name = "Size",
            classFqn = "io.jmix.flowui.component.validation.SizeValidator",
            xmlElement = StudioXmlElements.SIZE,
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            documentationLink = "%VERSION%/flow-ui/vc/miscellaneous/validator.html#SizeValidator",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField",
                    "io.jmix.flowui.component.twincolumn.TwinColumn"
            },
            propertyGroups = {
                    StudioPropertyGroups.Message.class,
                    StudioPropertyGroups.IntegerMin.class,
                    StudioPropertyGroups.IntegerMax.class
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MESSAGE,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MIN,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.INTEGER
                    ),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MAX,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.INTEGER
                    )
            }
    )
    void size();
}
