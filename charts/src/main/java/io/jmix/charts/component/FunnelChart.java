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


import io.jmix.charts.model.chart.FunnelChartModel;

/**
 * Funnel / pyramid chart component.
 * <br>
 * See documentation for properties of AmFunnelChart JS object.
 * <br>
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmFunnelChart">http://docs.amcharts.com/3/javascriptcharts/AmFunnelChart</a>
 */
public interface FunnelChart extends SlicedChart<FunnelChart>, FunnelChartModel<FunnelChart> {
    String NAME = "funnelChart";
}