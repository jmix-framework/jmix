/*
 * Copyright 2025 Haulmont.
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

package io.jmix.masquerade.config;

import com.google.common.collect.ImmutableMap;
import io.jmix.masquerade.component.*;
import org.openqa.selenium.By;

import java.util.Map;
import java.util.function.Function;

/**
 * Default implementation of the {@link ComponentConfig}. Contains all implementation of the default Masquerade
 * component wrappers.
 */
public class DefaultComponentConfig implements ComponentConfig {

    @Override
    public Map<Class<?>, Function<By, ?>> getComponents() {
        return new ImmutableMap.Builder<Class<?>, Function<By, ?>>()
                .put(Button.class, Button::new)
                .put(Checkbox.class, Checkbox::new)
                .put(CheckboxGroup.class, CheckboxGroup::new)
                .put(CodeEditor.class, CodeEditor::new)
                .put(ComboBox.class, ComboBox::new)
                .put(ComboButton.class, ComboButton::new)
                .put(DataGrid.class, DataGrid::new)
                .put(DatePicker.class, DatePicker::new)
                .put(DateTimePicker.class, DateTimePicker::new)
                .put(DropdownButton.class, DropdownButton::new)
                .put(EntityPicker.class, EntityPicker::new)
                .put(EntityComboBox.class, EntityComboBox::new)
                .put(HorizontalMenu.class, HorizontalMenu::new)
                .put(ListMenu.class, ListMenu::new)
                .put(MultiSelectComboBox.class, MultiSelectComboBox::new)
                .put(MultiSelectComboBoxPicker.class, MultiSelectComboBoxPicker::new)
                .put(MultiValuePicker.class, MultiValuePicker::new)
                .put(PasswordField.class, PasswordField::new)
                .put(RadioButtonGroup.class, RadioButtonGroup::new)
                .put(Select.class, Select::new)
                .put(TextArea.class, TextArea::new)
                .put(TextField.class, TextField::new)
                .put(TimePicker.class, TimePicker::new)
                .put(Unknown.class, Unknown::new)
                .put(ValuePicker.class, ValuePicker::new)
                .put(Accordion.class, Accordion::new)
                .put(Details.class, Details::new)
                .put(TabSheet.class, TabSheet::new)
                .build();
    }
}
