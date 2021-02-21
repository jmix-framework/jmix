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

package io.jmix.uicharts.model;

import io.jmix.uicharts.model.chart.impl.RadarChart;
import io.jmix.uicharts.model.chart.impl.RectangularChart;
import io.jmix.uicharts.model.chart.impl.SlicedChart;
import io.jmix.uicharts.model.chart.impl.XYChart;
import io.jmix.uicharts.model.legend.Legend;
import io.jmix.uicharts.model.settings.PanelsSettings;

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
     * If is used for {@link RadarChart} the default value is 0.
     * <p>
     * If is used for chart based on {@link RectangularChart}
     * (GanttChart, SerialChart, XYChart) the default value is 20.
     * <p>
     * If is used for chart based on {@link SlicedChart} the default value is 10.
     * <p>
     * If is used for {@link PanelsSettings} the default value is 0.
     *
     * @param marginTop top spacing
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
     * If is used for {@link RadarChart} the default value is 0.
     * <p>
     * If is used for chart based on {@link RectangularChart}
     * (GanttChart, SerialChart, XYChart) the default value is 20.
     * <p>
     * If is used for chart based on {@link SlicedChart} (FunnelChart, PieChart) the default value is 10.
     * <p>
     * If is used for {@link PanelsSettings} the default value is 0.
     *
     * @param marginBottom bottom spacing
     */
    T setMarginBottom(Integer marginBottom);

    /**
     * @return left-hand spacing
     */
    Integer getMarginLeft();

    /**
     * Sets left-hand spacing.
     * <p>
     * marginLeft will be ignored if chart is {@link SerialChart} or {@link XYChart} and {@link Legend#autoMargins}
     * is true.
     * <p>
     * If is used for {@link Legend Legend} the default value is 20.
     * <p>
     * If is used for {@link RadarChart} the default value is 0.
     * <p>
     * If is used for chart based on {@link RectangularChart} (GanttChart, SerialChart, XYChart) the default value is
     * 20.
     * <p>
     * If is used for chart based on {@link SlicedChart} the default value is 0.
     * <p>
     * If is used for {@link PanelsSettings} the default value is 0.
     *
     * @param marginLeft left-hand spacing
     */
    T setMarginLeft(Integer marginLeft);

    /**
     * @return right-hand spacing
     */
    Integer getMarginRight();

    /**
     * Sets right-hand spacing.
     * <p>
     * marginRight will be ignored if chart is {@link SerialChart} or {@link XYChart} and {@link Legend#autoMargins}
     * is true.
     * <p>
     * If is used for {@link Legend Legend} the default value is 20.
     * <p>
     * If is used for {@link RadarChart} the default value is 0.
     * <p>
     * If is used for chart based on {@link RectangularChart} (GanttChart, SerialChart, XYChart) the default value is
     * 20.
     * <p>
     * If is used for chart based on {@link SlicedChart} (FunnelChart, PieChart) the default value is 0.
     * <p>
     * If is used for {@link PanelsSettings} the default value is 0.
     *
     * @param marginRight right-hand spacing
     */
    T setMarginRight(Integer marginRight);
}