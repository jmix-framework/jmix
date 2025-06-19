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

package io.jmix.flowui.component;

/**
 * Interface for components that support managing the position of their associated label.
 * Components implementing this interface can specify where the label should be displayed
 * relative to the component.
 */
public interface SupportsLabelPosition {

    /**
     * Returns the current label position of the component.
     *
     * @return the label position indicating the position of the label
     */
    LabelPosition getLabelPosition();

    /**
     * Sets the position of the label for the component.
     *
     * @param labelPosition the label position to define the position of the label
     */
    void setLabelPosition(LabelPosition labelPosition);

    /**
     * Enum for describing the position of label components.
     */
    enum LabelPosition {

        /**
         * Label is displayed on the side of the wrapped component.
         */
        ASIDE,

        /**
         * Label is displayed atop the wrapped component.
         */
        TOP;
    }
}
