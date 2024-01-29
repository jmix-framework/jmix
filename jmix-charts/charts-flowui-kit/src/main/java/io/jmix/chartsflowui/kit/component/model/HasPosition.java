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

/**
 * A component that has position.
 *
 * @param <T> origin class type
 */
public interface HasPosition<T> {

    /**
     * @return distance between component and the left side of the container
     */
    String getLeft();

    /**
     * Sets a distance between component and the left side of the container.<br/>
     * Possible values are:
     * <ul>
     *    <li>
     *        A pixel value like {@code "20"}.
     *    </li>
     *    <li>
     *        Percentage value relative to container width like {@code "20%"}.
     *    </li>
     *    <li>
     *        Predefined values like {@code "left"}, {@code "center"}, {@code "right"}.
     *    </li>
     * </ul>
     *
     * @param left distance to set
     */
    void setLeft(String left);

    /**
     * @param left distance to set
     * @return this
     * @see HasPosition#setLeft(String)
     */
    @SuppressWarnings("unchecked")
    default T withLeft(String left) {
        setLeft(left);
        return (T) this;
    }

    /**
     * @return distance between component and the top side of the container
     */
    String getTop();

    /**
     * Sets a distance between component and the top side of the container.<br/>
     * Possible values are:
     * <ul>
     *    <li>
     *        A pixel value like {@code "20"}.
     *    </li>
     *    <li>
     *        Percentage value relative to container width like {@code "20%"}.
     *    </li>
     *    <li>
     *        Predefined values like {@code "top"}, {@code "middle"}, {@code "bottom"}.
     *    </li>
     * </ul>
     *
     * @param top distance to set
     */
    void setTop(String top);

    /**
     * @param top distance to set
     * @return this
     * @see HasPosition#setTop(String)
     */
    @SuppressWarnings("unchecked")
    default T withTop(String top) {
        setTop(top);
        return (T) this;
    }

    /**
     * @return distance between component and the right side of the container
     */
    String getRight();

    /**
     * Sets a distance between component and the right side of the container.<br/>
     * Possible values are:
     * <ul>
     *    <li>
     *        A pixel value like {@code "20"}.
     *    </li>
     *    <li>
     *        Percentage value relative to container width like {@code "20%"}.
     *    </li>
     * </ul>
     *
     * @param right distance to set
     */
    void setRight(String right);

    /**
     * @param right distance to set
     * @return this
     * @see HasPosition#setRight(String)
     */
    @SuppressWarnings("unchecked")
    default T withRight(String right) {
        setRight(right);
        return (T) this;
    }

    /**
     * @return distance between component and the bottom side of the container
     */
    String getBottom();

    /**
     * Sets a distance between component and the bottom side of the container.<br/>
     * Possible values are:
     * <ul>
     *    <li>
     *        A pixel value like {@code "20"}.
     *    </li>
     *    <li>
     *        Percentage value relative to container width like {@code "20%"}.
     *    </li>
     * </ul>
     *
     * @param bottom distance to set
     */
    void setBottom(String bottom);

    /**
     * @param bottom distance to set
     * @return this
     * @see HasPosition#setBottom(String)
     */
    @SuppressWarnings("unchecked")
    default T withBottom(String bottom) {
        setBottom(bottom);
        return (T) this;
    }
}
