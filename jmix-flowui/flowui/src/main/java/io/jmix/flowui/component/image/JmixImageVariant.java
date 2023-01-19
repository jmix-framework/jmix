/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.image;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@link JmixImage} component.
 */
public enum JmixImageVariant implements ThemeVariant {

    /**
     * Theme variant for image scale mode. The image is stretched according to the size of the component.
     * <p>
     * Corresponds to the {@code object-fit:fill} CSS property.
     */
    FILL("fill"),

    /**
     * Theme variant for image scale mode. The image is compressed or stretched to fill the entire component while
     * preserving the proportions. If the image proportions do not match the component's proportions then the image
     * will be clipped to fit.
     * <p>
     * Corresponds to the {@code object-fit:cover} CSS property.
     */
    COVER("cover"),

    /**
     * Theme variant for image scale mode. The image is compressed or stretched to fit the component dimensions
     * while preserving the proportions.
     * <p>
     * Corresponds to the {@code object-fit:contain} CSS property.
     */
    CONTAIN("contain"),

    /**
     * Theme variant for image scale mode. The image is sized as if {@link #CONTAIN} were specified or no theme
     * variant was specified, whichever would result in a smaller concrete object size.
     * <p>
     * Corresponds to the {@code object-fit:scale-down} CSS property.
     */
    SCALE_DOWN("scale-down");

    private final String name;

    JmixImageVariant(String name) {
        this.name = name;
    }

    @Override
    public String getVariantName() {
        return name;
    }
}
