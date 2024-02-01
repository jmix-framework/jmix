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

package io.jmix.chartsflowui.kit.component.model.shared;

/**
 * A component that has a space around content.
 *
 * @param <T> origin class type
 */
public interface HasPadding<T> {

    /**
     * @return space around content
     */
    Padding getPadding();

    /**
     * Sets padding on each of the four sides or replaces an existing one. The unit is px.
     *
     * @param padding padding to set
     */
    void setPadding(Integer padding);

    /**
     * Sets the top/bottom and left/right paddings or replaces an existing one. The unit is px.
     *
     * @param vertical   top and bottom padding to set
     * @param horizontal left and right padding to set
     */
    void setPadding(Integer vertical, Integer horizontal);

    /**
     * Sets each of the four paddings separately. The unit is px.
     *
     * @param top    top padding to set
     * @param right  right padding to set
     * @param bottom bottom padding to set
     * @param left   left padding to set
     */
    void setPadding(Integer top, Integer right, Integer bottom, Integer left);

    /**
     * @param padding padding to set
     * @return this
     * @see HasPadding#setPadding(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withPadding(Integer padding) {
        setPadding(padding);
        return (T) this;
    }

    /**
     * @param vertical   top and bottom padding to set
     * @param horizontal left and right padding to set
     * @return this
     * @see HasPadding#setPadding(Integer, Integer)
     */
    @SuppressWarnings("unchecked")
    default T withPadding(Integer vertical, Integer horizontal) {
        setPadding(vertical, horizontal);
        return (T) this;
    }

    /**
     * @param top    top padding to set
     * @param right  right padding to set
     * @param bottom bottom padding to set
     * @param left   left padding to set
     * @return this
     * @see HasPadding#setPadding(Integer, Integer, Integer, Integer)
     */
    @SuppressWarnings("unchecked")
    default T withPadding(Integer top, Integer right, Integer bottom, Integer left) {
        setPadding(top, right, bottom, left);
        return (T) this;
    }
}
