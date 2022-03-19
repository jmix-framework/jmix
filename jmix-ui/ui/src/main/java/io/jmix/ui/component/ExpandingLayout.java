/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.component;

/**
 * Component container which can expand enclosing components.
 */
public interface ExpandingLayout extends ComponentContainer {

    /**
     * Specifies the component that will be given maximum available space.
     *
     * @param component the component
     */
    void expand(Component component);

    /**
     * Resets the expanded component and provides equal space for all nested components.
     */
    void resetExpanded();

    /**
     * Returns true if the component occupies the maximum available space.
     *
     * @param component component to check
     * @return true if the component occupies the maximum available space
     */
    boolean isExpanded(Component component);

    /**
     * @return expand direction
     * @see ExpandDirection
     */
    ExpandDirection getExpandDirection();

    /**
     * Expand direction
     */
    enum ExpandDirection {
        VERTICAL,
        HORIZONTAL
    }
}
