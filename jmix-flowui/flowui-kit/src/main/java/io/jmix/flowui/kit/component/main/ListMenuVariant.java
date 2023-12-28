/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.kit.component.main;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for the {@link ListMenu} component.
 */
public enum ListMenuVariant implements ThemeVariant {

    /**
     * Expand/collapse toggle mark will be placed in the end of parent menu item component
     */
    TOGGLE_REVERSE("toggle-reverse");

    private final String variant;

    ListMenuVariant(String variant) {
        this.variant = variant;
    }

    @Override
    public String getVariantName() {
        return variant;
    }
}
