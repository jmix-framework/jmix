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

import io.jmix.ui.component.data.DataUnit;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A component that can display tabular data.
 *
 * @param <E> entity type
 */
public interface ListComponent<E> extends Component, Component.BelongToFrame, ActionsHolder {

    /**
     * @return true if multiple selection mode is enabled
     */
    boolean isMultiSelect();

    /**
     * Returns an instance of entity corresponding to the selected row of the list component. If nothing is selected,
     * the method returns {@code null}. If multiple selection mode is enabled, returns the first selected instance.
     *
     * @return an instance of entity corresponding to the selected row of the list component
     */
    @Nullable
    E getSingleSelected();

    /**
     * Returns a set of entity instances corresponding to the selected rows of the list component. If nothing is
     * selected, the method returns a {@link Collections#emptySet()}.
     *
     * @return a set of entity instances corresponding to the selected rows of the list component
     */
    Set<E> getSelected();

    /**
     * Selects a row of the list component for a given entity instance.
     *
     * @param item entity instance to select the row, {@code null} to reset the selection
     */
    void setSelected(@Nullable E item);

    /**
     * Selects the rows of the list component for a given collection of entity instances.
     *
     * @param items collection of entity instances to select rows
     */
    void setSelected(Collection<E> items);

    /**
     * @return a list component items
     */
    @Nullable
    DataUnit getItems();
}
