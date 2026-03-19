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

package io.jmix.flowui.component.grid;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.AggregationInfo;
import io.jmix.flowui.component.grid.sort.DataGridSort;
import io.jmix.flowui.kit.component.grid.JmixGridContextMenu;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Function;

public interface EnhancedDataGrid<T> {

    @Nullable
    MetaPropertyPath getColumnMetaPropertyPath(Grid.Column<T> column);

    /**
     * @param metaPropertyPath {@link MetaPropertyPath} that refers to the column
     * @return {@link DataGridColumn} that is bound to the passed {@code metaPropertyPath}
     */
    @Nullable
    DataGridColumn<T> getColumnByMetaPropertyPath(MetaPropertyPath metaPropertyPath);

    DataGridColumn<T> addColumn(MetaPropertyPath metaPropertyPath);

    DataGridColumn<T> addColumn(String key, MetaPropertyPath metaPropertyPath);

    boolean isEditorCreated();

    /**
     * @return true if DataGrid is aggregatable
     */
    boolean isAggregatable();

    /**
     * Set to true if aggregation should be enabled. Default value is false.
     *
     * @param aggregatable whether to aggregate DataGrid columns
     */
    void setAggregatable(boolean aggregatable);

    /**
     * @return return aggregation row position
     */
    AggregationPosition getAggregationPosition();

    /**
     * Sets aggregation row position. Default value is {@link AggregationPosition#BOTTOM}.
     *
     * @param position position: {@link AggregationPosition#TOP} or {@link AggregationPosition#BOTTOM}
     */
    void setAggregationPosition(AggregationPosition position);

    /**
     * Add an aggregation info in order to perform aggregation for column.
     *
     * @param column column for aggregation
     * @param info   aggregation info
     * @see DataGrid#setAggregatable(boolean)
     */
    void addAggregation(Grid.Column<T> column, AggregationInfo info);

    /**
     * @return aggregated values for columns
     */
    Map<Grid.Column<T>, Object> getAggregationResults();

    /**
     * @return context menu instance attached to the grid
     */
    JmixGridContextMenu<T> getContextMenu();

    // TODO: pinyazhin, javadoc
    @Nullable
    Function<DataGridSortContext, DataGridSort> getSortBuilderDelegate();

    // TODO: pinyazhin, javadoc
    void setSortBuilderDelegate(@Nullable Function<DataGridSortContext, DataGridSort> delegate);

    /**
     * Defines the position of aggregation row.
     */
    enum AggregationPosition {
        TOP,
        BOTTOM
    }

    // TODO: pinyazhin, javadoc
    class DataGridSortContext {

        protected List<ColumnSortInfo> columnSortInfos;

        public DataGridSortContext(List<ColumnSortInfo> columnSortInfos) {
            this.columnSortInfos = new ArrayList<>(columnSortInfos);
        }

        public List<ColumnSortInfo> getSortInfos() {
            return Collections.unmodifiableList(columnSortInfos);
        }

        public static class ColumnSortInfo {

            protected MetaPropertyPath metaPropertyPath;
            protected DataGridColumn<?> column;

            protected boolean ascending;

            public ColumnSortInfo(@Nullable MetaPropertyPath metaPropertyPath, DataGridColumn<?> column, boolean ascending) {
                this.metaPropertyPath = metaPropertyPath;
                this.ascending = ascending;
                this.column = column;
            }

            @Nullable
            public MetaPropertyPath getMetaPropertyPath() {
                return metaPropertyPath;
            }

            public DataGridColumn<?> getColumn() {
                return column;
            }

            public boolean isAscending() {
                return ascending;
            }
        }
    }
}
