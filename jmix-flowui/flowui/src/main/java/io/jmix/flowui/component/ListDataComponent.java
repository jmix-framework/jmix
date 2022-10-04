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

import io.jmix.flowui.data.DataUnit;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A component that displays list of items.
 *
 * @param <T> item type
 */
public interface ListDataComponent<T> {

    /**
     * Returns an item corresponding to the selected row of this component.
     * If nothing is selected, the method returns {@code null}. If multiple
     * selection mode is enabled, returns the first selected instance.
     *
     * @return an item corresponding to the selected row of this component
     */
    @Nullable
    T getSingleSelectedItem();

    /**
     * Returns a set of items corresponding to the selected rows of this
     * component. If nothing is selected, the method returns a {@link Collections#emptySet()}.
     *
     * @return a set of item instances corresponding to the selected rows of
     * this component, never {@code null}
     */
    Set<T> getSelectedItems();

    /**
     * Selects a row of this component for a given item.
     *
     * @param item item instance to select the row
     */
    void select(T item);

    /**
     * Selects the rows of this component for a given collection of items.
     *
     * @param items collection of items to select rows
     */
    void select(Collection<T> items);

    void deselect(T item);

    /**
     * Deselects all selected rows.
     */
    void deselectAll();

    /**
     * @return a data unit that holds component items
     */
    @Nullable
    DataUnit getItems();

    /**
     * @return {@code true} if multiple selection mode is enabled
     */
    boolean isMultiSelect();
}
