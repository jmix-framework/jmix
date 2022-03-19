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

package io.jmix.charts.widget.client.amcharts;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import io.jmix.ui.widget.client.JsDate;
import io.jmix.charts.widget.client.amcharts.events.*;

import java.util.function.Consumer;

public class JmixAmchartsJsOverlay {

    protected static boolean ready = false;

    protected JavaScriptObject chart;

    public JmixAmchartsJsOverlay(JavaScriptObject chart) {
        this.chart = chart;
    }

    public static JmixAmchartsJsOverlay makeChart(Element placeHolder, JavaScriptObject json) {
        if (!ready) {
            handleLoad();
            ready = true;
        }

        return new JmixAmchartsJsOverlay(makeJsChart(placeHolder, json));
    }

    public void updatePoints(JavaScriptObject value) {
        updatePoints(chart, value);
    }

    protected native void updatePoints(JavaScriptObject chart, JavaScriptObject src) /*-{
        (function () {
            var srcAdd = src["add"];
            if (srcAdd) {
                for (var i = 0; i < srcAdd.length; i++) {
                    chart.dataProvider.push(srcAdd[i]);
                }
            }
        })();

        (function () {
            var srcRemove = src["remove"];
            if (srcRemove) {
                for (var i = 0; i < srcRemove.length; i++) {
                    for (var j = 0; j < chart.dataProvider.length; j++) {
                        if (chart.dataProvider[j].$k == srcRemove[i].$k) {
                            chart.dataProvider.splice(j, 1);
                            break;
                        }
                    }
                }
            }
        })();

        (function () {
            var srcUpdate = src["update"];
            if (srcUpdate) {
                for (var i = 0; i < srcUpdate.length; i++) {
                    for (var j = 0; j < chart.dataProvider.length; j++) {
                        if (chart.dataProvider[j].$k == srcUpdate[i].$k) {
                            chart.dataProvider[j] = srcUpdate[i];
                            break;
                        }
                    }
                }
            }
        })();

        chart.validateData();
    }-*/;

    protected static native void handleLoad() /*-{
        $wnd.AmCharts.handleLoad();
    }-*/;

    protected static native JavaScriptObject makeJsChart(Element placeHolder, JavaScriptObject json) /*-{
        var chart = $wnd.AmCharts.makeChart(placeHolder, json);
        // save last cursor position to chart.cursorPosition
        if (("xy" === chart.type || "serial" === chart.type) && chart.chartCursor) {
            chart.chartCursor.addListener("moved", function (event) {
                chart.cursorPosition = {"x": event.x, "y": event.y};
            });
        }

        return chart;
    }-*/;

    public void updateSize() {
        updateSize(chart);
    }

    protected static native void updateSize(JavaScriptObject chart) /*-{
        chart.invalidateSize();
    }-*/;

    public void destroy() {
        destroy(chart);
    }

    protected static native void destroy(JavaScriptObject chart) /*-{
        chart.clear();
    }-*/;

    public JsChartClickEvent getClickEvent(int x, int y, int absoluteX, int absoluteY) {
        return getClickEvent(chart, x, y, absoluteX, absoluteY);
    }

    protected static native JsChartClickEvent getClickEvent(JavaScriptObject chart, int x, int y, int absoluteX, int absoluteY) /*-{
        var event = {};
        event.x = x;
        event.y = y;
        event.absoluteX = absoluteX;
        event.absoluteY = absoluteY;
        event.xAxis = -1;
        event.yAxis = -1;

        if ("xy" === chart.type || "serial" === chart.type) {
            if (chart.valueAxes && chart.cursorPosition) {
                for (var i = 0; i < chart.valueAxes.length; i++) {
                    var axis = chart.valueAxes[i];
                    if ("left" === axis.position || "right" === axis.position) {
                        event.yAxis = $wnd.AmCharts.roundTo(axis.coordinateToValue(chart.cursorPosition.y - axis.axisY), 2);
                    } else if ("bottom" === axis.position || "top" === axis.position) {
                        event.xAxis = $wnd.AmCharts.roundTo(axis.coordinateToValue(chart.cursorPosition.x - axis.axisX), 2);
                    }
                }
            }
        }

        return event;
    }-*/;

    public void addGraphClickHandler(Consumer<JsGraphClickEvent> handler) {
        addGraphClickHandler(chart, handler);
    }

    protected static native void addGraphClickHandler(JavaScriptObject chart, Consumer<JsGraphClickEvent> handler) /*-{
        chart.addListener("clickGraph", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;

    public void addGraphItemClickHandler(Consumer<JsGraphItemClickEvent> handler) {
        addGraphItemClickHandler(chart, handler);
    }

    protected static native void addGraphItemClickHandler(JavaScriptObject chart, Consumer<JsGraphItemClickEvent> handler) /*-{
        chart.addListener("clickGraphItem", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;

    public void addGraphItemRightClickHandler(Consumer<JsGraphItemClickEvent> handler) {
        addGraphItemRightClickHandler(chart, handler);
    }

    protected static native void addGraphItemRightClickHandler(JavaScriptObject chart, Consumer<JsGraphItemClickEvent> handler) /*-{
        chart.addListener("rightClickGraphItem", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);

            event.event.preventDefault();
        }));
    }-*/;

    public void addZoomHandler(Consumer<JsZoomEvent> zoomHandler) {
        addZoomHandler(chart, zoomHandler);
    }

    protected static native void addZoomHandler(JavaScriptObject chart, Consumer<JsZoomEvent> handler) /*-{
        chart.addListener("zoomed", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;

    public void addSliceClickHandler(Consumer<JsSliceClickEvent> sliceClickHandler) {
        addSliceClickHandler(chart, sliceClickHandler);
    }

    protected static native void addSliceClickHandler(JavaScriptObject chart, Consumer<JsSliceClickEvent> handler) /*-{
        chart.addListener("clickSlice", $entry(function (event) {
            if (event.event.which === 1) {
                handler.@java.util.function.Consumer::accept(*)(event);
            }
        }));
    }-*/;

    public void addSliceRightClickHandler(Consumer<JsSliceClickEvent> sliceRightClickHandler) {
        addSliceRightClickHandler(chart, sliceRightClickHandler);
    }

    protected static native void addSliceRightClickHandler(JavaScriptObject chart, Consumer<JsSliceClickEvent> handler) /*-{
        chart.addListener("rightClickSlice", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);

            event.event.preventDefault();
        }));
    }-*/;

    public void addSlicePullInHandler(Consumer<JsSlicePullEvent> slicePullInHandler) {
        addSlicePullInHandler(chart, slicePullInHandler);
    }

    protected static native void addSlicePullInHandler(JavaScriptObject chart, Consumer<JsSlicePullEvent> handler) /*-{
        chart.addListener("pullInSlice", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;

    public void addSlicePullOutHandler(Consumer<JsSlicePullEvent> slicePullOutHandler) {
        addSlicePullOutHandler(chart, slicePullOutHandler);
    }

    protected static native void addSlicePullOutHandler(JavaScriptObject chart, Consumer<JsSlicePullEvent> handler)  /*-{
        chart.addListener("pullOutSlice", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;

    public void addLegendLabelClickHandler(Consumer<JsLegendEvent> legendLabelClickHandler) {
        addLegendLabelClickHandler(chart, legendLabelClickHandler);
    }

    protected native static void addLegendLabelClickHandler(JavaScriptObject chart, Consumer<JsLegendEvent> handler) /*-{
        if (chart.legend) {
            chart.legend.clickLabel = $entry(function (event) {
                handler.@java.util.function.Consumer::accept(*)(event);
            });
        }
    }-*/;

    public void addLegendMarkerClickHandler(Consumer<JsLegendEvent> legendMarkerClickHandler) {
        addLegendMarkerClickHandler(chart, legendMarkerClickHandler);
    }

    protected native static void addLegendMarkerClickHandler(JavaScriptObject chart, Consumer<JsLegendEvent> handler) /*-{
        if (chart.legend) {
            chart.legend.clickMarker = $entry(function (event) {
                handler.@java.util.function.Consumer::accept(*)(event);
            });
        }
    }-*/;

    public void addLegendItemShowHandler(Consumer<JsLegendEvent> legendItemShowHandler) {
        addLegendItemShowHandler(chart, legendItemShowHandler);
    }

    protected native static void addLegendItemShowHandler(JavaScriptObject chart, Consumer<JsLegendEvent> handler) /*-{
        if (chart.legend) {
            chart.legend.addListener("showItem", $entry(function (event) {
                handler.@java.util.function.Consumer::accept(*)(event);
            }));
        }
    }-*/;

    public void addLegendItemHideHandler(Consumer<JsLegendEvent> legendItemHideHandler) {
        addLegendItemHideHandler(chart, legendItemHideHandler);
    }

    protected native static void addLegendItemHideHandler(JavaScriptObject chart, Consumer<JsLegendEvent> handler) /*-{
        if (chart.legend) {
            chart.legend.addListener("hideItem", $entry(function (event) {
                handler.@java.util.function.Consumer::accept(*)(event);
            }));
        }
    }-*/;

    public void addCursorPeriodSelectHandler(Consumer<JsCursorEvent> cursorPeriodSelectHandler) {
        addCursorPeriodSelectHandler(chart, cursorPeriodSelectHandler);
    }

    protected native static void addCursorPeriodSelectHandler(JavaScriptObject chart, Consumer<JsCursorEvent> handler) /*-{
        if (chart.chartCursor) {
            chart.chartCursor.addListener("selected", $entry(function (event) {
                handler.@java.util.function.Consumer::accept(*)(event);
            }));
        }
    }-*/;

    public void addCursorZoomHandler(Consumer<JsCursorEvent> cursorZoomHandler) {
        addCursorZoomHandler(chart, cursorZoomHandler);
    }

    protected native static void addCursorZoomHandler(JavaScriptObject chart, Consumer<JsCursorEvent> handler) /*-{
        if (chart.chartCursor) {
            chart.chartCursor.addListener("zoomed", $entry(function (event) {
                handler.@java.util.function.Consumer::accept(*)(event);
            }));
        }
    }-*/;

    public void addAxisZoomHandler(Consumer<JsAxisZoomedEvent> axisZoomHandler) {
        addAxisZoomHandler(chart, axisZoomHandler);
    }

    protected native static void addAxisZoomHandler(JavaScriptObject chart, Consumer<JsAxisZoomedEvent> handler) /*-{
        if (chart.valueAxes) {
            for (var i = 0; i < chart.valueAxes.length; i++) {
                var axis = chart.valueAxes[i];
                (function () {
                    // store axisId in function scope, prevent reference on mutable variable
                    var axisId = axis.id;
                    axis.addListener("axisZoomed", $entry(function (event) {
                        var axisEvent = {};
                        axisEvent.startValue = event.startValue;
                        axisEvent.endValue = event.endValue;
                        axisEvent.axisId = axisId;

                        handler.@java.util.function.Consumer::accept(*)(axisEvent);
                    }));
                })();
            }
        }
    }-*/;

    public void zoomOut() {
        zoomOut(chart);
    }

    protected native static void zoomOut(JavaScriptObject object) /*-{
        object.zoomOut();
    }-*/;

    public void zoomToIndexes(int start, int end) {
        zoomToIndexes(chart, start, end);
    }

    protected native static void zoomToIndexes(JavaScriptObject chart, int start, int end) /*-{
        chart.zoomToIndexes(start, end);
    }-*/;

    public void zoomToDates(JsDate start, JsDate end) {
        zoomToDates(chart, start, end);
    }

    protected native static void zoomToDates(JavaScriptObject chart, JsDate start, JsDate end) /*-{
        chart.zoomToDates(start, end);
    }-*/;

    public void zoomOutValueAxes() {
        zoomOutValueAxes(chart);
    }

    protected static native void zoomOutValueAxes(JavaScriptObject chart) /*-{
        chart.zoomOutValueAxes();
    }-*/;

    public void zoomOutValueAxis(String id) {
        JavaScriptObject axis = getValueAxis(chart, id);
        if (axis != null) {
            zoomOut(axis);
        }
    }

    public void zoomOutValueAxis(int index) {
        JavaScriptObject axis = getValueAxis(chart, index);
        if (axis != null) {
            zoomOut(axis);
        }
    }

    public void zoomValueAxisToValues(String id, String startValue, String endValue) {
        JavaScriptObject axis = getValueAxis(chart, id);
        if (axis != null) {
            zoomValueAxisToValues(axis, startValue, endValue);
        }
    }

    public void zoomValueAxisToValues(int index, String startValue, String endValue) {
        JavaScriptObject axis = getValueAxis(chart, index);
        if (axis != null) {
            zoomValueAxisToValues(axis, startValue, endValue);
        }
    }

    protected static native void zoomValueAxisToValues(JavaScriptObject axis, String startValue, String endValue) /*-{
        axis.zoomToValues(startValue, endValue);
    }-*/;

    public void zoomValueAxisToValues(String id, JsDate start, JsDate end) {
        JavaScriptObject axis = getValueAxis(chart, id);
        if (axis != null) {
            zoomValueAxisToValues(axis, start, end);
        }
    }

    public void zoomValueAxisToValues(int index, JsDate start, JsDate end) {
        JavaScriptObject axis = getValueAxis(chart, index);
        if (axis != null) {
            zoomValueAxisToValues(axis, start, end);
        }
    }

    protected static native void zoomValueAxisToValues(JavaScriptObject axis, JsDate startValue, JsDate endValue) /*-{
        axis.zoomToValues(startValue, endValue);
    }-*/;

    protected static native JavaScriptObject getValueAxis(JavaScriptObject chart, String id) /*-{
        if (chart.valueAxis) {
            if (chart.valueAxis.id == id) {
                return chart.valueAxis;
            }
        }

        if (chart.valueAxes) {
            for (var i = 0; i < chart.valueAxes.length; i++) {
                if (chart.valueAxes[i].id == id) {
                    return chart.valueAxes[i];
                }
            }
        }

        return null;
    }-*/;

    protected native static JavaScriptObject getValueAxis(JavaScriptObject chart, int index) /*-{
        if (chart.valueAxes) {
            return chart.valueAxes[index];
        }
        return null;
    }-*/;

    public void addCategoryItemClickHandler(Consumer<JsCategoryItemClickEvent> handler) {
        addCategoryItemClickHandler(chart, handler);
    }

    protected native static void addCategoryItemClickHandler(JavaScriptObject chart, Consumer<JsCategoryItemClickEvent> handler) /*-{
        if (chart.categoryAxis) {
            chart.categoryAxis.addListener("clickItem", $entry(function (event) {
                handler.@java.util.function.Consumer::accept(*)(event);
            }));
        }
    }-*/;

    public void animateOnce() {
        animateOnce(chart);
    }

    protected native static void animateOnce(JavaScriptObject chart) /*-{
        var onDrawn = function (event) {
            chart.animateAgain();
            chart.removeListener(chart, "drawn", onDrawn);
        }

        chart.addListener("drawn", onDrawn);
    }-*/;

    public void addRollOutGraphHandler(Consumer<JsRollOutGraphEvent> handler) {
        addRollOutGraphHandler(chart, handler);
    }

    protected static native void addRollOutGraphHandler(JavaScriptObject chart, Consumer<JsRollOutGraphEvent> handler) /*-{
        chart.addListener("rollOutGraph", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;

    public void addRollOutGraphItemHandler(Consumer<JsRollOutGraphItemEvent> handler) {
        addRollOutGraphItemHandler(chart, handler);
    }

    protected static native void addRollOutGraphItemHandler(JavaScriptObject chart, Consumer<JsRollOutGraphItemEvent> handler) /*-{
        chart.addListener("rollOutGraphItem", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;

    public void addRollOverGraphHandler(Consumer<JsRollOverGraphEvent> handler) {
        addRollOverGraphHandler(chart, handler);
    }

    protected static native void addRollOverGraphHandler(JavaScriptObject chart, Consumer<JsRollOverGraphEvent> handler) /*-{
        chart.addListener("rollOverGraph", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;

    public void addRollOverGraphItemHandler(Consumer<JsRollOverGraphItemEvent> handler) {
        addRollOverGraphItemHandler(chart, handler);
    }

    protected static native void addRollOverGraphItemHandler(JavaScriptObject chart, Consumer<JsRollOverGraphItemEvent> handler) /*-{
        chart.addListener("rollOverGraphItem", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;
}