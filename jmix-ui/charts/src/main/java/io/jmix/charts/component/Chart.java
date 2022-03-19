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


import io.jmix.core.Entity;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.Component;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.DataProvider;
import io.jmix.ui.data.impl.ContainerDataProvider;
import io.jmix.ui.data.impl.EntityDataItem;
import io.jmix.ui.data.impl.ListDataProvider;
import io.jmix.charts.model.chart.ChartModel;
import io.jmix.charts.model.graph.Graph;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Base interface for all *Chart components.
 *
 * @param <T> type of builder methods
 */
public interface Chart<T extends Chart> extends Component, ChartModel<T>, Component.BelongToFrame, Component.HasIcon,
        Component.HasCaption {

    /**
     * @return the data provider
     */
    @Override
    DataProvider getDataProvider();

    /**
     * @param dataProvider the data provider
     * @return chart
     * @see ContainerDataProvider
     * @see ListDataProvider
     */
    @Override
    T setDataProvider(DataProvider dataProvider);

    /**
     * Resend all items and properties to client and repaint chart.
     * Use this method if you change some property of already displayed chart.
     */
    void repaint();

    /**
     * Adds a listener for a chart. Called when user clicks on the chart.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addClickListener(Consumer<ChartClickEvent> listener);

    /**
     * Adds a listener for a chart. Called when user clicks on the chart.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addRightClickListener(Consumer<ChartRightClickEvent> listener);

    /**
     * Adds a listener for a chart. Called when the legend item hided.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addLegendItemHideListener(Consumer<LegendItemHideEvent> listener);

    /**
     * Adds a listener for a chart. Called when the legend item showed.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addLegendItemShowListener(Consumer<LegendItemShowEvent> listener);

    /**
     * Adds a listener for a chart. Called when user clicks on the legend item.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addLegendLabelClickListener(Consumer<LegendItemClickEvent> listener);

    /**
     * Adds a listener for a chart. Called when user clicks on the legend marker.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addLegendMarkerClickListener(Consumer<LegendMarkerClickEvent> listener);

    /**
     * Set additional JSON configuration as a string.
     * This JSON can override configuration loaded from XML and from Component API.
     * @param json JSON configuration
     */
    void setNativeJson(String json);

    /**
     * @return additional JSON configuration as a string.
     */
    String getNativeJson();

    abstract class AbstractChartEvent extends EventObject {

        public AbstractChartEvent(Chart source) {
            super(source);
        }

        @Override
        public Chart getSource() {
            return (Chart) super.getSource();
        }
    }

    abstract class AbstractItemEvent extends AbstractChartEvent {
        private final DataItem dataItem;

        public AbstractItemEvent(Chart chart, DataItem dataItem) {
            super(chart);
            this.dataItem = dataItem;
        }


        @Nullable
        public DataItem getDataItem() {
            return dataItem;
        }

        public DataItem getDataItemNN() {
            if (dataItem == null) {
                throw new IllegalStateException("dataItem is null");
            }
            return dataItem;
        }

        @Nullable
        public Entity getEntity() {
            if (dataItem != null) {
                return ((EntityDataItem) dataItem).getItem();
            }
            return null;
        }

        public Entity getEntityNN() {
            if (dataItem == null) {
                throw new IllegalStateException("dataItem is null");
            }
            return ((EntityDataItem) dataItem).getItem();
        }
    }

    abstract class AbstractClickEvent extends AbstractChartEvent {
        private final int x;
        private final int y;
        private final int absoluteX;
        private final int absoluteY;

        public AbstractClickEvent(Chart chart, int x, int y, int absoluteX, int absoluteY) {
            super(chart);
            this.x = x;
            this.y = y;
            this.absoluteX = absoluteX;
            this.absoluteY = absoluteY;
        }

        /**
         * @return the X coordinate of the mouse pointer in the chart coordinates.
         */
        public int getX() {
            return x;
        }

        /**
         * @return the Y coordinate of the mouse pointer in the chart coordinates.
         */
        public int getY() {
            return y;
        }

        /**
         * @return the X coordinate of the mouse pointer in local (DOM content) coordinates.
         */
        public int getAbsoluteX() {
            return absoluteX;
        }

        /**
         * @return the Y coordinate of the mouse pointer in local (DOM content) coordinates.
         */
        public int getAbsoluteY() {
            return absoluteY;
        }
    }

    abstract class AbstractChartClickEvent extends AbstractClickEvent {
        private final double xAxis;
        private final double yAxis;

        public AbstractChartClickEvent(Chart chart, int x, int y, int absoluteX, int absoluteY, double xAxis,
                                       double yAxis) {
            super(chart, x, y, absoluteX, absoluteY);
            this.xAxis = xAxis;
            this.yAxis = yAxis;
        }

        public double getxAxis() {
            return xAxis;
        }

        public double getyAxis() {
            return yAxis;
        }
    }

    abstract class AbstractGraphItemClickEvent extends AbstractClickEvent {
        private final String graphId;
        private final Graph graph;
        private final DataItem dataItem;
        private final int itemIndex;

        public AbstractGraphItemClickEvent(Chart chart, Graph graph, String graphId, DataItem dataItem,
                                           int itemIndex, int x, int y, int absoluteX, int absoluteY) {
            super(chart, x, y, absoluteX, absoluteY);
            this.graph = graph;
            this.dataItem = dataItem;
            this.itemIndex = itemIndex;
            this.graphId = graphId;
        }

        public String getGraphId() {
            return graphId;
        }

        @Nullable
        public DataItem getDataItem() {
            return dataItem;
        }

        public DataItem getDataItemNN() {
            if (dataItem == null) {
                throw new IllegalStateException("dataItem is null");
            }
            return dataItem;
        }

        @Nullable
        public Entity getEntity() {
            if (dataItem != null) {
                return ((EntityDataItem) dataItem).getItem();
            }
            return null;
        }

        public Entity getEntityNN() {
            if (dataItem == null) {
                throw new IllegalStateException("dataItem is null");
            }
            return ((EntityDataItem) dataItem).getItem();
        }

        public int getItemIndex() {
            return itemIndex;
        }

        /**
         * @return null or graph that contains clicked graph item
         */
        public Graph getGraph() {
            return graph;
        }
    }

    abstract class AbstractSliceClickEvent extends AbstractClickEvent {
        private final DataItem dataItem;

        public AbstractSliceClickEvent(Chart chart, DataItem dataItem, int x, int y, int absoluteX, int absoluteY) {
            super(chart, x, y, absoluteX, absoluteY);
            this.dataItem = dataItem;
        }


        @Nullable
        public DataItem getDataItem() {
            return dataItem;
        }

        public DataItem getDataItemNN() {
            if (dataItem == null) {
                throw new IllegalStateException("dataItem is null");
            }
            return dataItem;
        }

        @Nullable
        public Entity getEntity() {
            if (dataItem != null) {
                return ((EntityDataItem) dataItem).getItem();
            }
            return null;
        }

        public Entity getEntityNN() {
            if (dataItem == null) {
                throw new IllegalStateException("dataItem is null");
            }
            return ((EntityDataItem) dataItem).getItem();
        }
    }

    abstract class AbstractCursorEvent extends AbstractChartEvent {
        private final String start;
        private final String end;

        public AbstractCursorEvent(Chart chart, String start, String end) {
            super(chart);
            this.start = start;
            this.end = end;
        }

        /**
         * @return start period
         */
        public String getStart() {
            return start;
        }

        /**
         * @return end period
         */
        public String getEnd() {
            return end;
        }
    }

    /**
     * Describes axis zoom event.
     */
    class AxisZoomEvent {
        private final String axisId;
        private final double startValue;
        private final double endValue;

        public AxisZoomEvent(String axisId, double startValue, double endValue) {
            this.axisId = axisId;
            this.startValue = startValue;
            this.endValue = endValue;
        }

        /**
         * @return axis id
         */
        public String getAxisId() {
            return axisId;
        }

        /**
         * @return axis end value
         */
        public double getEndValue() {
            return endValue;
        }

        /**
         * @return axis start value
         */
        public double getStartValue() {
            return startValue;
        }
    }

    /**
     * Describes chart click event.
     */
    class ChartClickEvent extends AbstractChartClickEvent {
        public ChartClickEvent(Chart chart, int x, int y, int absoluteX, int absoluteY, double xAxis, double yAxis) {
            super(chart, x, y, absoluteX, absoluteY, xAxis, yAxis);
        }
    }

    /**
     * Describes chart click event.
     */
    class ChartRightClickEvent extends AbstractChartClickEvent {
        public ChartRightClickEvent(Chart chart, int x, int y, int absoluteX, int absoluteY, double xAxis,
                                    double yAxis) {
            super(chart, x, y, absoluteX, absoluteY, xAxis, yAxis);
        }
    }

    /**
     * Describes cursor period select event.
     */
    class CursorPeriodSelectEvent extends AbstractCursorEvent {

        public CursorPeriodSelectEvent(Chart chart, String start, String end) {
            super(chart, start, end);
        }
    }

    /**
     * Describes cursor zoom event.
     */
    class CursorZoomEvent extends AbstractCursorEvent {
        public CursorZoomEvent(Chart chart, String start, String end) {
            super(chart, start, end);
        }
    }

    /**
     * Describes graph click event.
     */
    class GraphClickEvent extends AbstractClickEvent {

        private final String graphId;

        public GraphClickEvent(Chart chart, String graphId, int x, int y, int absoluteX, int absoluteY) {
            super(chart, x, y, absoluteX, absoluteY);
            this.graphId = graphId;
        }

        /**
         * @return graph id
         */
        public String getGraphId() {
            return graphId;
        }
    }

    /**
     * Describes graph item click event.
     */
    class GraphItemClickEvent extends AbstractGraphItemClickEvent {
        public GraphItemClickEvent(Chart chart, Graph graph, String graphId, DataItem item, int itemIndex, int x, int y,
                                   int absoluteX, int absoluteY) {
            super(chart, graph, graphId, item, itemIndex, x, y, absoluteX, absoluteY);
        }
    }

    /**
     * Describes graph item click event.
     */
    class GraphItemRightClickEvent extends AbstractGraphItemClickEvent {
        public GraphItemRightClickEvent(Chart chart, Graph graph, String graphId, DataItem item, int itemIndex, int x,
                                        int y, int absoluteX, int absoluteY) {
            super(chart, graph, graphId, item, itemIndex, x, y, absoluteX, absoluteY);
        }
    }

    /**
     * Describes legend item hide event.
     */
    class LegendItemHideEvent extends LegendItemEvent {
        public LegendItemHideEvent(Chart chart, int itemIndex, DataItem dataItem) {
            super(chart, itemIndex, dataItem);
        }
    }

    /**
     * Describes legend item show event.
     */
    class LegendItemShowEvent extends LegendItemEvent {
        public LegendItemShowEvent(Chart chart, int itemIndex, DataItem dataItem) {
            super(chart, itemIndex, dataItem);
        }
    }

    /**
     * Describes legend item click event.
     */
    abstract class LegendItemEvent extends AbstractChartEvent {
        private final int itemIndex;
        private final DataItem dataItem;

        public LegendItemEvent(Chart chart, int itemIndex, DataItem dataItem) {
            super(chart);
            this.itemIndex = itemIndex;
            this.dataItem = dataItem;
        }

        @Override
        public Chart getSource() {
            return (Chart) super.getSource();
        }

        public int getItemIndex() {
            return itemIndex;
        }

        @Nullable
        public DataItem getDataItem() {
            return dataItem;
        }

        public DataItem getDataItemNN() {
            if (dataItem == null) {
                throw new IllegalStateException("dataItem is null");
            }
            return dataItem;
        }

        @Nullable
        public Entity getEntity() {
            if (dataItem != null) {
                return ((EntityDataItem) dataItem).getItem();
            }
            return null;
        }

        public Entity getEntityNN() {
            if (dataItem == null) {
                throw new IllegalStateException("dataItem is null");
            }
            return ((EntityDataItem) dataItem).getItem();
        }
    }

    /**
     * Describes legend item click event.
     */
    class LegendItemClickEvent extends LegendItemEvent {
        public LegendItemClickEvent(Chart chart, int itemIndex, DataItem dataItem) {
            super(chart, itemIndex, dataItem);
        }
    }

    /**
     * Describes legend marker click event.
     */
    class LegendMarkerClickEvent extends LegendItemEvent {
        public LegendMarkerClickEvent(Chart chart, int itemIndex, DataItem dataItem) {
            super(chart, itemIndex, dataItem);
        }
    }

    /**
     * Describes slice click event.
     */
    class SliceClickEvent extends AbstractSliceClickEvent {
        public SliceClickEvent(Chart chart, DataItem dataItem, int x, int y, int absoluteX, int absoluteY) {
            super(chart, dataItem, x, y, absoluteX, absoluteY);
        }
    }

    /**
     * Describes slice click event.
     */
    class SliceRightClickEvent extends AbstractSliceClickEvent {
        public SliceRightClickEvent(Chart chart, DataItem dataItem, int x, int y, int absoluteX, int absoluteY) {
            super(chart, dataItem, x, y, absoluteX, absoluteY);
        }
    }

    /**
     * Describes slice pull-in event.
     */
    class SlicePullInEvent extends AbstractItemEvent {
        public SlicePullInEvent(Chart chart, DataItem item) {
            super(chart, item);
        }
    }

    /**
     * Describes slice pull-out event.
     */
    class SlicePullOutEvent extends AbstractItemEvent {
        public SlicePullOutEvent(Chart chart, DataItem item) {
            super(chart, item);
        }
    }

    /**
     * Describes the graph roll-out event.
     */
    class RollOutGraphEvent extends AbstractChartEvent {

        private final Graph graph;

        public RollOutGraphEvent(Chart chart, Graph graph) {
            super(chart);
            this.graph = graph;
        }

        /**
         * @return a graph
         */
        public Graph getGraph() {
            return graph;
        }
    }

    /**
     * Describes the data item roll-out event.
     */
    class RollOutGraphItemEvent extends RollOutGraphEvent {

        private final DataItem dataItem;
        private final int itemIndex;

        public RollOutGraphItemEvent(Chart chart, Graph graph, DataItem dataItem, int itemIndex) {
            super(chart, graph);
            this.dataItem = dataItem;
            this.itemIndex = itemIndex;
        }

        /**
         * @return a data item
         */
        public DataItem getDataItem() {
            return dataItem;
        }

        /**
         * @return an item index
         */
        public int getItemIndex() {
            return itemIndex;
        }
    }

    /**
     * Describes the graph roll-over event.
     */
    class RollOverGraphEvent extends AbstractChartEvent {

        private final Graph graph;

        public RollOverGraphEvent(Chart chart, Graph graph) {
            super(chart);
            this.graph = graph;
        }

        /**
         * @return a graph
         */
        public Graph getGraph() {
            return graph;
        }
    }

    /**
     * Describes the data item roll-over event.
     */
    class RollOverGraphItemEvent extends RollOverGraphEvent {

        private final DataItem dataItem;
        private final int itemIndex;

        public RollOverGraphItemEvent(Chart chart, Graph graph, DataItem dataItem, int itemIndex) {
            super(chart, graph);
            this.dataItem = dataItem;
            this.itemIndex = itemIndex;
        }

        /**
         * @return a data item
         */
        public DataItem getDataItem() {
            return dataItem;
        }

        /**
         * @return an item index
         */
        public int getItemIndex() {
            return itemIndex;
        }
    }

    /**
     * Describes zoom event.
     */
    class ZoomEvent extends AbstractChartEvent {
        private final int startIndex;
        private final int endIndex;
        private final Date startDate;
        private final Date endDate;
        private final String startValue;
        private final String endValue;

        public ZoomEvent(Chart chart, int startIndex, int endIndex,
                         Date startDate, Date endDate, String startValue, String endValue) {
            super(chart);
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.startDate = startDate;
            this.endDate = endDate;
            this.startValue = startValue;
            this.endValue = endValue;
        }

        /**
         * @return end date of the chart zoom period
         */
        public Date getEndDate() {
            return endDate;
        }

        /**
         * @return end category index of the chart zoom period
         */
        public int getEndIndex() {
            return endIndex;
        }

        /**
         * @return end category value of the chart zoom period
         */
        public String getEndValue() {
            return endValue;
        }

        /**
         * @return start date of the chart zoom period
         */
        public Date getStartDate() {
            return startDate;
        }

        /**
         * @return start category index of the chart zoom period
         */
        public int getStartIndex() {
            return startIndex;
        }

        /**
         * @return start category value of the chart zoom period
         */
        public String getStartValue() {
            return startValue;
        }
    }
}