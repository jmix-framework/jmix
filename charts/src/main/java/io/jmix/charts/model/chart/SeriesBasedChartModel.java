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

package io.jmix.charts.model.chart;


import io.jmix.charts.model.Scrollbar;
import io.jmix.charts.model.axis.CategoryAxis;
import io.jmix.charts.model.cursor.Cursor;
import io.jmix.ui.meta.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@StudioProperties(groups = {
        @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                properties = {"dataContainer", "categoryField"})
})
public interface SeriesBasedChartModel<T extends SeriesBasedChartModel> extends RectangularChartModel<T> {
    /**
     * @return category axis
     */
    CategoryAxis getCategoryAxis();

    /**
     * Sets the category axis.
     *
     * @param categoryAxis the category axis
     * @return  chart model
     */
    @StudioElement
    T setCategoryAxis(CategoryAxis categoryAxis);

    /**
     * @return category field name
     */
    String getCategoryField();

    /**
     * Sets the category field name. It tells the chart the name of the field from your data provider object which will
     * be used for category axis values.
     *
     * @param categoryField category field name string
     * @return  chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setCategoryField(String categoryField);

    /**
     * @return balloon date format
     */
    String getBalloonDateFormat();

    /**
     * Sets date format of the graph balloon (if chart parses dates and you don't use
     * {@link Cursor}). If not set the default value is "MMM DD, YYYY".
     *
     * @param balloonDateFormat the balloon date format
     * @return  chart model
     */
    @StudioProperty(defaultValue = "MMM DD, YYYY")
    T setBalloonDateFormat(String balloonDateFormat);

    /**
     * @return space between 3D stacked columns
     */
    Integer getColumnSpacing3D();

    /**
     * Sets space between 3D stacked columns. If not set the default value is 0.
     *
     * @param columnSpacing3D space between 3D stacked columns
     * @return  chart model
     */
    @StudioProperty(defaultValue = "0")
    T setColumnSpacing3D(Integer columnSpacing3D);

    /**
     * @return column spacing in pixels
     */
    Integer getColumnSpacing();

    /**
     * Sets the gap in pixels between two columns of the same category. If not set the default value is 5.
     *
     * @param columnSpacing column spacing in pixels
     * @return  chart model
     */
    @StudioProperty(defaultValue = "5")
    T setColumnSpacing(Integer columnSpacing);

    /**
     * @return relative width of columns
     */
    Double getColumnWidth();

    /**
     * Sets relative width of columns. Value range is 0 - 1. If not set the default value is 0.8.
     *
     * @param columnWidth relative width of columns
     * @return  chart model
     */
    @StudioProperty(defaultValue = "0.8")
    @Max(1)
    @Min(0)
    T setColumnWidth(Double columnWidth);

    /**
     * @return data date format
     */
    String getDataDateFormat();

    /**
     * Sets data date format. Even if your chart parses dates, you can pass them as strings in your data – all you need
     * to do is to set data date format and the chart will parse dates to date objects. Please note that two-digit
     * years "YY" as well as literal month names "MMM" are NOT supported in this setting.
     *
     * @param dataDateFormat data date format string
     * @return  chart model
     */
    @StudioProperty
    T setDataDateFormat(String dataDateFormat);

    /**
     * @return maximum number of selected series
     */
    Integer getMaxSelectedSeries();

    /**
     * Sets maximum number of series allowed to select.
     *
     * @param maxSelectedSeries the maximum number of selected series
     * @return  chart model
     */
    @StudioProperty
    T setMaxSelectedSeries(Integer maxSelectedSeries);

    /**
     * @return maximum selected time in milliseconds
     */
    Long getMaxSelectedTime();

    /**
     * Sets the longest time span allowed to select in milliseconds for example, 259200000 will limit selection to 3
     * days. Works if {@link CategoryAxis#equalSpacing} is set to false.
     *
     * @param maxSelectedTime the maximum selected time in milliseconds
     * @return  chart model
     */
    @StudioProperty
    T setMaxSelectedTime(Long maxSelectedTime);

    /**
     * @return minimum selected time in milliseconds
     */
    Long getMinSelectedTime();

    /**
     * Sets the shortest time span allowed to select in milliseconds for example, 1000 will limit selection to 1
     * second. Works if {@link CategoryAxis#equalSpacing} is set to false. If not set the default value is 0.
     *
     * @param minSelectedTime the minimum selected time in milliseconds
     * @return  chart model
     */
    @StudioProperty(defaultValue = "0")
    T setMinSelectedTime(Long minSelectedTime);

    /**
     * @return true if scroll chart with the mouse wheel is enabled
     */
    Boolean getMouseWheelScrollEnabled();

    /**
     * Set true if you want scroll chart with the mouse wheel. If you press shift while rotating mouse wheel, the
     * chart will zoom-in/out. If not set the default value is false.
     *
     * @param mouseWheelScrollEnabled mouse wheel scroll option
     * @return  chart model
     */
    @StudioProperty(defaultValue = "false")
    T setMouseWheelScrollEnabled(Boolean mouseWheelScrollEnabled);

    /**
     * @return true if rotate is enabled
     */
    Boolean getRotate();

    /**
     * Set rotate to true, if the chart should be rotated by 90 degrees (the columns will become bars). If not set
     * the default value is false.
     *
     * @param rotate rotate option
     * @return  chart model
     */
    @StudioProperty(defaultValue = "false")
    T setRotate(Boolean rotate);

    /**
     * @return true if chart should be zoom-out when data is updated
     */
    Boolean getZoomOutOnDataUpdate();

    /**
     * Set zoomOutOnDataUpdate to true if chart should be zoom-out when data is updated. If not set the default value is
     * true.
     *
     * @param zoomOutOnDataUpdate zoomOutOnDataUpdate option
     * @return  chart model
     */
    @StudioProperty(defaultValue = "true")
    T setZoomOutOnDataUpdate(Boolean zoomOutOnDataUpdate);

    /**
     * @return true if zooming of a chart with mouse wheel is enabled
     */
    Boolean getMouseWheelZoomEnabled();

    /**
     * Set mouseWheelZoomEnabled to true if zooming of a chart with mouse wheel should be enabled. If you press shift
     * while rotating mouse wheel, the chart will scroll. If not set the default value is false.
     *
     * @param mouseWheelZoomEnabled mouseWheelZoomEnabled option
     * @return  chart model
     */
    @StudioProperty(defaultValue = "false")
    T setMouseWheelZoomEnabled(Boolean mouseWheelZoomEnabled);

    /**
     * @return value scrollbar
     */
    Scrollbar getValueScrollbar();

    /**
     * Sets value scrollbar, enables scrolling value axes.
     *
     * @param valueScrollbar the value scrollbar
     * @return  chart model
     */
    @StudioElement(caption = "Value Scrollbar", xmlElement = "valueScrollbar")
    T setValueScrollbar(Scrollbar valueScrollbar);

    /**
     * @return true if synchronized grid is enabled
     */
    Boolean getSynchronizeGrid();

    /**
     * Set synchronizeGrid property to true and the chart will adjust minimum and maximum of axes so that the grid
     * would be show at equal intervals. This helps users to compare values more easily.
     *
     * @param synchronizeGrid synchronized grid option
     * @return  chart model
     */
    @StudioProperty
    T setSynchronizeGrid(Boolean synchronizeGrid);
}