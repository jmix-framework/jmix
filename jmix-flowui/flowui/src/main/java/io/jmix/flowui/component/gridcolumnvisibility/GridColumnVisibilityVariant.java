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

package io.jmix.flowui.component.gridcolumnvisibility;

import com.vaadin.flow.component.shared.ThemeVariant;

public enum GridColumnVisibilityVariant implements ThemeVariant {

    LUMO_SMALL("small"),
    LUMO_LARGE("large"),
    LUMO_TERTIARY("tertiary"),
    LUMO_TERTIARY_INLINE("tertiary-inline"),
    LUMO_PRIMARY("primary"),
    LUMO_ERROR("error"),
    LUMO_SUCCESS("success"),
    LUMO_CONTRAST("contrast"),
    LUMO_ICON("icon"),
    MATERIAL_CONTAINED("contained"),
    MATERIAL_OUTLINED("outlined");

    private final String variant;

    GridColumnVisibilityVariant(String variant) {
        this.variant = variant;
    }

    @Override
    public String getVariantName() {
        return variant;
    }
}
