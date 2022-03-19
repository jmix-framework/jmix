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



import io.jmix.core.common.event.Subscription;
import io.jmix.charts.model.chart.CoordinateChartModel;

import java.util.function.Consumer;

/**
 * Base interface for {@link GanttChart}, {@link SerialChart}, {@link RadarChart} and {@link XYChart}.
 * <br>
 * See documentation for properties of AmCoordinateChart JS object.
 * <br>
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmCoordinateChart">http://docs.amcharts.com/3/javascriptcharts/AmCoordinateChart</a>
 */
public interface CoordinateChart<T extends CoordinateChart> extends Chart<T>, CoordinateChartModel<T> {

    /**
     * Adds a listener for graph. Called when user clicks on the graph item.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addGraphClickListener(Consumer<GraphClickEvent> listener);


    /**
     * Adds a listener for graph item. Called when user clicks on the graph item.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addGraphItemClickListener(Consumer<GraphItemClickEvent> listener);


    /**
     * Adds a listener for graph item. Called when user clicks on the graph item.
     *
     * @param clickListener a listener to add
     * @return subscription
     */
    Subscription addGraphItemRightClickListener(Consumer<GraphItemRightClickEvent> clickListener);


    /**
     * Adds a listener to axis. Called when value of the axis zoom changed.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addAxisZoomListener(Consumer<AxisZoomEvent> listener);

    /**
     * Adds a listener for graph. Called when user rolls-out of a graph.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addRollOutGraphListener(Consumer<RollOutGraphEvent> listener);

    /**
     * Adds a listener for graph item. Called when user rolls-out of the data item.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addRollOutGraphItemListener(Consumer<RollOutGraphItemEvent> listener);

    /**
     * Adds a listener for graph. Called when user rolls-over a graph.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addRollOverGraphListener(Consumer<RollOverGraphEvent> listener);

    /**
     * Adds a listener for graph item. Called when user rolls-over data item.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addRollOverGraphItemListener(Consumer<RollOverGraphItemEvent> listener);

    /**
     * Zooms out value axes, value axes shows all available data.
     */
    void zoomOutValueAxes();

    /**
     * Zooms out value axis, value axis shows all available data.
     *
     * @param id id of value axis
     */
    void zoomOutValueAxis(String id);

    /**
     * Zooms out value axis, value axis shows all available data.
     *
     * @param index index of value axis
     */
    void zoomOutValueAxis(int index);

    /**
     * Zooms-in an axis to the provided values.
     *
     * @param id         id of value axis
     * @param startValue start value
     * @param endValue   end value
     */
    void zoomValueAxisToValues(String id, Object startValue, Object endValue);

    /**
     * Zooms-in an axis to the provided values.
     *
     * @param index      index of value axis
     * @param startValue start value
     * @param endValue   end value
     */
    void zoomValueAxisToValues(int index, Object startValue, Object endValue);
}