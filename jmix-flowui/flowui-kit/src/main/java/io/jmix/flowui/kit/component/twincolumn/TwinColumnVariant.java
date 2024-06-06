/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.kit.component.twincolumn;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@link JmixTwinColumn} component.
 */
public enum TwinColumnVariant implements ThemeVariant {

    /**
     * Theme variant with hidden border of component columns.
     */
    NO_BORDER("no-border"),

    /**
     * Theme variant with hidden row borders between column items.
     */
    NO_ROW_BORDER("no-row-border"),

    /**
     * Theme variant with checkboxes to select items in columns.
     */
    CHECKBOXES("checkboxes"),

    /**
     * Theme variant with no space between buttons and rounded top and bottom borders.
     */
    NO_SPACE_BETWEEN_ACTIONS("no-space-between-actions");

    private final String name;

    TwinColumnVariant(String name) {
        this.name = name;
    }

    @Override
    public String getVariantName() {
        return name;
    }
}
