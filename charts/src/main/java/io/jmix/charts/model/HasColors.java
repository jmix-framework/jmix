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


import io.jmix.charts.model.chart.StockChartModel;
import io.jmix.charts.model.chart.impl.CoordinateChartModelImpl;
import io.jmix.charts.model.chart.impl.SlicedChartModelImpl;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioCollection;
import io.jmix.ui.meta.StudioProperty;

import java.util.List;

public interface HasColors<T> {

    /**
     * @return List of colors
     */
    List<Color> getColors();

    /**
     * Sets the list of colors.
     * <p>
     * If you use charts based on {@link SlicedChartModelImpl} it specifies the colors of the slices, if the slice color is
     * not set. If there are more slices than colors in this array, the chart picks random color.
     *
     * <p>
     * If you use charts based on {@link CoordinateChartModelImpl} or {@link StockChartModel} it specifies the colors of the graphs
     * if the lineColor of a graph is not set. If there are more graphs then colors in this array, the chart picks a
     * random color. If not set the default value is
     * <pre>{@code
     *  ["#FF6600", "#FCD202", "#B0DE09",
     *   "#0D8ECF", "#2A0CD0", "#CD0D74",
     *   "#CC0000", "#00CC00", "#0000CC",
     *   "#DDDDDD", "#999999", "#333333",
     *   "#990000"]
     * }</pre>
     *
     * <p>
     * If you use chart based on {@link SlicedChartModelImpl} (PieChart, FunnelChart). Specifies the colors of the slices, if
     * the slice color is not set. If there are more slices than colors in this array, the chart picks random color.
     * If not set default value is
     * <pre>{@code
     *  ["#FF0F00", "#FF6600", "#FF9E01", "#FCD202",
     *   "#F8FF01", "#B0DE09", "#04D215", "#0D8ECF",
     *   "#0D52D1", "#2A0CD0", "#8A0CCF", "#CD0D74",
     *   "#754DEB", "#DDDDDD", "#999999", "#333333",
     *   "#000000", "#57032A", "#CA9726", "#990000",
     *  "#4B0C25"]
     * }</pre>
     *
     * @param colors list of colors
     * @return chart
     */
    @StudioCollection(xmlElement = "colors",
            itemXmlElement = "color",
            itemCaption = "Color",
            itemProperties = {
                    @StudioProperty(name = "value", type = PropertyType.ENUMERATION,
                            options = {"@link io.jmix.charts.model.Color"})
            })
    T setColors(List<Color> colors);

    /**
     * Adds colors.
     *
     * @param colors list of colors
     * @return chart
     */
    T addColors(Color... colors);
}