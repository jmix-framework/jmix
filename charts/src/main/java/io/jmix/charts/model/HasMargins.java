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

package io.jmix.charts.model;

import io.jmix.charts.component.SerialChart;
import io.jmix.charts.model.chart.impl.RadarChartModelImpl;
import io.jmix.charts.model.chart.impl.RectangularChartModelImpl;
import io.jmix.charts.model.chart.impl.SlicedChartModelImpl;
import io.jmix.charts.model.chart.impl.XYChartModelImpl;
import io.jmix.charts.model.legend.Legend;
import io.jmix.charts.model.settings.PanelsSettings;

public interface HasMargins<T> {

    /**
     * @return top spacing
     */
    Integer getMarginTop();

    /**
     * Sets top spacing. If not set the default value is 10.
     * <p>
     * If is used for {@link Legend Legend} the default value is 0.
     * <p>
     * If is used for {@link RadarChartModelImpl} the default value is 0.
     * <p>
     * If is used for chart based on {@link RectangularChartModelImpl}
     * (GanttChart, SerialChart, XYChart) the default value is 20.
     * <p>
     * If is used for chart based on {@link SlicedChartModelImpl} the default value is 10.
     * <p>
     * If is used for {@link PanelsSettings} the default value is 0.
     *
     * @param marginTop top spacing
     * @return object with set top spacing
     */
    T setMarginTop(Integer marginTop);

    /**
     * @return bottom spacing
     */
    Integer getMarginBottom();

    /**
     * Sets bottom spacing. If not set the default value is 10.
     * <p>
     * If is used for {@link Legend Legend} default value is 0.
     * <p>
     * If is used for {@link RadarChartModelImpl} the default value is 0.
     * <p>
     * If is used for chart based on {@link RectangularChartModelImpl}
     * (GanttChart, SerialChart, XYChart) the default value is 20.
     * <p>
     * If is used for chart based on {@link SlicedChartModelImpl} (FunnelChart, PieChart) the default value is 10.
     * <p>
     * If is used for {@link PanelsSettings} the default value is 0.
     *
     * @param marginBottom bottom spacing
     * @return object with set bottom spacing
     */
    T setMarginBottom(Integer marginBottom);

    /**
     * @return left-hand spacing
     */
    Integer getMarginLeft();

    /**
     * Sets left-hand spacing.
     * <p>
     * marginLeft will be ignored if chart is {@link SerialChart} or {@link XYChartModelImpl} and {@link Legend#autoMargins}
     * is true.
     * <p>
     * If is used for {@link Legend Legend} the default value is 20.
     * <p>
     * If is used for {@link RadarChartModelImpl} the default value is 0.
     * <p>
     * If is used for chart based on {@link RectangularChartModelImpl} (GanttChart, SerialChart, XYChart) the default value is
     * 20.
     * <p>
     * If is used for chart based on {@link SlicedChartModelImpl} the default value is 0.
     * <p>
     * If is used for {@link PanelsSettings} the default value is 0.
     *
     * @param marginLeft left-hand spacing
     * @return object with set left-hand spacing
     */
    T setMarginLeft(Integer marginLeft);

    /**
     * @return right-hand spacing
     */
    Integer getMarginRight();

    /**
     * Sets right-hand spacing.
     * <p>
     * marginRight will be ignored if chart is {@link SerialChart} or {@link XYChartModelImpl} and {@link Legend#autoMargins}
     * is true.
     * <p>
     * If is used for {@link Legend Legend} the default value is 20.
     * <p>
     * If is used for {@link RadarChartModelImpl} the default value is 0.
     * <p>
     * If is used for chart based on {@link RectangularChartModelImpl} (GanttChart, SerialChart, XYChart) the default value is
     * 20.
     * <p>
     * If is used for chart based on {@link SlicedChartModelImpl} (FunnelChart, PieChart) the default value is 0.
     * <p>
     * If is used for {@link PanelsSettings} the default value is 0.
     *
     * @param marginRight right-hand spacing
     * @return object with set right-hand spacing
     */
    T setMarginRight(Integer marginRight);
}