/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.component.groupgrid.adapter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.Renderer;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.groupgrid.GroupListDataComponent;
import org.springframework.lang.Nullable;

/**
 * Abstract adapter for columns from {@link GroupListDataComponent} that delegates all calls to the adaptee. Is used in
 * {@link AbstractGroupDataGridAdapter}.
 *
 * @param <E> item type
 */
public abstract class AbstractGroupDataGridColumnAdapter<E> extends DataGridColumn<E> {

    public AbstractGroupDataGridColumnAdapter(Grid<E> grid, String columnId, Renderer<E> renderer) {
        super(grid, columnId, renderer);
    }

    /**
     * @return the adaptee column
     */
    public abstract Component getAdaptee();

    /**
     * Returns the header text that was directly set on the adaptee column.
     * <p>
     * The header text can be deleted from the column if a grid contains additional headers. In this case, the header
     * text must be retrieved from the cell of the header row. However, the grouping column is not attached to the grid
     * and at the same time contains empty header text. In such cases, to get a correct header text, this method should
     * be used.
     *
     * @return the header text or {@code null} if not set
     */
    @Nullable
    @Internal
    public abstract String getStoredHeaderText();

    /**
     * Returns the header component that was directly set on the adaptee column.
     * <p>
     * The header component can be deleted from the column if a grid contains additional headers. In this case, the
     * header component must be retrieved from the cell of the header row. However, the grouping column is not attached
     * to the grid and at the same time does not contain a header component. In such cases, to get a header component,
     * this method should be used.
     *
     * @return the header component or {@code null} if not set
     */
    @Nullable
    @Internal
    public abstract Component getStoredHeaderComponent();
}
