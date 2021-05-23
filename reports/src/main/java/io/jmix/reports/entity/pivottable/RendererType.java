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

package io.jmix.reports.entity.pivottable;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

public enum RendererType implements EnumClass<String> {
    TABLE("table"),
    TABLE_BAR_CHART("tableBarchart"),
    HEATMAP("heatmap"),
    ROW_HEATMAP("rowHeatmap"),
    COL_HEATMAP("colHeatmap"),
    LINE_CHART("lineChart"),
    BAR_CHART("barChart"),
    STACKED_BAR_CHART("stackedBarChart"),
    AREA_CHART("areaChart"),
    SCATTER_CHART("scatterChart"),
    //TREEMAP("treemap"),
    TSV_EXPORT("TSVExport");

    private String id;

    RendererType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public static RendererType fromId(String id) {
        for (RendererType type : RendererType.values()) {
            if (type.getId().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
