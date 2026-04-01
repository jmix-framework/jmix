/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.component.grid;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.annotation.Experimental;
import io.jmix.core.common.util.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the context for sorting operations in a data grid.
 *
 * @param <E> the type of the items displayed in the data grid
 */
@Experimental
public class DataGridSortContext<E> {

    protected final Grid<E> grid;
    protected final List<ColumnSortInfo<E>> columnSortInfos;

    public DataGridSortContext(Grid<E> grid, List<ColumnSortInfo<E>> columnSortInfos) {
        Preconditions.checkNotNullArgument(grid);
        Preconditions.checkNotNullArgument(columnSortInfos);

        this.grid = grid;
        this.columnSortInfos = new ArrayList<>(columnSortInfos);
    }

    /**
     * @return the grid
     */
    public Grid<E> getGrid() {
        return grid;
    }

    /**
     * @return an unmodifiable list of {@link ColumnSortInfo<E>}
     */
    public List<ColumnSortInfo<E>> getColumnSortInfos() {
        return Collections.unmodifiableList(columnSortInfos);
    }
}
