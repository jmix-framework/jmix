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

package io.jmix.charts.component;


import io.jmix.charts.model.chart.PieChartModel;
import io.jmix.ui.meta.CanvasIconSize;
import io.jmix.ui.meta.StudioComponent;

/**
 * Pie / donut chart component.
 * <br>
 * See documentation for properties of AmPieChart JS object.
 * <br>
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmPieChart">http://docs.amcharts.com/3/javascriptcharts/AmPieChart</a>
 */
@StudioComponent(
        caption = "PieChart",
        category = "Charts",
        xmlElement = "pieChart",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart",
        icon = "io/jmix/charts/icon/component/pieChart.svg",
        canvasIcon = "io/jmix/charts/icon/component/pieChart.svg",
        canvasIconSize = CanvasIconSize.LARGE
)
public interface PieChart extends SlicedChart<PieChart>, PieChartModel<PieChart> {
    String NAME = "pieChart";
}