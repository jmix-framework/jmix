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
import io.jmix.core.annotation.Experimental;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.AggregationInfo;
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter;
import io.jmix.flowui.component.grid.sort.DataGridSort;
import io.jmix.flowui.component.grid.sort.DataGridSortBuilder;
import io.jmix.flowui.kit.component.grid.JmixGridContextMenu;
import org.jspecify.annotations.Nullable;

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

    /**
     * @return a shortcut combination for applying {@link DataGridHeaderFilter}
     */
    @Nullable
    String getHeaderFilterApplyShortcut();

    /**
     * Sets a shortcut combination for applying {@link DataGridHeaderFilter}.
     *
     * @param shortcut shortcut combination (e.g. {@code "CONTROL-ENTER"})
     */
    void setHeaderFilterApplyShortcut(@Nullable String shortcut);

    /**
     * @return the delegate for building the sorting configuration of the {@link Grid} or {@code null} if not set
     */
    @Nullable
    @Experimental
    Function<DataGridSortContext<T>, DataGridSort> getSortBuilderDelegate();

    /**
     * Sets the delegate for building the sorting configuration of the {@link Grid}.
     * <p>
     * The {@link DataGridSortContext} contains sorting instructions from the grid.
     * The {@link DataGridSort} object represents the in-memory and persistent sorting to be applied.
     * <p>
     * Use {@link DataGridSortBuilder} to easily build and replace the sorting configuration.
     * <p>
     * For instance:
     * <pre>
     * &#064;Install(to = "customersGrid", subject = "sortBuilderDelegate")
     * private DataGridSort sortBuilderDelegate(final DataGridSortContext&lt;Customer&gt; context) {
     *     return DataGridSortBuilder.create(context)
     *             .replaceSort("loyaltyPointsCalc", "{E}.loyaltyPoints", ((o1, o2) -&gt; {
     *                 int calc1 = Integer.parseInt(o1.getLoyaltyPointsCalc());
     *                 int calc2 = Integer.parseInt(o2.getLoyaltyPointsCalc());
     *                 return Integer.compare(calc1, calc2);
     *             }))
     *             .build();
     * }
     * </pre>
     *
     * @param delegate a function to set
     */
    @Experimental
    void setSortBuilderDelegate(@Nullable Function<DataGridSortContext<T>, DataGridSort> delegate);

    /**
     * Defines the position of aggregation row.
     */
    enum AggregationPosition {
        TOP,
        BOTTOM
    }
}
