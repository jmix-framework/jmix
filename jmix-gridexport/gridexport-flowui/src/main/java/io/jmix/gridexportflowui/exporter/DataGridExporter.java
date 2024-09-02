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

package io.jmix.gridexportflowui.exporter;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.download.Downloader;
import org.springframework.lang.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

public interface DataGridExporter {

    void setFileName(String fileName);

    /**
     * Exports {@link DataGrid} content using {@link Downloader}.
     *
     * @param downloader   {@link Downloader} instance
     * @param dataGrid     {@link DataGrid} to get content
     * @param exportMode   exportMode
     * @param columnFilter filter of the {@link Grid.Column}
     */
    void exportDataGrid(Downloader downloader, Grid<Object> dataGrid, ExportMode exportMode,
                        Predicate<Grid.Column<Object>> columnFilter);

    /**
     * Exports {@link DataGrid} content using {@link Downloader}.
     *
     * @param downloader {@link Downloader} instance
     * @param dataGrid   {@link DataGrid} to get content
     * @param exportMode exportMode
     */
    default void exportDataGrid(Downloader downloader, Grid<Object> dataGrid, ExportMode exportMode) {
        exportDataGrid(downloader, dataGrid, exportMode, ColumnExportFilter.VISIBLE_COLUMNS.getFilterPredicate());
    }

    /**
     * @return exporter label
     */
    String getLabel();

    /**
     * Adds a function to get value from the column.
     *
     * @param columnId            column id
     * @param columnValueProvider column value provider function
     */
    void addColumnValueProvider(String columnId, Function<ColumnValueContext, Object> columnValueProvider);

    /**
     * Removes an column value provider function by column id.
     *
     * @param columnId column id
     */
    void removeColumnValueProvider(String columnId);

    /**
     * @param columnId column id
     * @return column value provider function for the column id
     */
    @Nullable
    Function<ColumnValueContext, Object> getColumnValueProvider(String columnId);

    class ColumnValueContext {
        protected Object column;
        protected Object entity;

        protected ListDataComponent<?> target;

        public ColumnValueContext(ListDataComponent<?> target, Object column, Object entity) {
            this.target = target;
            this.column = column;
            this.entity = entity;
        }

        @SuppressWarnings("unchecked")
        public <C> C getColumn() {
            return (C) column;
        }

        @SuppressWarnings("unchecked")
        public <T> T getTarget() {
            return (T) target;
        }

        @SuppressWarnings("unchecked")
        public <E> E getEntity() {
            return (E) entity;
        }
    }
}
