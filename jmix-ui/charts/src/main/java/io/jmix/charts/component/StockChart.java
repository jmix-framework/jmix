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
import io.jmix.ui.data.impl.EntityDataItem;
import io.jmix.charts.model.chart.StockChartModel;
import io.jmix.charts.model.chart.impl.StockPanel;
import io.jmix.charts.model.date.DatePeriod;
import io.jmix.charts.model.period.PeriodType;
import io.jmix.charts.model.stock.StockEvent;
import io.jmix.charts.model.stock.StockGraph;
import io.jmix.ui.meta.CanvasIconSize;
import io.jmix.ui.meta.StudioComponent;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Stock chart component.
 * <br>
 * See documentation for properties of AmStockChart JS object.
 * <br>
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/AmStockChart">http://docs.amcharts.com/3/javascriptstockchart/AmStockChart</a>
 */
@StudioComponent(
        caption = "StockChart",
        category = "Charts",
        xmlElement = "stockChart",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart",
        icon = "io/jmix/charts/icon/component/stockChart.svg",
        canvasIcon = "io/jmix/charts/icon/component/stockChart.svg",
        canvasIconSize = CanvasIconSize.LARGE
)
public interface StockChart extends Component, StockChartModel<StockChart>, Component.BelongToFrame,
        Component.HasIcon, Component.HasCaption {
    String NAME = "stockChart";

    /**
     * Resend all items and properties to client and repaint chart.
     * Use this method if you change some property of already displayed chart.
     */
    void repaint();

    /**
     * Adds a listener to stock chart click events. Called when user clicks on the stock chart.
     *
     * @param clickListener a listener to add
     * @return subscription
     */
    Subscription addClickListener(Consumer<StockChartClickEvent> clickListener);


    /**
     * Adds listener to stock chart right click events. Called when user clicks on the stock chart.
     *
     * @param clickListener a listener to add
     * @return subscription
     */
    Subscription addRightClickListener(Consumer<StockChartRightClickEvent> clickListener);

    /**
     * Adds listener to stock event click events. Called when user clicks on the stock event.
     *
     * @param clickListener a listener to add
     * @return subscription
     */
    Subscription addStockEventClickListener(Consumer<StockEventClickEvent> clickListener);

    /**
     * Adds a listener to stock event roll-out events. Called when the stock event did roll-out.
     *
     * @param rollOutListener a listener to add
     * @return subscription
     */
    Subscription addStockEventRollOutListener(Consumer<StockEventRollOutEvent> rollOutListener);

    /**
     * Adds listener to stock event roll-over events. Called when the stock event did roll-over.
     *
     * @param rollOverListener a listener to add
     * @return subscription
     */
    Subscription addStockEventRollOverListener(Consumer<StockEventRollOverEvent> rollOverListener);


    /**
     * Adds listener to stock chart zoom events.
     *
     * @param zoomListener a listener to add
     * @return subscription
     */
    Subscription addZoomListener(Consumer<ZoomEvent> zoomListener);

    /**
     * Adds a listener to period selector change events. Called when the period selector changed.
     *
     * @param changeListener a listener
     * @return subscription
     */
    Subscription addPeriodSelectorChangeListener(Consumer<PeriodSelectorChangeEvent> changeListener);

    /**
     * Adds a listener to dataset selector compare events. Called when the data set is selected for comparing.
     *
     * @param compareListener a listener to add
     * @return subscription
     */
    Subscription addDataSetSelectorCompareListener(Consumer<DataSetSelectorCompareEvent> compareListener);

    /**
     * Adds a listener to dataset selector select events. Called when the dataset selector selected.
     *
     * @param selectListener a listener to add
     * @return subscription
     */
    Subscription addDataSetSelectorSelectListener(Consumer<DataSetSelectorSelectEvent> selectListener);

    /**
     * Adds a listener to dataset selector uncompare events. Called when the dataset selector removed from
     * comparison.
     *
     * @param unCompareListener a listener to add
     * @return subscription
     */
    Subscription addDataSetSelectorUnCompareListener(Consumer<DataSetSelectorUnCompareEvent> unCompareListener);

    /**
     * Adds a listener to stock graph click events. Called when user clicks on the stock graph.
     *
     * @param clickListener a listener to add
     * @return subscription
     */
    Subscription addStockGraphClickListener(Consumer<StockGraphClickEvent> clickListener);

    /**
     * Adds a listener to stock graph roll-out events. Called when the stock graph did roll-out.
     *
     * @param rollOutListener a listener to add
     * @return subscription
     */
    Subscription addStockGraphRollOutListener(Consumer<StockGraphRollOutEvent> rollOutListener);

    /**
     * Adds a listener to stock graph roll-over events. Called when the stock graph did roll-over.
     *
     * @param rollOverListener a listener to add
     * @return subscription
     */
    Subscription addStockGraphRollOverListener(Consumer<StockGraphRollOverEvent> rollOverListener);

    /**
     * Adds a listener to stock graph item click events. Called when user clicks on the stock graph item.
     *
     * @param clickListener a listener to add
     * @return subscription
     */
    Subscription addStockGraphItemClickListener(Consumer<StockGraphItemClickEvent> clickListener);

    /**
     * Adds a listener to stock graph item right click events. Called when user clicks on the stock graph item.
     *
     * @param clickListener a listener to add
     * @return subscription
     */
    Subscription addStockGraphItemRightClickListener(Consumer<StockGraphItemRightClickEvent> clickListener);

    /**
     * Adds a listener to stock graph item roll-out events. Called when the stock graph item did roll-out.
     *
     * @param rollOutListener a listener to add
     * @return subscription
     */
    Subscription addStockGraphItemRollOutListener(Consumer<StockGraphItemRollOutEvent> rollOutListener);

    /**
     * Adds a listener to stock graph item roll-over events.
     *
     * @param rollOverListener a listener to add
     * @return subscription
     */
    Subscription addStockGraphItemRollOverListener(Consumer<StockGraphItemRollOverEvent> rollOverListener);

    /**
     * Set additional JSON configuration as a string.
     * This JSON can override configuration loaded from XML and from Component API.
     * @param json additional JSON configuration
     */
    void setNativeJson(String json);

    /**
     * @return additional JSON configuration as a string.
     */
    String getNativeJson();

    abstract class AbstractStockChartEvent extends EventObject {

        public AbstractStockChartEvent(StockChart stockChart) {
            super(stockChart);
        }

        @Override
        public StockChart getSource() {
            return (StockChart) super.getSource();
        }
    }

    abstract class AbstractStockChartClickEvent extends AbstractStockChartEvent {
        private final int x;
        private final int y;
        private final int absoluteX;
        private final int absoluteY;

        public AbstractStockChartClickEvent(StockChart stockChart, int x, int y, int absoluteX, int absoluteY) {
            super(stockChart);
            this.x = x;
            this.y = y;
            this.absoluteX = absoluteX;
            this.absoluteY = absoluteY;
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
    }

    /**
     * Describes stock chart click event.
     */
    class StockChartClickEvent extends AbstractStockChartClickEvent {
        public StockChartClickEvent(StockChart stockChart, int x, int y, int absoluteX, int absoluteY) {
            super(stockChart, x, y, absoluteX, absoluteY);
        }
    }

    /**
     * Describes stock chart right click event.
     */
    class StockChartRightClickEvent extends AbstractStockChartClickEvent {
        public StockChartRightClickEvent(StockChart stockChart, int x, int y, int absoluteX, int absoluteY) {
            super(stockChart, x, y, absoluteX, absoluteY);
        }
    }

    /**
     * Describes StockEvent event.
     */
    abstract class AbstractStockEventEvent extends AbstractStockChartEvent {
        private final String graphId;
        private final Date date;
        private final StockEvent stockEvent;
        private final StockGraph graph;

        protected AbstractStockEventEvent(StockChart stockChart, StockGraph graph, String graphId, Date date,
                                          StockEvent stockEvent) {
            super(stockChart);
            this.graph = graph;
            this.graphId = graphId;
            this.date = date;
            this.stockEvent = stockEvent;
        }

        /**
         * @return graph id
         */
        public String getGraphId() {
            return graphId;
        }

        /**
         * @return stock event date
         */
        public Date getDate() {
            return date;
        }

        /**
         * @return stock event
         */
        public StockEvent getStockEvent() {
            return stockEvent;
        }

        /**
         * @return null or stock graph
         */
        public StockGraph getStockGraph() {
            return graph;
        }
    }

    /**
     * Describes StockEvent click event.
     */
    class StockEventClickEvent extends AbstractStockEventEvent {
        public StockEventClickEvent(StockChart stockChart, StockGraph graph, String graphId, Date date,
                                    StockEvent stockEvent) {
            super(stockChart, graph, graphId, date, stockEvent);
        }
    }

    /**
     * Describes StockEvent roll-out event.
     */
    class StockEventRollOutEvent extends AbstractStockEventEvent {
        public StockEventRollOutEvent(StockChart stockChart, StockGraph graph, String graphId, Date date, StockEvent stockEvent) {
            super(stockChart, graph, graphId, date, stockEvent);
        }
    }

    /**
     * Describes StockEvent roll-over event.
     */
    class StockEventRollOverEvent extends AbstractStockEventEvent {
        public StockEventRollOverEvent(StockChart stockChart, StockGraph graph, String graphId, Date date, StockEvent stockEvent) {
            super(stockChart, graph, graphId, date, stockEvent);
        }
    }

    /**
     * Describes zoom event.
     */
    class ZoomEvent extends AbstractStockChartEvent {
        private final Date startDate;
        private final Date endDate;
        private final DatePeriod period;

        public ZoomEvent(StockChart stockChart, Date startDate, Date endDate, DatePeriod period) {
            super(stockChart);
            this.startDate = startDate;
            this.endDate = endDate;
            this.period = period;
        }

        /**
         * @return end date of the chart zoom period
         */
        public Date getEndDate() {
            return endDate;
        }

        /**
         * @return start date of the chart zoom period
         */
        public Date getStartDate() {
            return startDate;
        }

        /**
         * @return date period
         */
        public DatePeriod getPeriod() {
            return period;
        }
    }

    /**
     * Describes period selector change event.
     */
    class PeriodSelectorChangeEvent extends AbstractStockChartEvent {
        private final Date startDate;
        private final Date endDate;

        private final PeriodType predefinedPeriod;
        private final Integer count;

        private final int x;
        private final int y;
        private final int absoluteX;
        private final int absoluteY;

        public PeriodSelectorChangeEvent(StockChart stockChart, Date startDate, Date endDate,
                                         PeriodType predefinedPeriod, Integer count, int x, int y, int absoluteX,
                                         int absoluteY) {
            super(stockChart);
            this.startDate = startDate;
            this.endDate = endDate;
            this.predefinedPeriod = predefinedPeriod;
            this.count = count;
            this.x = x;
            this.y = y;
            this.absoluteX = absoluteX;
            this.absoluteY = absoluteY;
        }

        /**
         * @return period start date
         */
        public Date getStartDate() {
            return startDate;
        }

        /**
         * @return period end date
         */
        public Date getEndDate() {
            return endDate;
        }

        /**
         * @return predefined period type
         */
        public PeriodType getPredefinedPeriod() {
            return predefinedPeriod;
        }

        public Integer getCount() {
            return count;
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

    /**
     * Describes dataset selector event.
     */
    abstract class AbstractDataSetSelectorEvent extends AbstractStockChartEvent {
        private final String dataSetId;

        protected AbstractDataSetSelectorEvent(StockChart stockChart, String dataSetId) {
            super(stockChart);
            this.dataSetId = dataSetId;
        }

        /**
         * @return dataset id
         */
        public String getDataSetId() {
            return dataSetId;
        }
    }

    /**
     * Describes dataset selector compare event.
     */
    class DataSetSelectorCompareEvent extends AbstractDataSetSelectorEvent {
        public DataSetSelectorCompareEvent(StockChart stockChart, String dataSetId) {
            super(stockChart, dataSetId);
        }
    }

    /**
     * Describes dataset selector select event.
     */
    class DataSetSelectorSelectEvent extends AbstractDataSetSelectorEvent {
        public DataSetSelectorSelectEvent(StockChart stockChart, String dataSetId) {
            super(stockChart, dataSetId);
        }
    }

    /**
     * Describes dataset selector uncompare event.
     */
    class DataSetSelectorUnCompareEvent extends AbstractDataSetSelectorEvent {
        public DataSetSelectorUnCompareEvent(StockChart stockChart, String dataSetId) {
            super(stockChart, dataSetId);
        }
    }

    /**
     * Describes stock graph event.
     */
    abstract class AbstractStockGraphEvent extends AbstractStockChartEvent {

        private final String panelId;
        private final String graphId;
        private final StockGraph stockGraph;
        private final StockPanel stockPanel;
        private final int x;
        private final int y;
        private final int absoluteX;
        private final int absoluteY;

        protected AbstractStockGraphEvent(StockChart stockChart, StockPanel stockPanel, String panelId,
                                          StockGraph stockGraph, String graphId, int x, int y,
                                          int absoluteX, int absoluteY) {
            super(stockChart);
            this.panelId = panelId;
            this.graphId = graphId;
            this.stockGraph = stockGraph;
            this.stockPanel = stockPanel;
            this.x = x;
            this.y = y;
            this.absoluteX = absoluteX;
            this.absoluteY = absoluteY;
        }

        /**
         * @return panel id
         */
        public String getPanelId() {
            return panelId;
        }

        /**
         * @return graph id
         */
        public String getGraphId() {
            return graphId;
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

        /**
         * @return null or stock graph
         */
        public StockGraph getStockGraph() {
            return stockGraph;
        }

        /**
         * @return null or stock panel
         */
        public StockPanel getStockPanel() {
            return stockPanel;
        }
    }

    /**
     * Describes stock graph click event.
     */
    class StockGraphClickEvent extends AbstractStockGraphEvent {
        public StockGraphClickEvent(StockChart stockChart, StockPanel stockPanel, String panelId,
                                    StockGraph stockGraph, String graphId, int x, int y, int absoluteX, int absoluteY) {
            super(stockChart, stockPanel, panelId, stockGraph, graphId, x, y, absoluteX, absoluteY);
        }
    }

    /**
     * Describes stock graph roll-out event.
     */
    class StockGraphRollOutEvent extends AbstractStockGraphEvent {
        public StockGraphRollOutEvent(StockChart stockChart, StockPanel stockPanel, String panelId,
                                      StockGraph stockGraph, String graphId, int x, int y, int absoluteX,
                                      int absoluteY) {
            super(stockChart, stockPanel, panelId, stockGraph, graphId, x, y, absoluteX, absoluteY);
        }
    }

    /**
     * Describes stock graph roll over event.
     */
    class StockGraphRollOverEvent extends AbstractStockGraphEvent {
        public StockGraphRollOverEvent(StockChart stockChart, StockPanel stockPanel, String panelId,
                                       StockGraph stockGraph, String graphId, int x, int y, int absoluteX,
                                       int absoluteY) {
            super(stockChart, stockPanel, panelId, stockGraph, graphId, x, y, absoluteX, absoluteY);
        }
    }

    /**
     * Describes stock graph item event.
     */
    abstract class AbstractStockGraphItemEvent extends AbstractStockChartEvent {
        private final String panelId;
        private final String graphId;

        private final StockPanel stockPanel;
        private final StockGraph stockGraph;

        private final int x;
        private final int y;
        private final int absoluteX;
        private final int absoluteY;

        private final DataItem dataItem;
        private final int itemIndex;

        protected AbstractStockGraphItemEvent(StockChart stockChart, StockPanel stockPanel, String panelId,
                                              StockGraph stockGraph, String graphId, DataItem dataItem,
                                              int itemIndex, int x, int y, int absoluteX, int absoluteY) {
            super(stockChart);
            this.panelId = panelId;
            this.dataItem = dataItem;
            this.stockPanel = stockPanel;
            this.stockGraph = stockGraph;
            this.itemIndex = itemIndex;
            this.absoluteY = absoluteY;
            this.absoluteX = absoluteX;
            this.graphId = graphId;
            this.x = x;
            this.y = y;
        }

        /**
         * @return panel id
         */
        public String getPanelId() {
            return panelId;
        }

        /**
         * @return graph id
         */
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

        /**
         * @return item index
         */
        public int getItemIndex() {
            return itemIndex;
        }

        /**
         * @return the X coordinate of the mouse pointer in the chart coordinates.
         */
        public int getX() {
            return x;
        }

        /**
         * @return the X coordinate of the mouse pointer in the chart coordinates.
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

        /**
         * @return null or panel that contains clicked stock graph item
         */
        public StockPanel getStockPanel() {
            return stockPanel;
        }

        /**
         * @return null or stock graph that contains clicked item
         */
        public StockGraph getStockGraph() {
            return stockGraph;
        }
    }

    /**
     * Describes stock graph item click event.
     */
    class StockGraphItemClickEvent extends AbstractStockGraphItemEvent {
        public StockGraphItemClickEvent(StockChart stockChart, StockPanel stockPanel, String panelId,
                                        StockGraph stockGraph, String graphId, DataItem item, int itemIndex, int x,
                                        int y, int absoluteX, int absoluteY) {
            super(stockChart, stockPanel, panelId, stockGraph, graphId, item, itemIndex, x, y, absoluteX, absoluteY);
        }
    }

    /**
     * Describes stock graph item click event.
     */
    class StockGraphItemRightClickEvent extends AbstractStockGraphItemEvent {
        public StockGraphItemRightClickEvent(StockChart stockChart, StockPanel stockPanel, String panelId,
                                             StockGraph stockGraph, String graphId, DataItem item,
                                             int itemIndex, int x, int y, int absoluteX, int absoluteY) {
            super(stockChart, stockPanel, panelId, stockGraph, graphId, item, itemIndex, x, y, absoluteX, absoluteY);
        }
    }

    /**
     * Describes stock graph item roll-out event.
     */
    class StockGraphItemRollOutEvent extends AbstractStockGraphItemEvent {
        public StockGraphItemRollOutEvent(StockChart stockChart, StockPanel stockPanel, String panelId,
                                          StockGraph stockGraph, String graphId, DataItem item, int itemIndex, int x,
                                          int y, int absoluteX, int absoluteY) {
            super(stockChart, stockPanel, panelId, stockGraph, graphId, item, itemIndex, x, y, absoluteX, absoluteY);
        }
    }

    /**
     * Describes stock graph item roll-over event.
     */
    class StockGraphItemRollOverEvent extends AbstractStockGraphItemEvent {
        public StockGraphItemRollOverEvent(StockChart stockChart, StockPanel stockPanel, String panelId,
                                           StockGraph stockGraph, String graphId, DataItem item,
                                           int itemIndex, int x, int y, int absoluteX, int absoluteY) {
            super(stockChart, stockPanel, panelId, stockGraph, graphId, item, itemIndex, x, y, absoluteX, absoluteY);
        }
    }
}