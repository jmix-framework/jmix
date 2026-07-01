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
import io.jmix.flowui.theme.StyleUtility;

public enum GridColumnVisibilityVariant implements ThemeVariant {

    LUMO_TERTIARY_INLINE("tertiary-inline"),

    SMALL("small"),
    LARGE("large"),
    TERTIARY("tertiary"),
    PRIMARY("primary"),
    ERROR("error"),
    SUCCESS("success"),
    CONTRAST("contrast"),
    ICON("icon"),

    /**
     * @deprecated Use {@link #LUMO_TERTIARY_INLINE} instead or {@link StyleUtility.Button#LINK_BUTTON}.
     */
    @Deprecated(since = "3.0", forRemoval = true)
    TERTIARY_INLINE("tertiary-inline");

    private final String variant;

    GridColumnVisibilityVariant(String variant) {
        this.variant = variant;
    }

    @Override
    public String getVariantName() {
        return variant;
    }
}
