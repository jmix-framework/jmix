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

package io.jmix.pivottableflowui.component.settings;

import io.jmix.flowui.facet.settings.Settings;
import org.springframework.lang.Nullable;

import java.util.List;

public class PivotTableSettings implements Settings {

    protected String id;
    protected List<String> rows;
    protected List<String> cols;
    protected String rendererName;
    protected String aggregatorName;
    protected List<String> vals;
    protected List<String> inclusions;
    protected List<String> exclusions;
    protected String rowOrder;
    protected String colOrder;

    @Override
    @Nullable
    public String getId() {
        return id;
    }

    @Override
    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public List<String> getRows() {
        return rows;
    }

    public void setRows(@Nullable List<String> rows) {
        this.rows = rows;
    }

    @Nullable
    public List<String> getCols() {
        return cols;
    }

    public void setCols(@Nullable List<String> cols) {
        this.cols = cols;
    }

    @Nullable
    public String getRendererName() {
        return rendererName;
    }

    public void setRendererName(@Nullable String rendererName) {
        this.rendererName = rendererName;
    }

    @Nullable
    public String getAggregatorName() {
        return aggregatorName;
    }

    public void setAggregatorName(@Nullable String aggregatorName) {
        this.aggregatorName = aggregatorName;
    }

    @Nullable
    public List<String> getVals() {
        return vals;
    }

    public void setVals(@Nullable List<String> vals) {
        this.vals = vals;
    }

    @Nullable
    public List<String> getInclusions() {
        return inclusions;
    }

    public void setInclusions(@Nullable List<String> inclusions) {
        this.inclusions = inclusions;
    }

    @Nullable
    public List<String> getExclusions() {
        return exclusions;
    }

    public void setExclusions(@Nullable List<String> exclusions) {
        this.exclusions = exclusions;
    }

    @Nullable
    public String getRowOrder() {
        return rowOrder;
    }

    public void setRowOrder(@Nullable String rowOrder) {
        this.rowOrder = rowOrder;
    }

    @Nullable
    public String getColOrder() {
        return colOrder;
    }

    public void setColOrder(@Nullable String colOrder) {
        this.colOrder = colOrder;
    }
}
