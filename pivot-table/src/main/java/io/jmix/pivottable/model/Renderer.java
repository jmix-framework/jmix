/*
 * Copyright 2021 Haulmont.
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

package io.jmix.pivottable.model;

import javax.annotation.Nullable;

/**
 * An enum with predefined renderers.
 */
public enum Renderer implements JsonEnum {
    // standard
    TABLE("table"),
    TABLE_BAR_CHART("tableBarchart"),
    HEATMAP("heatmap"),
    ROW_HEATMAP("rowHeatmap"),
    COL_HEATMAP("colHeatmap"),
    // c3
    LINE_CHART("lineChart"),
    BAR_CHART("barChart"),
    STACKED_BAR_CHART("stackedBarChart"),
    HORIZONTAL_BAR_CHART("horizontalBarChart"),
    HORIZONTAL_STACKED_BAR_CHART("horizontalStackedBarChart"),
    AREA_CHART("areaChart"),
    SCATTER_CHART("scatterChart"),
    // d3
    TREEMAP("treemap"),
    // export
    TSV_EXPORT("TSVExport");

    private String id;

    Renderer(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static Renderer fromId(String id) {
        for (Renderer renderer : values()) {
            if (renderer.getId().equals(id)) {
                return renderer;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return id;
    }
}
