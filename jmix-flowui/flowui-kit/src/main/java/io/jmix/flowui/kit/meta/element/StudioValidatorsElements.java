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

@StudioUiKit
public interface StudioValidatorsElements {

    @StudioElement(
            name = "Custom",
            classFqn = "io.jmix.flowui.component.validation.Validator",
            xmlElement = "custom",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            properties = {
                    @StudioProperty(
                            xmlAttribute = "bean",
                            type = StudioPropertyType.STRING,
                            required = true
                    ),
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void custom();

    @StudioElement(
            name = "DecimalMax",
            classFqn = "io.jmix.flowui.component.validation.DecimalMaxValidator",
            xmlElement = "decimalMax",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "value",
                            type = StudioPropertyType.STRING,
                            required = true
                    ),
                    @StudioProperty(
                            xmlAttribute = "inclusive",
                            type = StudioPropertyType.BOOLEAN
                    )
            }
    )
    void decimalMax();

    @StudioElement(
            name = "DecimalMin",
            classFqn = "io.jmix.flowui.component.validation.DecimalMinValidator",
            xmlElement = "decimalMin",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "value",
                            type = StudioPropertyType.STRING,
                            required = true
                    ),
                    @StudioProperty(
                            xmlAttribute = "inclusive",
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
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            xmlElement = "digits",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "integer",
                            type = StudioPropertyType.INTEGER,
                            required = true
                    ),
                    @StudioProperty(
                            xmlAttribute = "fraction",
                            type = StudioPropertyType.INTEGER
                    )
            }
    )
    void digits();

    @StudioElement(
            name = "DoubleMax",
            classFqn = "io.jmix.flowui.component.validation.DoubleMaxValidator",
            xmlElement = "doubleMax",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "value",
                            type = StudioPropertyType.STRING,
                            required = true
                    ),
                    @StudioProperty(
                            xmlAttribute = "inclusive",
                            type = StudioPropertyType.BOOLEAN
                    )
            }
    )
    void doubleMax();

    @StudioElement(
            name = "DoubleMin",
            classFqn = "io.jmix.flowui.component.validation.DoubleMinValidator",
            xmlElement = "doubleMin",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "value",
                            type = StudioPropertyType.STRING,
                            required = true
                    ),
                    @StudioProperty(
                            xmlAttribute = "inclusive",
                            type = StudioPropertyType.BOOLEAN
                    )
            }
    )
    void doubleMin();

    @StudioElement(
            name = "Email",
            classFqn = "io.jmix.flowui.component.validation.EmailValidator",
            xmlElement = "email",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void email();

    @StudioElement(
            name = "Future",
            classFqn = "io.jmix.flowui.component.validation.FutureValidator",
            xmlElement = "future",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.kit.component.valuepicker.ValuePickerBase",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "checkSeconds",
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"
                    )
            }
    )
    void future();

    @StudioElement(
            name = "FutureOrPresent",
            classFqn = "io.jmix.flowui.component.validation.FutureOrPresentValidator",
            xmlElement = "futureOrPresent",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.kit.component.valuepicker.ValuePickerBase",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "checkSeconds",
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"
                    )
            }
    )
    void futureOrPresent();

    @StudioElement(
            name = "Max",
            classFqn = "io.jmix.flowui.component.validation.MaxValidator",
            xmlElement = "max",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "value",
                            type = StudioPropertyType.STRING,
                            required = true
                    )
            }
    )
    void max();

    @StudioElement(
            name = "Min",
            classFqn = "io.jmix.flowui.component.validation.MinValidator",
            xmlElement = "min",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "value",
                            type = StudioPropertyType.STRING,
                            required = true
                    )
            }
    )
    void min();

    @StudioElement(
            name = "NegativeOrZero",
            classFqn = "io.jmix.flowui.component.validation.NegativeOrZeroValidator",
            xmlElement = "negativeOrZero",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void negativeOrZero();

    @StudioElement(
            name = "Negative",
            classFqn = "io.jmix.flowui.component.validation.NegativeValidator",
            xmlElement = "negative",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void negative();

    @StudioElement(
            name = "NotBlank",
            classFqn = "io.jmix.flowui.component.validation.NotBlankValidator",
            xmlElement = "notBlank",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void notBlank();

    @StudioElement(
            name = "NotEmpty",
            classFqn = "io.jmix.flowui.component.validation.NotEmptyValidator",
            xmlElement = "notEmpty",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void notEmpty();

    @StudioElement(
            name = "NotNull",
            classFqn = "io.jmix.flowui.component.validation.NotNullValidator",
            xmlElement = "notNull",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void notNull();

    @StudioElement(
            name = "PastOrPresent",
            classFqn = "io.jmix.flowui.component.validation.PastOrPresentValidator",
            xmlElement = "pastOrPresent",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.kit.component.valuepicker.ValuePickerBase",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "checkSeconds",
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"
                    )
            }
    )
    void pastOrPresent();

    @StudioElement(
            name = "Past",
            classFqn = "io.jmix.flowui.component.validation.PastValidator",
            xmlElement = "past",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.kit.component.valuepicker.ValuePickerBase",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "checkSeconds",
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"
                    )
            }
    )
    void past();

    @StudioElement(
            name = "PositiveOrZero",
            classFqn = "io.jmix.flowui.component.validation.PositiveOrZeroValidator",
            xmlElement = "positiveOrZero",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void positiveOrZero();

    @StudioElement(
            name = "Positive",
            classFqn = "io.jmix.flowui.component.validation.PositiveValidator",
            xmlElement = "positive",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.codeeditor.CodeEditor"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    )
            }
    )
    void positive();

    @StudioElement(
            name = "Regexp",
            classFqn = "io.jmix.flowui.component.validation.RegexpValidator",
            xmlElement = "regexp",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
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
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "regexp",
                            type = StudioPropertyType.STRING,
                            required = true
                    )
            }
    )
    void regexp();

    @StudioElement(
            name = "Size",
            classFqn = "io.jmix.flowui.component.validation.SizeValidator",
            xmlElement = "size",
            icon = "io/jmix/flowui/kit/meta/icon/element/validator.svg",
            unsupportedTarget = {
                    "io.jmix.flowui.component.datetimepicker.TypedDateTimePicker",
                    "io.jmix.flowui.component.datepicker.TypedDatePicker",
                    "io.jmix.flowui.component.timepicker.TypedTimePicker",
                    "io.jmix.flowui.component.textfield.JmixNumberField",
                    "io.jmix.flowui.component.textfield.JmixIntegerField",
                    "io.jmix.flowui.component.combobox.EntityComboBox",
                    "io.jmix.flowui.component.valuepicker.EntityPicker",
                    "io.jmix.flowui.component.textfield.JmixBigDecimalField"
            },
            properties = {
                    @StudioProperty(
                            xmlAttribute = "message",
                            type = StudioPropertyType.LOCALIZED_STRING
                    ),
                    @StudioProperty(
                            xmlAttribute = "min",
                            type = StudioPropertyType.INTEGER
                    ),
                    @StudioProperty(
                            xmlAttribute = "max",
                            type = StudioPropertyType.INTEGER
                    )
            }
    )
    void size();
}
