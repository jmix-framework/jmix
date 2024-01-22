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

package io.jmix.chartsflowui.kit.component.model;

import io.jmix.chartsflowui.kit.component.model.shared.Color;

/**
 * A component that has border.
 *
 * @param <T> origin class type
 */
public interface HasBorder<T> {

    /**
     * @return border color
     */
    Color getBorderColor();

    /**
     * Sets a border color or replaces existing one.
     *
     * @param borderColor color to set
     */
    void setBorderColor(Color borderColor);

    /**
     * @param borderColor color to set
     * @return this
     * @see HasBorder#setBorderColor(Color)
     */
    @SuppressWarnings("unchecked")
    default T withBorderColor(Color borderColor) {
        setBorderColor(borderColor);
        return (T) this;
    }

    /**
     * @return border width in pixels
     */
    Integer getBorderWidth();

    /**
     * Sets a border width or replaces existing one.
     *
     * @param borderWidth border width in pixels
     */
    void setBorderWidth(Integer borderWidth);

    /**
     * @param borderWidth border width in pixels
     * @return this
     * @see HasBorder#setBorderWidth(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withBorderWidth(Integer borderWidth) {
        setBorderWidth(borderWidth);
        return (T) this;
    }

    /**
     * @return border radius of the text fragment
     */
    Integer getBorderRadius();

    /**
     * Sets a border radius of the text fragment or replaces existing one.
     *
     * @param borderRadius border radius to set in pixels
     */
    void setBorderRadius(Integer borderRadius);

    /**
     * @param borderRadius border radius to set in pixels
     * @return this
     * @see HasBorder#setBorderRadius(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withBorderRadius(Integer borderRadius) {
        setBorderRadius(borderRadius);
        return (T) this;
    }
}
