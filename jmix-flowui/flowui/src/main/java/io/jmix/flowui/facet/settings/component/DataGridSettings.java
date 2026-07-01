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

package io.jmix.flowui.facet.settings.component;

import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.facet.settings.Settings;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Represents settings for a {@link DataGrid} component, such as sorting, column configurations,
 * and other configuration properties that define the behavior of the data grid.
 */
public class DataGridSettings implements Settings {

    protected String id;
    protected List<SortOrder> sortOrder;
    protected List<Column> columns;


    /**
     * Returns the unique identifier of {@link DataGrid}.
     *
     * @return the identifier of {@link DataGrid}
     */
    @Nullable
    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of {@link DataGrid}.
     *
     * @param id the identifier of {@link DataGrid} to set
     */
    @Override
    public void setId(@Nullable String id) {
        this.id = id;
    }

    /**
     * Returns the list of sort orders applied to {@link DataGrid}.
     *
     * @return list of sort orders, or {@code null} if not set
     */
    @Nullable
    public List<SortOrder> getSortOrder() {
        return sortOrder;
    }

    /**
     * Sets the list of sort orders for {@link DataGrid}.
     *
     * @param sortOrder list of sort orders to apply
     */
    public void setSortOrder(@Nullable List<SortOrder> sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Returns the list of column configurations for {@link DataGrid}.
     *
     * @return list of column configurations or {@code null} if not set
     */
    @Nullable
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Sets the list of column configurations for {@link DataGrid}.
     *
     * @param columns list of column configurations to set
     */
    public void setColumns(@Nullable List<Column> columns) {
        this.columns = columns;
    }

    /**
     * Represents a single sort order configuration used in {@link DataGrid} settings.
     * It contains a sort key and a sort direction that define the ordering criteria.
     */
    public static class SortOrder {

        protected String key;
        protected String sortDirection;

        /**
         * Returns the sort key associated with this sort order.
         *
         * @return the sort key, or {@code null} if not specified
         */
        @Nullable
        public String getKey() {
            return key;
        }

        /**
         * Sets the sort key associated with this sort order.
         *
         * @param key the sort key to set, may be {@code null}
         */
        public void setKey(@Nullable String key) {
            this.key = key;
        }

        /**
         * Returns the sort direction associated with this sort order.
         *
         * @return the sort direction
         */
        public String getSortDirection() {
            return sortDirection;
        }

        /**
         * Sets the sort direction for the sort order configuration.
         *
         * @param sortDirection the sort direction to set
         */
        public void setSortDirection(String sortDirection) {
            this.sortDirection = sortDirection;
        }
    }

    /**
     * Represents settings for a column of a {@link DataGrid} component,
     * including its properties such as key, width, visibility, and flexibility.
     */
    public static class Column {

        protected String key;
        protected String width;
        protected Boolean visible;
        protected int flexGrow;


        /**
         * Returns the key of the column.
         *
         * @return the key of the column, or {@code null} if not set
         */
        @Nullable
        public String getKey() {
            return key;
        }

        /**
         * Sets the key of the column.
         *
         * @param key the key of the column, or {@code null} if no key is set
         */
        public void setKey(@Nullable String key) {
            this.key = key;
        }

        /**
         * Returns the width of the column.
         *
         * @return the width of the column, or {@code null} if no width is set
         */
        @Nullable
        public String getWidth() {
            return width;
        }

        /**
         * Sets the width of the column.
         *
         * @param width the new width of the column, or {@code null} if no width is set
         */
        public void setWidth(@Nullable String width) {
            this.width = width;
        }

        /**
         * Returns the visibility state of the column.
         *
         * @return {@code true} if the column is visible, {@code false} if it is hidden,
         * or {@code null} if the visibility is not explicitly set
         */
        @Nullable
        public Boolean getVisible() {
            return visible;
        }

        /**
         * Sets the visibility state of the column.
         *
         * @param visible the new visibility state of the column; {@code true} if the column
         *                should be visible, {@code false} if it should be hidden,
         *                or {@code null} if the visibility is not explicitly set
         */
        public void setVisible(@Nullable Boolean visible) {
            this.visible = visible;
        }

        /**
         * Returns the flex grow value of the column.
         *
         * @return the flex grow value representing the relative flexibility
         */
        public int getFlexGrow() {
            return flexGrow;
        }

        /**
         * Sets the flex grow value for the column.
         *
         * @param flexGrow the flex grow value to set
         */
        public void setFlexGrow(int flexGrow) {
            this.flexGrow = flexGrow;
        }
    }
}
