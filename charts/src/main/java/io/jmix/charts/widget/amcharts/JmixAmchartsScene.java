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

package io.jmix.charts.widget.amcharts;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vaadin.server.KeyMapper;
import com.vaadin.ui.AbstractComponent;
import io.jmix.ui.data.DataChangeListener;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.DataItemsChangeEvent;
import io.jmix.ui.data.DataProvider;
import io.jmix.ui.widget.EnhancedUI;
import io.jmix.charts.model.chart.ChartType;
import io.jmix.charts.model.chart.impl.*;
import io.jmix.charts.widget.amcharts.events.axis.AxisZoomEvent;
import io.jmix.charts.widget.amcharts.events.axis.AxisZoomListener;
import io.jmix.charts.widget.amcharts.events.category.CategoryItemClickEvent;
import io.jmix.charts.widget.amcharts.events.category.CategoryItemClickListener;
import io.jmix.charts.widget.amcharts.events.chart.ChartClickEvent;
import io.jmix.charts.widget.amcharts.events.chart.ChartRightClickEvent;
import io.jmix.charts.widget.amcharts.events.chart.listener.ChartClickListener;
import io.jmix.charts.widget.amcharts.events.chart.listener.ChartRightClickListener;
import io.jmix.charts.widget.amcharts.events.cursor.CursorPeriodSelectEvent;
import io.jmix.charts.widget.amcharts.events.cursor.CursorZoomEvent;
import io.jmix.charts.widget.amcharts.events.cursor.listener.CursorPeriodSelectListener;
import io.jmix.charts.widget.amcharts.events.cursor.listener.CursorZoomListener;
import io.jmix.charts.widget.amcharts.events.graph.GraphClickEvent;
import io.jmix.charts.widget.amcharts.events.graph.GraphItemClickEvent;
import io.jmix.charts.widget.amcharts.events.graph.GraphItemRightClickEvent;
import io.jmix.charts.widget.amcharts.events.graph.listener.GraphClickListener;
import io.jmix.charts.widget.amcharts.events.graph.listener.GraphItemClickListener;
import io.jmix.charts.widget.amcharts.events.graph.listener.GraphItemRightClickListener;
import io.jmix.charts.widget.amcharts.events.legend.LegendItemHideEvent;
import io.jmix.charts.widget.amcharts.events.legend.LegendItemShowEvent;
import io.jmix.charts.widget.amcharts.events.legend.LegendLabelClickEvent;
import io.jmix.charts.widget.amcharts.events.legend.LegendMarkerClickEvent;
import io.jmix.charts.widget.amcharts.events.legend.listener.LegendItemHideListener;
import io.jmix.charts.widget.amcharts.events.legend.listener.LegendItemShowListener;
import io.jmix.charts.widget.amcharts.events.legend.listener.LegendLabelClickListener;
import io.jmix.charts.widget.amcharts.events.legend.listener.LegendMarkerClickListener;
import io.jmix.charts.widget.amcharts.events.roll.RollOutGraphEvent;
import io.jmix.charts.widget.amcharts.events.roll.RollOutGraphItemEvent;
import io.jmix.charts.widget.amcharts.events.roll.RollOverGraphEvent;
import io.jmix.charts.widget.amcharts.events.roll.RollOverGraphItemEvent;
import io.jmix.charts.widget.amcharts.events.roll.listener.RollOutGraphItemListener;
import io.jmix.charts.widget.amcharts.events.roll.listener.RollOutGraphListener;
import io.jmix.charts.widget.amcharts.events.roll.listener.RollOverGraphItemListener;
import io.jmix.charts.widget.amcharts.events.roll.listener.RollOverGraphListener;
import io.jmix.charts.widget.amcharts.events.slice.SliceClickEvent;
import io.jmix.charts.widget.amcharts.events.slice.SlicePullInEvent;
import io.jmix.charts.widget.amcharts.events.slice.SlicePullOutEvent;
import io.jmix.charts.widget.amcharts.events.slice.SliceRightClickEvent;
import io.jmix.charts.widget.amcharts.events.slice.listener.SliceClickListener;
import io.jmix.charts.widget.amcharts.events.slice.listener.SlicePullInListener;
import io.jmix.charts.widget.amcharts.events.slice.listener.SlicePullOutListener;
import io.jmix.charts.widget.amcharts.events.slice.listener.SliceRightClickListener;
import io.jmix.charts.widget.amcharts.events.zoom.ZoomEvent;
import io.jmix.charts.widget.amcharts.events.zoom.ZoomListener;
import io.jmix.charts.widget.amcharts.serialization.ChartIncrementalChanges;
import io.jmix.charts.widget.amcharts.serialization.ChartSerializer;
import io.jmix.charts.widget.client.amcharts.rpc.JmixAmchartsSceneClientRpc;
import io.jmix.charts.widget.client.amcharts.rpc.JmixAmchartsServerRpc;
import io.jmix.charts.widget.client.amcharts.state.JmixAmchartsSceneState;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static com.vaadin.util.ReflectTools.findMethod;

public class JmixAmchartsScene extends AbstractComponent {
    private static final Logger log = LoggerFactory.getLogger(JmixAmchartsScene.class);

    protected final static Method chartClickMethod =
            findMethod(ChartClickListener.class, "onClick", ChartClickEvent.class);

    protected final static Method chartRightClickMethod =
            findMethod(ChartRightClickListener.class, "onClick", ChartRightClickEvent.class);

    protected final static Method graphClickMethod =
            findMethod(GraphClickListener.class, "onClick", GraphClickEvent.class);

    protected final static Method graphItemClickMethod =
            findMethod(GraphItemClickListener.class, "onClick", GraphItemClickEvent.class);

    protected final static Method graphItemRightClickMethod =
            findMethod(GraphItemRightClickListener.class, "onClick", GraphItemRightClickEvent.class);

    protected final static Method rollOutGraphMethod =
            findMethod(RollOutGraphListener.class, "onRollOut", RollOutGraphEvent.class);

    protected final static Method rollOutGraphItemMethod =
            findMethod(RollOutGraphItemListener.class, "onRollOut", RollOutGraphItemEvent.class);

    protected final static Method rollOverGraphMethod =
            findMethod(RollOverGraphListener.class, "onRollOver", RollOverGraphEvent.class);

    protected final static Method rollOverGraphItemMethod =
            findMethod(RollOverGraphItemListener.class, "onRollOver", RollOverGraphItemEvent.class);

    protected final static Method zoomMethod =
            findMethod(ZoomListener.class, "onZoom", ZoomEvent.class);

    protected final static Method sliceClickMethod =
            findMethod(SliceClickListener.class, "onClick", SliceClickEvent.class);

    protected final static Method sliceRightClickMethod =
            findMethod(SliceRightClickListener.class, "onClick", SliceRightClickEvent.class);

    protected final static Method slicePullInMethod =
            findMethod(SlicePullInListener.class, "onClick", SlicePullInEvent.class);

    protected final static Method slicePullOutMethod =
            findMethod(SlicePullOutListener.class, "onClick", SlicePullOutEvent.class);
    protected final static Method legendLabelClickMethod =
            findMethod(LegendLabelClickListener.class, "onClick", LegendLabelClickEvent.class);

    protected final static Method legendMarkerClickMethod =
            findMethod(LegendMarkerClickListener.class, "onClick", LegendMarkerClickEvent.class);

    protected final static Method legendItemShowMethod =
            findMethod(LegendItemShowListener.class, "onShow", LegendItemShowEvent.class);

    protected final static Method legendItemHideMethod =
            findMethod(LegendItemHideListener.class, "onHide", LegendItemHideEvent.class);

    protected final static Method cursorZoomMethod =
            findMethod(CursorZoomListener.class, "onZoom", CursorZoomEvent.class);

    protected final static Method cursorPeriodSelectMethod =
            findMethod(CursorPeriodSelectListener.class, "onSelect", CursorPeriodSelectEvent.class);

    protected final static Method axisZoomMethod =
            findMethod(AxisZoomListener.class, "onZoom", AxisZoomEvent.class);

    protected final static Method categoryItemClickMethod =
            findMethod(CategoryItemClickListener.class, "onClick", CategoryItemClickEvent.class);

    protected final DataChangeListener changeListener = new ProxyChangeForwarder(this);

    protected boolean dirty = false;

    protected AbstractChart chart;

    protected ChartIncrementalChanges changedItems;

    protected ChartSerializer chartSerializer;
    protected KeyMapper<Object> dataItemKeys = new KeyMapper<>();
    protected Function<DataItem, String> dataItemKeyMapper;

    public JmixAmchartsScene() {
        // enable amcharts integration
        JmixAmchartsIntegration.get();

        dataItemKeyMapper = item -> {
            if (item instanceof DataItem.HasId) {
                return dataItemKeys.key(((DataItem.HasId) item).getId());
            }
            return null;
        };

        registerRpc(new JmixAmchartsServerRpcImpl(), JmixAmchartsServerRpc.class);
    }

    public void setChartSerializer(ChartSerializer chartSerializer) {
        this.chartSerializer = chartSerializer;
        this.chartSerializer.setDataItemKeyMapper(dataItemKeyMapper);
    }

    @Override
    protected JmixAmchartsSceneState getState() {
        return (JmixAmchartsSceneState) super.getState();
    }

    @Override
    protected JmixAmchartsSceneState getState(boolean markAsDirty) {
        return (JmixAmchartsSceneState) super.getState(markAsDirty);
    }

    public AbstractChart getChart() {
        return chart;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setJson(String json) {
        if (!StringUtils.equals(getJson(), json)) {
            if (json != null) {
                try {
                    JsonParser parser = new JsonParser();
                    parser.parse(json);
                } catch (JsonSyntaxException e) {
                    throw new IllegalStateException("Unable to parse JSON chart configuration");
                }
            }

            getState().json = json;
            forceStateChange();
        }
    }

    public String getJson() {
        return getState(false).json;
    }

    public void drawChart() {
        forceStateChange();
    }

    public void drawChart(AbstractChart chart) {
        this.chart = chart;
        this.chart.addDataProviderChangeListener(() -> {
            forgetChangedItems();
            forceStateChange();
        });
        forceStateChange();
    }

    protected void forgetChangedItems() {
        this.changedItems = null;
    }

    protected void addChangedItems(IncrementalUpdateType type, List<DataItem> items) {
        if (changedItems == null) {
            changedItems = new ChartIncrementalChanges();
        }

        switch (type) {
            case ADD:
                changedItems.registerAddedItem(items);
                break;
            case REMOVE:
                changedItems.registerRemovedItems(items);
                break;
            case UPDATE:
                changedItems.registerUpdatedItems(items);
                break;
        }

        markAsDirty();
    }

    public void activateResponsivePlugin() {
        JmixAmchartsResponsiveIntegration.get();
    }

    public AngularGaugeChartModelImpl gaugeChart() {
        AngularGaugeChartModelImpl gaugeChart = new AngularGaugeChartModelImpl();
        drawChart(gaugeChart);
        return gaugeChart;
    }

    public FunnelChartModelImpl funnelChart() {
        FunnelChartModelImpl funnelChart = new FunnelChartModelImpl();
        drawChart(funnelChart);
        return funnelChart;
    }

    public PieChartModelImpl pieChart() {
        PieChartModelImpl pieChart = new PieChartModelImpl();
        drawChart(pieChart);
        return pieChart;
    }

    public RadarChartModelImpl radarChart() {
        RadarChartModelImpl radarChart = new RadarChartModelImpl();
        drawChart(radarChart);
        return radarChart;
    }

    public SerialChartModelImpl serialChart() {
        SerialChartModelImpl serialChart = new SerialChartModelImpl();
        drawChart(serialChart);
        return serialChart;
    }

    public XYChartModelImpl xyChart() {
        XYChartModelImpl xyChart = new XYChartModelImpl();
        drawChart(xyChart);
        return xyChart;
    }

    public GanttChartModelImpl ganttChart() {
        GanttChartModelImpl ganttChart = new GanttChartModelImpl();
        drawChart(ganttChart);
        return ganttChart;
    }

    public void addChartClickListener(ChartClickListener listener) {
        addListener(JmixAmchartsSceneState.CHART_CLICK_EVENT, ChartClickEvent.class, listener, chartClickMethod);
    }

    public void removeChartClickListener(ChartClickListener listener) {
        removeListener(JmixAmchartsSceneState.CHART_CLICK_EVENT, ChartClickEvent.class, listener);
    }

    public void addChartRightClickListener(ChartRightClickListener listener) {
        addListener(JmixAmchartsSceneState.CHART_RIGHT_CLICK_EVENT, ChartRightClickEvent.class, listener, chartRightClickMethod);
    }

    public void removeChartRightClickListener(ChartRightClickListener listener) {
        removeListener(JmixAmchartsSceneState.CHART_RIGHT_CLICK_EVENT, ChartRightClickEvent.class, listener);
    }

    public void addGraphClickListener(GraphClickListener listener) {
        addListener(JmixAmchartsSceneState.GRAPH_CLICK_EVENT, GraphClickEvent.class, listener, graphClickMethod);
    }

    public void removeGraphClickListener(GraphClickListener listener) {
        removeListener(JmixAmchartsSceneState.GRAPH_CLICK_EVENT, GraphClickEvent.class, listener);
    }

    public void addGraphItemClickListener(GraphItemClickListener listener) {
        addListener(JmixAmchartsSceneState.GRAPH_ITEM_CLICK_EVENT, GraphItemClickEvent.class, listener, graphItemClickMethod);
    }

    public void removeGraphItemClickListener(GraphItemClickListener listener) {
        removeListener(JmixAmchartsSceneState.GRAPH_ITEM_CLICK_EVENT, GraphItemClickEvent.class, listener);
    }

    public void addGraphItemRightClickListener(GraphItemRightClickListener listener) {
        addListener(JmixAmchartsSceneState.GRAPH_ITEM_RIGHT_CLICK_EVENT, GraphItemRightClickEvent.class, listener, graphItemRightClickMethod);
    }

    public void removeGraphItemRightClickListener(GraphItemRightClickListener listener) {
        removeListener(JmixAmchartsSceneState.GRAPH_ITEM_RIGHT_CLICK_EVENT, GraphItemRightClickEvent.class, listener);
    }

    public void addZoomListener(ZoomListener listener) {
        addListener(JmixAmchartsSceneState.ZOOM_EVENT, ZoomEvent.class, listener, zoomMethod);
    }

    public void removeZoomListener(ZoomListener listener) {
        removeListener(JmixAmchartsSceneState.ZOOM_EVENT, ZoomEvent.class, listener);
    }

    public void addSliceClickListener(SliceClickListener listener) {
        addListener(JmixAmchartsSceneState.SLICE_CLICK_EVENT, SliceClickEvent.class, listener, sliceClickMethod);
    }

    public void removeSliceClickListener(SliceClickListener listener) {
        removeListener(JmixAmchartsSceneState.SLICE_CLICK_EVENT, SliceClickEvent.class, listener);
    }

    public void addSliceRightClickListener(SliceRightClickListener listener) {
        addListener(JmixAmchartsSceneState.SLICE_RIGHT_CLICK_EVENT, SliceRightClickEvent.class, listener, sliceRightClickMethod);
    }

    public void removeSliceRightClickListener(SliceRightClickListener listener) {
        removeListener(JmixAmchartsSceneState.SLICE_RIGHT_CLICK_EVENT, SliceRightClickEvent.class, listener);
    }

    public void addSlicePullInListener(SlicePullInListener listener) {
        addListener(JmixAmchartsSceneState.SLICE_PULL_IN_EVENT, SlicePullInEvent.class, listener, slicePullInMethod);
    }

    public void removeSlicePullInListener(SlicePullInListener listener) {
        removeListener(JmixAmchartsSceneState.SLICE_PULL_IN_EVENT, SlicePullInEvent.class, listener);
    }

    public void addSlicePullOutListener(SlicePullOutListener listener) {
        addListener(JmixAmchartsSceneState.SLICE_PULL_OUT_EVENT, SlicePullOutEvent.class, listener, slicePullOutMethod);
    }

    public void removeSlicePullOutListener(SlicePullOutListener listener) {
        removeListener(JmixAmchartsSceneState.SLICE_PULL_OUT_EVENT, SlicePullOutEvent.class, listener);
    }

    public void addLegendLabelClickListener(LegendLabelClickListener listener) {
        addListener(JmixAmchartsSceneState.LEGEND_LABEL_CLICK_EVENT, LegendLabelClickEvent.class, listener, legendLabelClickMethod);
    }

    public void removeLegendLabelClickListener(LegendLabelClickListener listener) {
        removeListener(JmixAmchartsSceneState.LEGEND_LABEL_CLICK_EVENT, LegendLabelClickEvent.class, listener);
    }

    public void addLegendMarkerClickListener(LegendMarkerClickListener listener) {
        addListener(JmixAmchartsSceneState.LEGEND_MARKER_CLICK_EVENT, LegendMarkerClickEvent.class, listener, legendMarkerClickMethod);
    }

    public void removeLegendMarkerClickListener(LegendMarkerClickListener listener) {
        removeListener(JmixAmchartsSceneState.LEGEND_MARKER_CLICK_EVENT, LegendMarkerClickEvent.class, listener);
    }

    public void addLegendItemShowListener(LegendItemShowListener listener) {
        addListener(JmixAmchartsSceneState.LEGEND_ITEM_SHOW_EVENT, LegendItemShowEvent.class, listener, legendItemShowMethod);
    }

    public void removeLegendItemShowListener(LegendItemShowListener listener) {
        removeListener(JmixAmchartsSceneState.LEGEND_ITEM_SHOW_EVENT, LegendItemShowEvent.class, listener);
    }

    public void addLegendItemHideListener(LegendItemHideListener listener) {
        addListener(JmixAmchartsSceneState.LEGEND_ITEM_HIDE_EVENT, LegendItemHideEvent.class, listener, legendItemHideMethod);
    }

    public void removeLegendItemHideListener(LegendItemHideListener listener) {
        removeListener(JmixAmchartsSceneState.LEGEND_ITEM_HIDE_EVENT, LegendItemHideEvent.class, listener);
    }

    public void addCursorZoomListener(CursorZoomListener listener) {
        addListener(JmixAmchartsSceneState.CURSOR_ZOOM_EVENT, CursorZoomEvent.class, listener, cursorZoomMethod);
    }

    public void removeCursorZoomListener(CursorZoomListener listener) {
        removeListener(JmixAmchartsSceneState.CURSOR_ZOOM_EVENT, CursorZoomEvent.class, listener);
    }

    public void addCursorPeriodSelectListener(CursorPeriodSelectListener listener) {
        addListener(JmixAmchartsSceneState.CURSOR_PERIOD_SELECT_EVENT, CursorPeriodSelectEvent.class, listener, cursorPeriodSelectMethod);
    }

    public void removeCursorPeriodSelectListener(CursorPeriodSelectListener listener) {
        removeListener(JmixAmchartsSceneState.CURSOR_PERIOD_SELECT_EVENT, CursorPeriodSelectEvent.class, listener);
    }

    public void addAxisZoomListener(AxisZoomListener listener) {
        addListener(JmixAmchartsSceneState.VALUE_AXIS_ZOOM_EVENT, AxisZoomEvent.class, listener, axisZoomMethod);
    }

    public void removeAxisZoomListener(AxisZoomListener listener) {
        removeListener(JmixAmchartsSceneState.VALUE_AXIS_ZOOM_EVENT, AxisZoomEvent.class, listener);
    }

    public void addCategoryItemClickListener(CategoryItemClickListener listener) {
        addListener(JmixAmchartsSceneState.CATEGORY_ITEM_CLICK_EVENT, CategoryItemClickEvent.class, listener, categoryItemClickMethod);
    }

    public void removeCategoryItemClickListener(CategoryItemClickListener listener) {
        removeListener(JmixAmchartsSceneState.CATEGORY_ITEM_CLICK_EVENT, CategoryItemClickEvent.class, listener);
    }

    public void addRollOutGraphListener(RollOutGraphListener listener) {
        addListener(JmixAmchartsSceneState.ROLL_OUT_GRAPH_EVENT, RollOutGraphEvent.class, listener, rollOutGraphMethod);
    }

    public void addRollOutGraphItemListener(RollOutGraphItemListener listener) {
        addListener(JmixAmchartsSceneState.ROLL_OUT_GRAPH_ITEM_EVENT, RollOutGraphItemEvent.class, listener, rollOutGraphItemMethod);
    }

    public void addRollOverGraphListener(RollOverGraphListener listener) {
        addListener(JmixAmchartsSceneState.ROLL_OVER_GRAPH_EVENT, RollOverGraphEvent.class, listener, rollOverGraphMethod);
    }

    public void addRollOverGraphItemListener(RollOverGraphItemListener listener) {
        addListener(JmixAmchartsSceneState.ROLL_OVER_GRAPH_ITEM_EVENT, RollOverGraphItemEvent.class, listener, rollOverGraphItemMethod);
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        if (initial || dirty) {
            if (chart != null) {
                // Full repaint
                setupDefaults(chart);
                setupPaths(chart);

                dataItemKeys.removeAll();

                if (chart.getDataProvider() != null) {
                    chart.getDataProvider().addChangeListener(changeListener);
                }

                String jsonString = chartSerializer.serialize(chart);
                log.trace("Chart full JSON:\n{}", jsonString);

                getRpcProxy(JmixAmchartsSceneClientRpc.class).draw(jsonString);
            }
            dirty = false;
        } else if (changedItems != null && !changedItems.isEmpty()) {
            // Incremental update

            String jsonString = chartSerializer.serializeChanges(chart, changedItems);
            log.trace("Chart update JSON:\n{}", jsonString);

            List<DataItem> removedItems = changedItems.getRemovedItems();
            if (removedItems != null) {
                for (DataItem removedItem : removedItems) {
                    dataItemKeys.remove(removedItem);
                }
            }

            getRpcProxy(JmixAmchartsSceneClientRpc.class).updatePoints(jsonString);
        }

        forgetChangedItems();
    }

    protected void setupDefaults(AbstractChart chart) {
    }

    protected void setupPaths(AbstractChart chart) {
        if (chart.getPath() != null && !chart.getPath().isEmpty()) {
            return;
        }

        if (chart.getPath() == null || chart.getPath().isEmpty()) {
            EnhancedUI ui = ((EnhancedUI) getUI());
            String amchartsPath = ui.getWebJarPath("amcharts", "amcharts.js");
            String path = ui.translateToWebPath(amchartsPath.substring(0, amchartsPath.lastIndexOf("/"))) + "/";
            chart.setPath(path);
        }
    }

    protected void forceStateChange() {
        this.dirty = true;
        markAsDirty();
    }

    public void zoomOut() {
        getRpcProxy(JmixAmchartsSceneClientRpc.class).zoomOut();
    }

    public void zoomToIndexes(int start, int end) {
        getRpcProxy(JmixAmchartsSceneClientRpc.class).zoomToIndexes(start, end);
    }

    public void zoomToDates(Date start, Date end) {
        getRpcProxy(JmixAmchartsSceneClientRpc.class).zoomToDates(start, end);
    }

    public void zoomOutValueAxes() {
        getRpcProxy(JmixAmchartsSceneClientRpc.class).zoomOutValueAxes();
    }

    public void zoomOutValueAxis(String id) {
        getRpcProxy(JmixAmchartsSceneClientRpc.class).zoomOutValueAxisById(id);
    }

    public void zoomOutValueAxis(int index) {
        getRpcProxy(JmixAmchartsSceneClientRpc.class).zoomOutValueAxisByIndex(index);
    }

    protected String convertObjectToString(Object value) {
        return chartSerializer.toJson(value);
    }

    public void zoomValueAxisToValues(String id, Object startValue, Object endValue) {
        if (startValue == null || endValue == null) {
            throw new IllegalArgumentException("startValue or endValue cannot be null");
        }

        JmixAmchartsSceneClientRpc rpc = getRpcProxy(JmixAmchartsSceneClientRpc.class);
        if (startValue instanceof Date) {
            rpc.zoomValueAxisToDatesById(id, (Date) startValue, (Date) endValue);
        } else {
            rpc.zoomValueAxisToValuesById(id, convertObjectToString(startValue), convertObjectToString(endValue));
        }
    }

    public void zoomValueAxisToValues(int index, Object startValue, Object endValue) {
        if (startValue == null || endValue == null) {
            throw new IllegalArgumentException("startValue or endValue cannot be null");
        }

        JmixAmchartsSceneClientRpc rpc = getRpcProxy(JmixAmchartsSceneClientRpc.class);
        if (startValue instanceof Date) {
            rpc.zoomValueAxisToDatesByIndex(index, (Date) startValue, (Date) endValue);
        } else {
            rpc.zoomValueAxisToValuesByIndex(index, convertObjectToString(startValue), convertObjectToString(endValue));
        }
    }

    protected class JmixAmchartsServerRpcImpl implements JmixAmchartsServerRpc {

        @Override
        public void onChartClick(int x, int y, int absoluteX, int absoluteY, double xAxis, double yAxis) {
            fireEvent(new ChartClickEvent(JmixAmchartsScene.this, x, y, absoluteX, absoluteY, xAxis, yAxis));
        }

        @Override
        public void onChartRightClick(int x, int y, int absoluteX, int absoluteY, double xAxis, double yAxis) {
            fireEvent(new ChartRightClickEvent(JmixAmchartsScene.this, x, y, absoluteX, absoluteY, xAxis, yAxis));
        }

        @Override
        public void onGraphClick(String graphId, int x, int y, int absoluteX, int absoluteY) {
            fireEvent(new GraphClickEvent(JmixAmchartsScene.this, graphId, x, y, absoluteX, absoluteY));
        }

        @Override
        public void onGraphItemClick(String graphId, int itemIndex, String itemKey, int x, int y, int absoluteX, int absoluteY) {
            DataItem dataItem = getDataItemByKey(itemKey);
            fireEvent(new GraphItemClickEvent(JmixAmchartsScene.this,
                    graphId, itemIndex, dataItem, x, y, absoluteX, absoluteY));
        }

        @Override
        public void onGraphItemRightClick(String graphId, int itemIndex, String itemKey, int x, int y, int absoluteX, int absoluteY) {
            DataItem dataItem = getDataItemByKey(itemKey);
            fireEvent(new GraphItemRightClickEvent(JmixAmchartsScene.this,
                    graphId, itemIndex, dataItem, x, y, absoluteX, absoluteY));
        }

        @Override
        public void onZoom(int startIndex, int endIndex, Date startDate, Date endDate, String startValue, String endValue) {
            fireEvent(new ZoomEvent(JmixAmchartsScene.this, startIndex, endIndex, startDate, endDate, startValue, endValue));
        }

        @Override
        public void onSliceClick(int itemIndex, String dataItemKey, int x, int y, int absoluteX, int absoluteY) {
            DataItem dataItem = getDataItemByKey(dataItemKey);
            fireEvent(new SliceClickEvent(JmixAmchartsScene.this, dataItem, x, y, absoluteX, absoluteY));
        }

        @Override
        public void onSliceRightClick(int itemIndex, String dataItemKey, int x, int y, int absoluteX, int absoluteY) {
            DataItem dataItem = getDataItemByKey(dataItemKey);
            fireEvent(new SliceRightClickEvent(JmixAmchartsScene.this, dataItem, x, y, absoluteX, absoluteY));
        }

        @Override
        public void onSlicePullIn(String dataItemKey) {
            DataItem dataItem = getDataItemByKey(dataItemKey);
            fireEvent(new SlicePullInEvent(JmixAmchartsScene.this, dataItem));
        }

        @Override
        public void onSlicePullOut(String dataItemKey) {
            DataItem dataItem = getDataItemByKey(dataItemKey);
            fireEvent(new SlicePullOutEvent(JmixAmchartsScene.this, dataItem));
        }

        @Override
        public void onLegendLabelClick(int legendItemIndex, @Nullable String dataItemKey) {
            DataItem dataItem = getDataItemByKey(dataItemKey);
            fireEvent(new LegendLabelClickEvent(JmixAmchartsScene.this, legendItemIndex, dataItem));
        }

        @Override
        public void onLegendMarkerClick(int legendItemIndex, @Nullable String dataItemKey) {
            DataItem dataItem = getDataItemByKey(dataItemKey);
            fireEvent(new LegendMarkerClickEvent(JmixAmchartsScene.this, legendItemIndex, dataItem));
        }

        @Override
        public void onLegendItemHide(int legendItemIndex, @Nullable String dataItemKey) {
            DataItem dataItem = getDataItemByKey(dataItemKey);
            fireEvent(new LegendItemHideEvent(JmixAmchartsScene.this, legendItemIndex, dataItem));
        }

        @Override
        public void onLegendItemShow(int legendItemIndex, @Nullable String dataItemKey) {
            DataItem dataItem = getDataItemByKey(dataItemKey);
            fireEvent(new LegendItemShowEvent(JmixAmchartsScene.this, legendItemIndex, dataItem));
        }

        protected DataItem getDataItemByKey(@Nullable String dataItemKey) {
            DataItem dataItem = null;
            if (dataItemKey != null) {
                DataProvider dataProvider = chart.getDataProvider();

                if (chart.getType() == ChartType.GANTT && dataItemKey.contains(":")) {
                    String graphItemKey = dataItemKey.substring(0, dataItemKey.indexOf(":"));
                    String segmentItemKey = dataItemKey.substring(dataItemKey.indexOf(":") + 1, dataItemKey.length());

                    Object dataItemId = dataItemKeys.get(graphItemKey);

                    if (dataProvider != null) {
                        DataItem graphDataItem = dataProvider.getItem(dataItemId);
                        if (graphDataItem != null) {
                            int segmentIndex = Integer.parseInt(segmentItemKey);
                            String segmentsField = ((GanttChartModelImpl) chart).getSegmentsField();

                            @SuppressWarnings("unchecked")
                            List<DataItem> segmentItems = (List<DataItem>) graphDataItem.getValue(segmentsField);
                            dataItem = segmentItems.get(segmentIndex);
                        }
                    }
                } else {
                    Object dataItemId = dataItemKeys.get(dataItemKey);

                    if (dataProvider != null) {
                        dataItem = dataProvider.getItem(dataItemId);
                    }
                }
            }
            return dataItem;
        }

        @Override
        public void onCursorZoom(String start, String end) {
            fireEvent(new CursorZoomEvent(JmixAmchartsScene.this, start, end));
        }

        @Override
        public void onCursorPeriodSelect(String start, String end) {
            fireEvent(new CursorPeriodSelectEvent(JmixAmchartsScene.this, start, end));
        }

        @Override
        public void onValueAxisZoom(String axisId, double startValue, double endValue) {
            fireEvent(new AxisZoomEvent(JmixAmchartsScene.this, axisId, startValue, endValue));
        }

        @Override
        public void onCategoryItemClick(String value, int x, int y, int offsetX, int offsetY, int xAxis, int yAxis) {
            fireEvent(new CategoryItemClickEvent(JmixAmchartsScene.this, value, x, y, offsetX, offsetY, xAxis, yAxis));
        }

        @Override
        public void onRollOutGraph(String graphId) {
            fireEvent(new RollOutGraphEvent(JmixAmchartsScene.this, graphId));
        }

        @Override
        public void onRollOutGraphItem(String graphId, int itemIndex, String itemKey) {
            DataItem dataItem = getDataItemByKey(itemKey);
            fireEvent(new RollOutGraphItemEvent(JmixAmchartsScene.this, graphId, itemIndex, dataItem));
        }

        @Override
        public void onRollOverGraph(String graphId) {
            fireEvent(new RollOverGraphEvent(JmixAmchartsScene.this, graphId));
        }

        @Override
        public void onRollOverGraphItem(String graphId, int itemIndex, String itemKey) {
            DataItem dataItem = getDataItemByKey(itemKey);
            fireEvent(new RollOverGraphItemEvent(JmixAmchartsScene.this, graphId, itemIndex, dataItem));
        }
    }

    protected static class ProxyChangeForwarder implements DataChangeListener {
        protected final JmixAmchartsScene chart;

        public ProxyChangeForwarder(JmixAmchartsScene chart) {
            this.chart = chart;
        }

        @Override
        public void dataItemsChanged(DataItemsChangeEvent e) {
            if (chart.isDirty()) {
                // full repaint required, don't need to send incremental updates
                return;
            }

            IncrementalUpdateType updateType = null;
            switch (e.getOperation()) {
                case ADD:
                    updateType = IncrementalUpdateType.ADD;
                    break;
                case REMOVE:
                    updateType = IncrementalUpdateType.REMOVE;
                    break;
                case UPDATE:
                    updateType = IncrementalUpdateType.UPDATE;
                    break;
                case REFRESH:
                    chart.getChart().getDataProvider().removeChangeListener(this);
                    chart.forgetChangedItems();
                    chart.drawChart();
                    break;
            }
            if (updateType != null && CollectionUtils.isNotEmpty(e.getItems())) {
                chart.addChangedItems(updateType, e.getItems());
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            ProxyChangeForwarder that = (ProxyChangeForwarder) obj;

            return this.chart.equals(that.chart);
        }

        @Override
        public int hashCode() {
            return chart.hashCode();
        }
    }
}