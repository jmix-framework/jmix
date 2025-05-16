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

package io.jmix.masquerade.condition;

import io.jmix.masquerade.component.MultiSelectComboBox;
import io.jmix.masquerade.component.MultiSelectComboBoxPicker;

import java.util.List;

/**
 * Condition for checking the contained selected items of multi-select components web-element wrappers
 * (e.g. {@link MultiSelectComboBox}, {@link MultiSelectComboBoxPicker}).
 */
public class SelectedItemsContains extends SpecificCondition {

    protected List<String> items;

    public SelectedItemsContains(String... items) {
        this(List.of(items));
    }

    public SelectedItemsContains(List<String> items) {
        super("selectedItemContains'");

        this.items = items;
    }

    /**
     * @return selected items
     */
    public List<String> getValue() {
        return items;
    }

    @Override
    public String toString() {
        return "%s:[%s]".formatted(getName(), getValue());
    }
}
