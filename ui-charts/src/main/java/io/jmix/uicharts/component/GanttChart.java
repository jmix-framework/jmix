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

package io.jmix.uicharts.component;


import io.jmix.uicharts.model.chart.GanttChartModel;

/**
 * Gantt chart component. It displays multiple bars on one series where value axis displays date/time and is horizontal.
 * <br>
 * See documentation for properties of AmGanttChart JS object.
 * <br>
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmGanttChart">http://docs.amcharts.com/3/javascriptcharts/AmGanttChart</a>
 */
public interface GanttChart extends SeriesBasedChart<GanttChart>, GanttChartModel<GanttChart> {
    String NAME = "ganttChart";
}