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

import io.jmix.masquerade.component.CheckboxGroup;

import java.util.List;

/**
 * Condition for checking the checked items of {@link CheckboxGroup} web-element wrapper.
 */
public class CheckedItems extends SpecificCondition {

    protected List<String> visibleItems;

    public CheckedItems(String... visibleItems) {
        this(List.of(visibleItems));
    }

    public CheckedItems(List<String> visibleItems) {
        super("checkedItems");

        this.visibleItems = visibleItems;
    }

    /**
     * @return list of the checked items
     */
    public List<String> getValue() {
        return visibleItems;
    }

    @Override
    public String toString() {
        return "%s:[%s]".formatted(getName(), getValue());
    }
}
