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

package io.jmix.flowui.component.menusearchfield;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for the {@link MenuSearchField} component.
 */
public enum MenuSearchFieldVariant implements ThemeVariant {

    SMALL("small"),
    ALIGN_CENTER("align-center"),
    ALIGN_RIGHT("align-right"),
    HELPER_ABOVE_FIELD("helper-above-field"),
    ALWAYS_FLOAT_LABEL("always-float-label");

    private final String variant;

    MenuSearchFieldVariant(String variant) {
        this.variant = variant;
    }

    @Override
    public String getVariantName() {
        return variant;
    }
}
