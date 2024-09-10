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

package io.jmix.pivottableflowui.kit.event.js;

import io.jmix.pivottableflowui.kit.component.model.AggregationMode;
import io.jmix.pivottableflowui.kit.component.model.Order;
import io.jmix.pivottableflowui.kit.component.model.Renderer;

import java.util.List;
import java.util.Map;

/**
 * Contains deserialized json data from {@link PivotTableJsRefreshEvent}
 */
public class PivotTableRefreshEventParams {

    private List<String> rows;
    private List<String> cols;
    private Renderer renderer;
    private AggregationMode aggregationMode;
    private List<String> aggregationProperties;
    private Map<String, List<String>> inclusions;
    private Map<String, List<String>> exclusions;
    private Order colOrder;
    private Order rowOrder;

    public List<String> getRows() {
        return rows;
    }

    public void setRows(List<String> rows) {
        this.rows = rows;
    }

    public List<String> getCols() {
        return cols;
    }

    public void setCols(List<String> cols) {
        this.cols = cols;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public AggregationMode getAggregationMode() {
        return aggregationMode;
    }

    public void setAggregationMode(AggregationMode aggregationMode) {
        this.aggregationMode = aggregationMode;
    }

    public List<String> getAggregationProperties() {
        return aggregationProperties;
    }

    public void setAggregationProperties(List<String> aggregationProperties) {
        this.aggregationProperties = aggregationProperties;
    }

    public Map<String, List<String>> getInclusions() {
        return inclusions;
    }

    public void setInclusions(Map<String, List<String>> inclusions) {
        this.inclusions = inclusions;
    }

    public Map<String, List<String>> getExclusions() {
        return exclusions;
    }

    public void setExclusions(Map<String, List<String>> exclusions) {
        this.exclusions = exclusions;
    }

    public Order getColOrder() {
        return colOrder;
    }

    public void setColOrder(Order colOrder) {
        this.colOrder = colOrder;
    }

    public Order getRowOrder() {
        return rowOrder;
    }

    public void setRowOrder(Order rowOrder) {
        this.rowOrder = rowOrder;
    }
}