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
import io.jmix.charts.model.chart.SeriesBasedChartModel;

import java.util.Date;
import java.util.function.Consumer;

/**
 * Base interface for {@link SerialChart} and {@link GanttChart}.
 */
public interface SeriesBasedChart<T extends SeriesBasedChart> extends RectangularChart<T>, SeriesBasedChartModel<T> {
    /**
     * Adds a listener for zoom. Called when value of the chart zoom changed.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addZoomListener(Consumer<ZoomEvent> listener);

    /**
     * Adds a listener for category item click. Called when user clicks on the category.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addCategoryItemClickListener(Consumer<CategoryItemClickEvent> listener);


    /**
     * Zooms out, charts shows all available data.
     */
    void zoomOut();

    /**
     * Zooms the chart by the index of the category.
     *
     * @param start start index
     * @param end   end index
     */
    void zoomToIndexes(int start, int end);

    /**
     * Zooms the chart from one date to another.
     *
     * @param start start date
     * @param end   end date
     */
    void zoomToDates(Date start, Date end);

    /**
     * Describes clickItem event on categories in the CategoryAxis.
     * <br>
     * See documentation for properties of clickItem event in the CategoryAxis.
     * <br>
     * <a href="https://docs.amcharts.com/3/javascriptcharts/CategoryAxis#clickItem">https://docs.amcharts.com/3/javascriptcharts/CategoryAxis#clickItem</a>
     */
    class CategoryItemClickEvent extends AbstractChartEvent {
        private String value;

        private int offsetX;
        private int offsetY;

        private int x;
        private int y;

        private int xAxis;
        private int yAxis;

        public CategoryItemClickEvent(Chart chart, String value, int x, int y, int offsetX, int offsetY, int xAxis,
                                      int yAxis) {
            super(chart);
            this.value = value;
            this.x = x;
            this.y = y;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.xAxis = xAxis;
            this.yAxis = yAxis;
        }

        /**
         * @return category value that represents in String type
         */
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        /**
         * @return offset in the X coordinate of the mouse pointer between that event and the padding edge of the
         * target node
         */
        public int getOffsetX() {
            return offsetX;
        }

        public void setOffsetX(int offsetX) {
            this.offsetX = offsetX;
        }

        /**
         * @return offset in the Y coordinate of the mouse pointer between that event and the padding edge of the
         * target node
         */
        public int getOffsetY() {
            return offsetY;
        }

        public void setOffsetY(int offsetY) {
            this.offsetY = offsetY;
        }

        /**
         * @return X coordinate within the application's client area
         */
        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        /**
         * @return Y coordinate within the application's client area
         */
        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        /**
         * @return X coordinate of the axis
         */
        public int getXAxis() {
            return xAxis;
        }

        public void setXAxis(int xAxis) {
            this.xAxis = xAxis;
        }

        /**
         * @return Y coordinate of the axis
         */
        public int getYAxis() {
            return yAxis;
        }

        public void setYAxis(int yAxis) {
            this.yAxis = yAxis;
        }
    }
}