/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.kit.event;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;
import io.jmix.pivottableflowui.kit.component.model.AggregationMode;
import io.jmix.pivottableflowui.kit.component.model.Order;
import io.jmix.pivottableflowui.kit.component.model.Renderer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Describes the PivotTable refresh event. Fires each time the state of the pivot table changes.
 */
public class PivotTableRefreshEvent extends ComponentEvent<JmixPivotTable> {

    protected List<String> rows;
    protected List<String> cols;
    protected Renderer renderer;
    protected AggregationMode aggregationMode;
    protected List<String> aggregationProperties;
    protected Map<String, List<String>> inclusions;
    protected Map<String, List<String>> exclusions;
    protected Order colOrder;
    protected Order rowOrder;

    public PivotTableRefreshEvent(JmixPivotTable pivotTable, List<String> rows, List<String> cols, Renderer renderer,
                                  AggregationMode aggregationMode, List<String> aggregationProperties,
                                  Map<String, List<String>> inclusions, Map<String, List<String>> exclusions,
                                  Order colOrder, Order rowOrder) {
        super(pivotTable, false);

        this.rows = rows;
        this.cols = cols;
        this.renderer = renderer;
        this.aggregationMode = aggregationMode;
        this.aggregationProperties = aggregationProperties;
        this.inclusions = inclusions;
        this.exclusions = exclusions;
        this.colOrder = colOrder;
        this.rowOrder = rowOrder;
    }

    /**
     * @return currently selected properties as rows
     */
    public List<String> getRows() {
        return rows;
    }

    /**
     * @return currently selected properties as columns
     */
    public List<String> getCols() {
        return cols;
    }

    /**
     * @return currently selected renderer, or null if not selected
     */
    @Nullable
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * @return currently selected aggregation, or null if not selected
     */
    @Nullable
    public AggregationMode getAggregationMode() {
        return aggregationMode;
    }

    /**
     * @return currently selected aggregation properties, or empty if not selected
     */
    public List<String> getAggregationProperties() {
        return aggregationProperties;
    }

    /**
     * @return currently defined map whose keys are properties names and values are lists
     * of properties values which denote records to include in rendering; used to prepopulate
     * the filter menus that appear on double-click
     */
    public Map<String, List<String>> getInclusions() {
        return inclusions;
    }

    /**
     * @return currently defined map whose keys are properties names and values are lists
     * of properties values which denote records to exclude from rendering; used to prepopulate
     * the filter menus that appear on double-click
     */
    public Map<String, List<String>> getExclusions() {
        return exclusions;
    }

    /**
     * @return currently selected columns order
     */
    public Order getColumnOrder() {
        return colOrder;
    }

    /**
     * @return currently selected rows order
     */
    public Order getRowOrder() {
        return rowOrder;
    }
}
