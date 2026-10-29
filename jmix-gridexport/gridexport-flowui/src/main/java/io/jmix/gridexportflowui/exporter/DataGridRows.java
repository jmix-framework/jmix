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

package io.jmix.gridexportflowui.exporter;

import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Rows of a data grid as the grid exporters see them: the exported columns in display order and the formatted
 * value of every cell.
 *
 * @see AbstractDataGridExporter#readRows(io.jmix.flowui.component.ListDataComponent, ExportMode,
 * java.util.function.Predicate)
 */
@NullMarked
public class DataGridRows {

    protected final List<Column> columns;
    protected final List<List<String>> rows;

    /**
     * @param columns exported columns in display order
     * @param rows    formatted cell values of each row, one per column; an empty string stands for a missing value
     */
    public DataGridRows(List<Column> columns, List<List<String>> rows) {
        this.columns = List.copyOf(columns);
        this.rows = rows.stream()
                .map(List::copyOf)
                .toList();
    }

    /**
     * Returns the exported columns.
     *
     * @return columns in display order
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Returns the formatted cell values of each row, one per column in the order of {@link #getColumns()}.
     *
     * @return rows of cell values; an empty string stands for a missing value
     */
    public List<List<String>> getRows() {
        return rows;
    }

    /**
     * An exported column.
     */
    public static class Column {

        @Nullable
        protected final String key;
        protected final String headerText;
        @Nullable
        protected final MetaPropertyPath metaPropertyPath;

        /**
         * @param key              column key, {@code null} if the column has none
         * @param headerText       header text as shown in the grid, empty if the header has no text
         * @param metaPropertyPath property path the column shows, {@code null} for a column without a property,
         *                         e.g. a generated one
         */
        public Column(@Nullable String key, String headerText, @Nullable MetaPropertyPath metaPropertyPath) {
            this.key = key;
            this.headerText = headerText;
            this.metaPropertyPath = metaPropertyPath;
        }

        /**
         * Returns the key of the column.
         *
         * @return column key, or {@code null} if the column has none
         */
        @Nullable
        public String getKey() {
            return key;
        }

        /**
         * Returns the header text as shown in the grid.
         *
         * @return header text, or an empty string if the header has no text
         */
        public String getHeaderText() {
            return headerText;
        }

        /**
         * Returns the property path the column shows.
         *
         * @return property path, or {@code null} for a column without a property, e.g. a generated one
         */
        @Nullable
        public MetaPropertyPath getMetaPropertyPath() {
            return metaPropertyPath;
        }
    }
}
