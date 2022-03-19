/*
 * Copyright 2020 Haulmont.
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

package io.jmix.uiexport.exporter;

import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.Table;
import io.jmix.ui.download.Downloader;
import io.jmix.uiexport.action.ExportAction;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Table exporter interface.
 * <br>Just create an instance of {@link ExportAction} with <code>withExporter</code> method.
 */
public interface TableExporter {

    /**
     * download <code>table</code> content via <code>downloader</code>
     */
    void exportTable(Downloader downloader, Table<Object> table, ExportMode exportMode);

    /**
     * download <code>dataGrid</code> content via <code>downloader</code>
     */
    void exportDataGrid(Downloader downloader, DataGrid<Object> dataGrid, ExportMode exportMode);

    /**
     * returns exporter caption
     */
    String getCaption();

    /**
     * Adds a function to get value from the column.
     *
     * @param columnId       column id
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

    /**
     * Describes a context for a column value provider.
     */
    class ColumnValueContext {
        protected Object column;
        protected Object entity;

        protected ListComponent target;

        public ColumnValueContext(ListComponent target, Object column, Object entity) {
            this.target = target;
            this.column = column;
            this.entity = entity;
        }

        public <C> C getColumn() {
            return (C) column;
        }

        public <T> T getTarget() {
            return (T) target;
        }

        public <E> E getEntity() {
            return (E) entity;
        }
    }
}
