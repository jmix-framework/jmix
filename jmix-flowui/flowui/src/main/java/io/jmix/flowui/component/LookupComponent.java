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

import java.util.Set;

/**
 * A component which can be set as lookup component for a view.
 *
 * @param <T> the type of items contained within the lookup component
 */
public interface LookupComponent<T> {

    /**
     * @return items selected in this lookup component
     */
    Set<T> getSelectedItems();

    /**
     * Interface representing a multi-select lookup component.
     *
     * @param <T> the type of items contained within the lookup component
     */
    interface MultiSelectLookupComponent<T> extends LookupComponent<T> {

        /**
         * Enables multi-selection functionality for the component.
         * <p>
         * This method sets the component to allow the selection of multiple items
         * simultaneously by internally invoking {@code setMultiSelect(true)}.
         */
        default void enableMultiSelect() {
            setMultiSelect(true);
        }

        /**
         * Sets whether the component allows multi-selection or not.
         *
         * @param multiSelect if {@code true}, multi-selection is enabled; otherwise, multi-selection is disabled
         */
        void setMultiSelect(boolean multiSelect);
    }
}
