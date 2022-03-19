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

package io.jmix.charts.widget.client.amcharts.connector;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.json.client.JSONParser;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;
import io.jmix.ui.widget.client.JsDate;
import io.jmix.charts.widget.amcharts.JmixAmchartsScene;
import io.jmix.charts.widget.client.amcharts.*;
import io.jmix.charts.widget.client.amcharts.rpc.JmixAmchartsSceneClientRpc;
import io.jmix.charts.widget.client.amcharts.rpc.JmixAmchartsServerRpc;
import io.jmix.charts.widget.client.amcharts.state.JmixAmchartsSceneState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Connect(JmixAmchartsScene.class)
public class JmixAmchartsSceneConnector extends AbstractComponentConnector {

    protected JmixAmchartsServerRpc rpc = RpcProxy.create(JmixAmchartsServerRpc.class, this);
    protected ElementResizeListener resizeListener;

    protected boolean dataReady = false;
    protected List<Runnable> afterDataReady = new ArrayList<>();

    public JmixAmchartsSceneConnector() {
        registerRpc(JmixAmchartsSceneClientRpc.class, new JmixAmchartsSceneClientRpc() {
            @Override
            public void draw(String chartJson) {
                drawChart(chartJson);
            }

            @Override
            public void updatePoints(String json) {
                updateChart(json);
            }

            @Override
            public void zoomOut() {
                Scheduler.get().scheduleDeferred(() -> {
                    if (dataReady) {
                        getWidget().zoomOut();
                    } else {
                        afterDataReady.add(() -> getWidget().zoomOut());
                    }
                });
            }

            @Override
            public void zoomToIndexes(int start, int end) {
                Scheduler.get().scheduleDeferred(() -> {
                    if (dataReady) {
                        getWidget().zoomToIndexes(start, end);
                    } else {
                        afterDataReady.add(() -> getWidget().zoomToIndexes(start, end));
                    }
                });
            }

            @Override
            public void zoomToDates(Date start, Date end) {
                Scheduler.get().scheduleDeferred(() -> {
                    if (dataReady) {
                        getWidget().zoomToDates(JsDate.toJs(start), JsDate.toJs(end));
                    } else {
                        afterDataReady.add(() -> getWidget().zoomToDates(JsDate.toJs(start), JsDate.toJs(end)));
                    }
                });
            }

            @Override
            public void zoomOutValueAxes() {
                Scheduler.get().scheduleDeferred(() -> {
                    if (dataReady) {
                        getWidget().zoomOutValueAxes();
                    } else {
                        afterDataReady.add(() -> getWidget().zoomOutValueAxes());
                    }
                });
            }

            @Override
            public void zoomOutValueAxisById(String id) {
                Scheduler.get().scheduleDeferred(() -> {
                    if (dataReady) {
                        getWidget().zoomOutValueAxis(id);
                    } else {
                        afterDataReady.add(() -> getWidget().zoomOutValueAxis(id));
                    }
                });
            }

            @Override
            public void zoomOutValueAxisByIndex(int index) {
                Scheduler.get().scheduleDeferred(() -> {
                    if (dataReady) {
                        getWidget().zoomOutValueAxis(index);
                    } else {
                        afterDataReady.add(() -> getWidget().zoomOutValueAxis(index));
                    }
                });
            }

            @Override
            public void zoomValueAxisToValuesById(String id, String startValue, String endValue) {
                Scheduler.get().scheduleDeferred(() -> {
                    if (dataReady) {
                        getWidget().zoomValueAxisToValues(id, startValue, endValue);
                    } else {
                        afterDataReady.add(() -> getWidget().zoomValueAxisToValues(id, startValue, endValue));
                    }
                });
            }

            @Override
            public void zoomValueAxisToValuesByIndex(int index, String startValue, String endValue) {
                Scheduler.get().scheduleDeferred(() -> {
                    if (dataReady) {
                        getWidget().zoomValueAxisToValues(index, startValue, endValue);
                    } else {
                        afterDataReady.add(() -> getWidget().zoomValueAxisToValues(index, startValue, endValue));
                    }
                });
            }

            @Override
            public void zoomValueAxisToDatesById(String id, Date start, Date end) {
                Scheduler.get().scheduleDeferred(() -> {
                    if (dataReady) {
                        getWidget().zoomValueAxisToValues(id, JsDate.toJs(start), JsDate.toJs(end));
                    } else {
                        afterDataReady.add(() ->
                                getWidget().zoomValueAxisToValues(id, JsDate.toJs(start), JsDate.toJs(end)));
                    }
                });
            }

            @Override
            public void zoomValueAxisToDatesByIndex(int index, Date start, Date end) {
                Scheduler.get().scheduleDeferred(() -> {
                    if (dataReady) {
                        getWidget().zoomValueAxisToValues(index, JsDate.toJs(start), JsDate.toJs(end));
                    } else {
                        afterDataReady.add(() ->
                                getWidget().zoomValueAxisToValues(index, JsDate.toJs(start), JsDate.toJs(end)));
                    }
                });
            }
        });
    }

    protected void updateChart(String json) {
        dataReady = false;

        Scheduler.get().scheduleDeferred(() -> {
            getWidget().updatePoints(getJsonAsObject(json));
            dataReady = true;
            executeAfterDataReady();
        });
    }

    protected void executeAfterDataReady() {
        for (Runnable runnable : afterDataReady) {
            runnable.run();
        }
        afterDataReady.clear();
    }

    protected void drawChart(String chartJson) {
        dataReady = false;

        AmchartsConfig config = AmchartsConfig.fromServerConfig(chartJson, getState().json);
        AmchartsEvents amchartsEvents = createEvents(config);

        Scheduler.get().scheduleDeferred(() -> {
            getWidget().init(config, amchartsEvents);

            // Add resize listener lazily here.
            // If done in init like in examples it will be called way too early,
            // like before the widget is not even rendered yet
            if (resizeListener == null) {
                resizeListener = e -> getWidget().updateSize();

                getLayoutManager().addElementResizeListener(getWidget().getElement(), resizeListener);
            }

            dataReady = true;
            executeAfterDataReady();
        });
    }

    protected JavaScriptObject getJsonAsObject(String json) {
        return JSONParser.parseLenient(json).isObject().getJavaScriptObject();
    }

    @Override
    public JmixAmchartsSceneState getState() {
        return (JmixAmchartsSceneState) super.getState();
    }

    @Override
    public JmixAmchartsSceneWidget getWidget() {
        return (JmixAmchartsSceneWidget) super.getWidget();
    }

    protected AmchartsEvents createEvents(AmchartsConfig config) {
        AmchartsEvents amchartsEvents = new AmchartsEvents();
        Set<String> events = getState().registeredEventListeners;
        if (events != null) {
            bindClickEvents(amchartsEvents, events);

            String chartType = config.getChartType();
            if ("xy".equals(chartType)
                    || "radar".equals(chartType)
                    || "serial".equals(chartType)
                    || "gantt".equals(chartType)) {
                bindCoordinateChartEvents(amchartsEvents, events);
            }
            if ("serial".equals(chartType) || "gantt".equals(chartType)) {
                bindSerialChartEvents(amchartsEvents, events);
            }
            if ("pie".equals(chartType) || "funnel".equals(chartType)) {
                bindSlicedChartEvents(amchartsEvents, events);
            }
            if (config.hasLegend()) {
                bindLegendEvents(amchartsEvents, events);
            }
            if (("xy".equals(chartType)
                    || "serial".equals(chartType)
                    || "gantt".equals(chartType)) && config.hasCursor()) {
                bindCursorEvents(amchartsEvents, events);
            }
            if ("xy".equals(chartType)) {
                bindXYChartEvents(amchartsEvents, events);
            }
        }
        return amchartsEvents;
    }

    protected void bindClickEvents(AmchartsEvents amchartsEvents, Set<String> events) {
        if (events.contains(JmixAmchartsSceneState.CHART_CLICK_EVENT)) {
            amchartsEvents.setChartClickHandler(event ->
                    rpc.onChartClick(event.getX(), event.getY(),
                            event.getAbsoluteX(), event.getAbsoluteY(), event.getXAxis(), event.getYAxis())
            );
        }
        if (events.contains(JmixAmchartsSceneState.CHART_RIGHT_CLICK_EVENT)) {
            amchartsEvents.setChartRightClickHandler(event ->
                    rpc.onChartRightClick(event.getX(), event.getY(),
                            event.getAbsoluteX(), event.getAbsoluteY(), event.getXAxis(), event.getYAxis())
            );
        }
    }

    protected void bindXYChartEvents(AmchartsEvents amchartsEvents, Set<String> events) {
        if (events.contains(JmixAmchartsSceneState.VALUE_AXIS_ZOOM_EVENT)) {
            amchartsEvents.setAxisZoomHandler(event ->
                    rpc.onValueAxisZoom(event.getAxisId(), event.getStartValue(), event.getEndValue())
            );
        }
    }

    protected void bindCursorEvents(AmchartsEvents amchartsEvents, Set<String> events) {
        if (events.contains(JmixAmchartsSceneState.CURSOR_ZOOM_EVENT)) {
            amchartsEvents.setCursorZoomHandler(event ->
                    rpc.onCursorZoom(event.getStart(), event.getEnd())
            );
        }
        if (events.contains(JmixAmchartsSceneState.CURSOR_PERIOD_SELECT_EVENT)) {
            amchartsEvents.setCursorPeriodSelectHandler(event ->
                    rpc.onCursorPeriodSelect(event.getStart(), event.getEnd())
            );
        }
    }

    protected void bindLegendEvents(AmchartsEvents amchartsEvents, Set<String> events) {
        if (events.contains(JmixAmchartsSceneState.LEGEND_LABEL_CLICK_EVENT)) {
            amchartsEvents.setLegendLabelClickHandler(event ->
                    rpc.onLegendLabelClick(event.getItemIndex(), event.getItemKey())
            );
        }
        if (events.contains(JmixAmchartsSceneState.LEGEND_MARKER_CLICK_EVENT)) {
            amchartsEvents.setLegendMarkerClickHandler(event ->
                    rpc.onLegendMarkerClick(event.getItemIndex(), event.getItemKey())
            );
        }
        if (events.contains(JmixAmchartsSceneState.LEGEND_ITEM_SHOW_EVENT)) {
            amchartsEvents.setLegendItemShowHandler(event ->
                    rpc.onLegendItemShow(event.getItemIndex(), event.getItemKey())
            );
        }
        if (events.contains(JmixAmchartsSceneState.LEGEND_ITEM_HIDE_EVENT)) {
            amchartsEvents.setLegendItemHideHandler(event ->
                    rpc.onLegendItemHide(event.getItemIndex(), event.getItemKey())
            );
        }
    }

    protected void bindSlicedChartEvents(AmchartsEvents amchartsEvents, Set<String> events) {
        if (events.contains(JmixAmchartsSceneState.SLICE_CLICK_EVENT)) {
            amchartsEvents.setSliceClickHandler(event -> {
                NativeEvent me = event.getMouseEvent();

                rpc.onSliceClick(event.getItemIndex(), event.getItemKey(), MouseHelper.getX(me), MouseHelper.getY(me),
                        me.getClientX(), me.getClientY());
            });
        }
        if (events.contains(JmixAmchartsSceneState.SLICE_RIGHT_CLICK_EVENT)) {
            amchartsEvents.setSliceRightClickHandler(event -> {
                NativeEvent me = event.getMouseEvent();

                rpc.onSliceRightClick(event.getItemIndex(), event.getItemKey(), MouseHelper.getX(me), MouseHelper.getY(me),
                        me.getClientX(), me.getClientY());
            });
        }
        if (events.contains(JmixAmchartsSceneState.SLICE_PULL_IN_EVENT)) {
            amchartsEvents.setSlicePullInHandler(event -> rpc.onSlicePullIn(event.getItemKey()));
        }
        if (events.contains(JmixAmchartsSceneState.SLICE_PULL_OUT_EVENT)) {
            amchartsEvents.setSlicePullOutHandler(event -> rpc.onSlicePullOut(event.getItemKey()));
        }
    }

    protected void bindSerialChartEvents(AmchartsEvents amchartsEvents, Set<String> events) {
        if (events.contains(JmixAmchartsSceneState.ZOOM_EVENT)) {
            amchartsEvents.setZoomHandler(event ->
                    rpc.onZoom(event.getStartIndex(), event.getEndIndex(),
                            JsDate.toJava(event.getStartDate()), JsDate.toJava(event.getEndDate()),
                            event.getStartValue(), event.getEndValue())
            );
        }
        if (events.contains(JmixAmchartsSceneState.CATEGORY_ITEM_CLICK_EVENT)) {
            amchartsEvents.setCategoryItemClickHandler(event ->
                    rpc.onCategoryItemClick(event.getValue(), event.getX(), event.getY(),
                            event.getOffsetX(), event.getOffsetY(), event.getXAxis(), event.getYAxis())
            );
        }
    }

    protected void bindCoordinateChartEvents(AmchartsEvents amchartsEvents, Set<String> events) {
        if (events.contains(JmixAmchartsSceneState.GRAPH_CLICK_EVENT)) {
            amchartsEvents.setGraphClickHandler(event -> {
                NativeEvent me = event.getMouseEvent();

                rpc.onGraphClick(event.getGraphId(), MouseHelper.getX(me), MouseHelper.getY(me),
                        me.getClientX(), me.getClientY());
            });
        }
        if (events.contains(JmixAmchartsSceneState.GRAPH_ITEM_CLICK_EVENT)) {
            amchartsEvents.setGraphItemClickHandler(event -> {
                NativeEvent me = event.getMouseEvent();

                rpc.onGraphItemClick(event.getGraphId(), event.getIndex(), event.getItemKey(),
                        MouseHelper.getX(me), MouseHelper.getY(me),
                        me.getClientX(), me.getClientY());
            });
        }
        if (events.contains(JmixAmchartsSceneState.GRAPH_ITEM_RIGHT_CLICK_EVENT)) {
            amchartsEvents.setGraphItemRightClickHandler(event -> {
                NativeEvent me = event.getMouseEvent();

                rpc.onGraphItemRightClick(event.getGraphId(), event.getIndex(), event.getItemKey(),
                        MouseHelper.getX(me), MouseHelper.getY(me),
                        me.getClientX(), me.getClientY());
            });
        }
        if (events.contains(JmixAmchartsSceneState.ROLL_OUT_GRAPH_EVENT)) {
            amchartsEvents.setRollOutGraphHandler(event -> {
                rpc.onRollOutGraph(event.getGraphId());
            });
        }
        if (events.contains(JmixAmchartsSceneState.ROLL_OUT_GRAPH_ITEM_EVENT)) {
            amchartsEvents.setRollOutGraphItemHandler(event -> {
                rpc.onRollOutGraphItem(event.getGraphId(), event.getIndex(), event.getItemKey());
            });
        }
        if (events.contains(JmixAmchartsSceneState.ROLL_OVER_GRAPH_EVENT)) {
            amchartsEvents.setRollOverGraphHandler(event -> {
                rpc.onRollOverGraph(event.getGraphId());
            });
        }
        if (events.contains(JmixAmchartsSceneState.ROLL_OVER_GRAPH_ITEM_EVENT)) {
            amchartsEvents.setRollOverGraphItemHandler(event -> {
                rpc.onRollOverGraphItem(event.getGraphId(), event.getIndex(), event.getItemKey());
            });
        }
    }
}