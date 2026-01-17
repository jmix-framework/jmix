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

import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * An interface used in describing the responsive layouting behavior of an implementing layout.
 */
public interface SupportsResponsiveSteps {

    /**
     * Get the list of {@link ResponsiveStep}s used to configure this layout.
     *
     * @return the list of {@link ResponsiveStep}s used to configure this layout
     * @see ResponsiveStep
     */
    List<ResponsiveStep> getResponsiveSteps();

    /**
     * Configure the responsive steps used in this layout.
     *
     * @param steps list of {@link ResponsiveStep}s to set
     * @see ResponsiveStep
     */
    void setResponsiveSteps(List<ResponsiveStep> steps);

    /**
     * Configure the responsive steps used in this layout.
     *
     * @param steps the {@link ResponsiveStep}s to set
     * @see ResponsiveStep
     */
    default void setResponsiveSteps(ResponsiveStep... steps) {
        setResponsiveSteps(Arrays.asList(steps));
    }

    /**
     * A class used in describing the responsive layouting behavior of a component.
     */
    class ResponsiveStep {

        private final String minWidth;
        private final int columns;
        private final LabelsPosition labelsPosition;

        /**
         * Constructs a ResponsiveStep with the given minimum width and number
         * of columns.
         *
         * @param minWidth the minimum width as a CSS string value after which this
         *                 responsive step is to be applied
         * @param columns  the number of columns the layout should have
         */
        public ResponsiveStep(String minWidth, int columns) {
            this(minWidth, columns, null);
        }

        /**
         * Constructs a ResponsiveStep with the given minimum width, number of
         * columns and label position.
         *
         * @param minWidth       the minimum width as a CSS string value after which this
         *                       responsive step is to be applied
         * @param columns        the number of columns the layout should have
         * @param labelsPosition the position where label components are to be displayed
         * @see LabelsPosition
         */
        public ResponsiveStep(String minWidth, int columns,
                              @Nullable LabelsPosition labelsPosition) {
            this.minWidth = minWidth;
            this.columns = columns;
            this.labelsPosition = labelsPosition;
        }

        /**
         * Returns the minimum width as a CSS string value after which the current
         * responsive step is to be applied.
         *
         * @return the minimum width
         */
        public String getMinWidth() {
            return minWidth;
        }

        /**
         * Returns the number of columns the layout should have
         *
         * @return the number of columns the layout should have
         */
        public int getColumns() {
            return columns;
        }

        /**
         * Returns the position where label components are displayed in the layout.
         *
         * @return the {@link LabelsPosition} value indicating the position of the labels,
         * or {@code null} if the label position is not specified
         */
        @Nullable
        public LabelsPosition getLabelsPosition() {
            return labelsPosition;
        }

        /**
         * Specifies the position of labels relative to their associated components
         * in a responsive layout.
         */
        public enum LabelsPosition {

            /**
             * Labels are displayed on the left hand side of the wrapped
             * component.
             */
            ASIDE,

            /**
             * Labels are displayed atop the wrapped component.
             */
            TOP
        }
    }
}
