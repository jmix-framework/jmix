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

import io.jmix.masquerade.component.AbstractOverlay;

import java.util.List;

/**
 * Condition for checking visible items for overlay web-element wrappers (inheritors of {@link AbstractOverlay}).
 */
public class VisibleItems extends SpecificCondition {

    protected List<String> visibleItems;

    public VisibleItems(String... visibleItems) {
        this(List.of(visibleItems));
    }

    public VisibleItems(List<String> visibleItems) {
        super("visibleItems");

        this.visibleItems = visibleItems;
    }

    /**
     * @return visible items
     */
    public List<String> getValue() {
        return visibleItems;
    }

    @Override
    public String toString() {
        return "%s:[%s]".formatted(getName(), getValue());
    }
}
