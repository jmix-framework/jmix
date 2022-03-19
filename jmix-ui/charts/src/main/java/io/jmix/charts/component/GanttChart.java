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


import io.jmix.charts.model.chart.GanttChartModel;
import io.jmix.ui.meta.CanvasIconSize;
import io.jmix.ui.meta.StudioComponent;

/**
 * Gantt chart component. It displays multiple bars on one series where value axis displays date/time and is horizontal.
 * <br>
 * See documentation for properties of AmGanttChart JS object.
 * <br>
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmGanttChart">http://docs.amcharts.com/3/javascriptcharts/AmGanttChart</a>
 */
@StudioComponent(
        caption = "GanttChart",
        category = "Charts",
        xmlElement = "ganttChart",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart",
        icon = "io/jmix/charts/icon/component/ganttChart.svg",
        canvasIcon = "io/jmix/charts/icon/component/ganttChart.svg",
        canvasIconSize = CanvasIconSize.LARGE
)
public interface GanttChart extends SeriesBasedChart<GanttChart>, GanttChartModel<GanttChart> {
    String NAME = "ganttChart";
}