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

package io.jmix.masquerade.component;

import org.openqa.selenium.By;

/**
 * Web-element wrapper for entity combobox. Supports setting value.
 */
public class EntityComboBox extends AbstractComboBox<EntityComboBox> implements HasActions<EntityComboBox> {

    public EntityComboBox(By by) {
        super(by);
    }

    /**
     * Selects the value in the {@link ComboBoxOverlay}.
     * The value is usually equals to the entity instance name.
     *
     * @param value value to select
     * @return {@code this} to call fluent API
     */
    public EntityComboBox setValue(String value) {
        return selectSingleValue(value);
    }
}
