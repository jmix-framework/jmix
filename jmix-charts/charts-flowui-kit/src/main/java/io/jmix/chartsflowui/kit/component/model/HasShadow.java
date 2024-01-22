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
 * A component that has shadow.
 *
 * @param <T> origin class type
 */
public interface HasShadow<T> {

    /**
     * @return depth of shadow blur
     */
    Integer getShadowBlur();

    /**
     * Sets depth of shadow blur or replaces existing one.
     *
     * @param shadowBlur depth of shadow blur
     */
    void setShadowBlur(Integer shadowBlur);

    /**
     * @param shadowBlur depth of shadow blur
     * @return this
     * @see HasShadow#setShadowBlur(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withShadowBlur(Integer shadowBlur) {
        setShadowBlur(shadowBlur);
        return (T) this;
    }

    /**
     * @return shadow color
     */
    Color getShadowColor();

    /**
     * Sets a color for shadow or replaces existing one.
     *
     * @param shadowColor color to set
     */
    void setShadowColor(Color shadowColor);

    /**
     * @param shadowColor color to set
     * @return thus
     * @see HasShadow#setShadowColor(Color)
     */
    @SuppressWarnings("unchecked")
    default T withShadowColor(Color shadowColor) {
        setShadowColor(shadowColor);
        return (T) this;
    }

    /**
     * @return horizontal offset for shadow in pixels
     */
    Integer getShadowOffsetX();

    /**
     * Sets horizontal offset for shadow or replaces existing one.
     *
     * @param shadowOffsetX offset to set in pixels
     */
    void setShadowOffsetX(Integer shadowOffsetX);

    /**
     * @param shadowOffsetX offset to set in pixels
     * @return this
     * @see HasShadow#setShadowOffsetX(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withShadowOffsetX(Integer shadowOffsetX) {
        setShadowOffsetX(shadowOffsetX);
        return (T) this;
    }

    /**
     * @return vertical offset for shadow in pixels
     */
    Integer getShadowOffsetY();

    /**
     * Sets vertical offset for shadow or replaces existing one.
     *
     * @param shadowOffsetY offset to set in pixels
     */
    void setShadowOffsetY(Integer shadowOffsetY);

    /**
     * @param shadowOffsetY offset to set in pixels
     * @return this
     * @see HasShadow#withShadowOffsetY(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withShadowOffsetY(Integer shadowOffsetY) {
        setShadowOffsetY(shadowOffsetY);
        return (T) this;
    }
}
