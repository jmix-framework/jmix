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

package io.jmix.flowui.component.grid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.kit.component.grid.JmixTreeGrid;
import io.jmix.flowui.kit.meta.StudioIgnore;
import io.jmix.flowui.sys.BeanUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.Objects;

public class DataGridColumn<E> extends Grid.Column<E> implements ApplicationContextAware {

    protected DataGridHeaderFilter dataGridFilter;
    protected ApplicationContext applicationContext;

    /**
     * Constructs a new DataGridColumn for use inside a {@link DataGrid}.
     *
     * @param grid     the grid this column is attached to
     * @param columnId unique identifier of this column
     * @param renderer the renderer to use in this column, must not be
     *                 {@code null}
     */
    public DataGridColumn(Grid<E> grid, String columnId, Renderer<E> renderer) {
        super(grid, columnId, renderer);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @StudioIgnore
    public Grid.Column<E> setClassNameGenerator(SerializableFunction<E, String> classNameGenerator) {
        return super.setClassNameGenerator(classNameGenerator);
    }

    /**
     * Sets the filtering for a column. If the filtering is enabled,
     * a filter button will be added to the column header.
     * The filtering is disabled by default.
     *
     * @param filterable whether to add a filter to the header
     */
    public void setFilterable(boolean filterable) {
        if (filterable && dataGridFilter == null) {
            dataGridFilter = new DataGridHeaderFilter(new DataGridHeaderFilter.HeaderFilterContext(grid, this));
            super.setHeader(dataGridFilter);

            BeanUtil.autowireContext(applicationContext, dataGridFilter);
        } else if (!filterable && dataGridFilter != null) {
            Component currentHeader = dataGridFilter.getHeader();
            dataGridFilter = null;

            if (currentHeader != null) {
                currentHeader.removeFromParent();
            }
            super.setHeader(currentHeader);
        }
    }

    /**
     * @return {@code true} if the filter is added to the column header, {@code false} otherwise
     */
    public boolean isFilterable() {
        return dataGridFilter != null;
    }

    @Override
    public Grid.Column<E> setHeader(String labelText) {
        if (dataGridFilter != null) {
            dataGridFilter.setHeader(labelText);
            return this;
        }

        return super.setHeader(labelText);
    }

    @Override
    public Grid.Column<E> setHeader(Component headerComponent) {
        if (dataGridFilter != null && !(headerComponent instanceof DataGridHeaderFilter)) {
            dataGridFilter.setHeader(headerComponent);
            return this;
        }

        return super.setHeader(headerComponent);
    }

    /**
     * Returns the header text of this column in the grid's default header row, i.e. the text set by
     * {@link #setHeader(String)}. Unlike {@link #getHeaderText()}, the result does not change when a header row
     * is appended below the default one, e.g. the aggregation row with
     * {@link EnhancedDataGrid.AggregationPosition#TOP}.
     *
     * @return the header text or {@code null} if the header has no text
     */
    @Nullable
    public String getDefaultHeaderText() {
        HeaderRow defaultHeaderRow = getGridDefaultHeaderRow();
        return defaultHeaderRow != null
                ? defaultHeaderRow.getCell(this).getText()
                : getHeaderText();
    }

    /**
     * Returns the header component of this column in the grid's default header row, e.g. the
     * {@link DataGridHeaderFilter} of a filterable column. Unlike {@link #getHeaderComponent()}, the result does not
     * change when a header row is appended below the default one, e.g. the aggregation row with
     * {@link EnhancedDataGrid.AggregationPosition#TOP}.
     *
     * @return the header component or {@code null} if the header has no component
     */
    @Nullable
    public Component getDefaultHeaderComponent() {
        HeaderRow defaultHeaderRow = getGridDefaultHeaderRow();
        return defaultHeaderRow != null
                ? defaultHeaderRow.getCell(this).getComponent()
                : getHeaderComponent();
    }

    @Nullable
    protected HeaderRow getGridDefaultHeaderRow() {
        if (grid instanceof JmixGrid<?> jmixGrid) {
            return jmixGrid.getDefaultHeaderRow();
        } else if (grid instanceof JmixTreeGrid<?> jmixTreeGrid) {
            return jmixTreeGrid.getDefaultHeaderRow();
        }
        return null;
    }

    @Override
    public void setVisible(boolean visible) {
        boolean prevVisible = isVisible();
        super.setVisible(visible);
        if (prevVisible != visible) {
            fireEvent(new DataGridColumnVisibilityChangedEvent<>(this, false, visible));
        }
    }

    /**
     * Add listener for event of column visibility change
     * @param listener the listener to add
     * @return a registration handle to remove the listener
     */
    public Registration addColumnVisibilityChangedListener(
            ComponentEventListener<DataGridColumnVisibilityChangedEvent<E>> listener) {
        Preconditions.checkNotNullArgument(listener);

        //noinspection unchecked,rawtypes
        return addListener(DataGridColumnVisibilityChangedEvent.class, (ComponentEventListener) listener);
    }
}
