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

package io.jmix.charts.widget.client.amstockcharts;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import io.jmix.charts.widget.client.amstockcharts.events.*;

import java.util.function.Consumer;

public class JmixAmStockChartJsOverlay {
    private static boolean ready = false;

    private JavaScriptObject chart;

    public JmixAmStockChartJsOverlay(JavaScriptObject chart) {
        this.chart = chart;
    }

    public static JmixAmStockChartJsOverlay makeChart(Element placeHolder, JavaScriptObject json) {
        if (!ready) {
            handleLoad();
            ready = true;
        }

        return new JmixAmStockChartJsOverlay(makeJsChart(placeHolder, json));
    }

    public void updatePoints(JavaScriptObject value) {
        updatePoints(chart, value);
    }

    private native void updatePoints(JavaScriptObject chart, JavaScriptObject src) /*-{

        for (var i = 0; i < chart.dataSets.length; i++) {
            var dataSet = chart.dataSets[i];
            var dataSetObj = src[dataSet.id];
            if (dataSetObj) {

                (function () {
                    var opp = 'add';
                    if (dataSetObj[opp]) {
                        for (var i = 0; i < dataSetObj[opp].length; i++) {
                            dataSetObj[opp][i].$d = dataSet.id;
                            dataSet.dataProvider.push(dataSetObj[opp][i]);
                        }
                    }
                })();

                (function () {
                    var opp = 'remove';
                    if (dataSetObj[opp]) {
                        for (var i = 0; i < dataSetObj[opp].length; i++) {
                            for (var j = 0; j < dataSet.dataProvider.length; j++) {
                                if (dataSet.dataProvider[j].$k == dataSetObj[opp][i].$k) {
                                    dataSet.dataProvider.splice(j, 1);
                                    break;
                                }
                            }
                        }
                    }
                })();

                (function () {
                    var opp = 'update';
                    if (dataSetObj[opp]) {
                        for (var i = 0; i < dataSetObj[opp].length; i++) {
                            for (var j = 0; j < dataSet.dataProvider.length; j++) {
                                if (dataSet.dataProvider[j].$k == dataSetObj[opp][i].$k) {
                                    dataSet.dataProvider[j] = dataSetObj[opp][i];
                                    dataSet.dataProvider[j].$d = dataSet.id;
                                    break;
                                }
                            }
                        }
                    }
                })();
            }
        }

        chart.validateData();
    }-*/;

    private static native void handleLoad() /*-{
        $wnd.AmCharts.handleLoad();
    }-*/;

    private static native JavaScriptObject makeJsChart(Element placeHolder, JavaScriptObject json) /*-{
        var chart = $wnd.AmCharts.makeChart(placeHolder, json);
        return chart;
    }-*/;

    public void updateSize() {
        updateSize(chart);
    }

    private static native void updateSize(JavaScriptObject chart) /*-{
        chart.invalidateSize();
    }-*/;

    public void destroy() {
        destroy(chart);
    }

    private static native void destroy(JavaScriptObject chart) /*-{
        chart.clear();
    }-*/;

    public JsStockChartClickEvent getClickEvent(int x, int y, int absoluteX, int absoluteY) {
        return getClickEvent(chart, x, y, absoluteX, absoluteY);
    }

    private static native JsStockChartClickEvent getClickEvent(JavaScriptObject chart, int x, int y, int absoluteX, int absoluteY) /*-{
        var event = {};
        event.x = x;
        event.y = y;
        event.absoluteX = absoluteX;
        event.absoluteY = absoluteY;

        return event;
    }-*/;

    public void addStockEventClickHandler(Consumer<JsStockEventClickEvent> handler) {
        addStockEventClickHandler(chart, handler);
    }

    private static native void addStockEventClickHandler(JavaScriptObject chart, Consumer<JsStockEventClickEvent> handler) /*-{
        chart.addListener("clickStockEvent", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;

    public void addStockEventRollOutHandler(Consumer<JsStockEventRollOutEvent> handler) {
        addStockEventRollOutHandler(chart, handler);
    }

    private static native void addStockEventRollOutHandler(JavaScriptObject chart, Consumer<JsStockEventRollOutEvent> handler) /*-{
        chart.addListener("rollOutStockEvent", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;

    public void addStockEventRollOverHandler(Consumer<JsStockEventRollOverEvent> handler) {
        addStockEventRollOverHandler(chart, handler);
    }

    private static native void addStockEventRollOverHandler(JavaScriptObject chart, Consumer<JsStockEventRollOverEvent> handler) /*-{
        chart.addListener("rollOverStockEvent", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;

    public void addStockPanelZoomHandler(Consumer<JsStockPanelZoomEvent> handler) {
        addStockZoomHandler(chart, handler);
    }

    private static native void addStockZoomHandler(JavaScriptObject chart, Consumer<JsStockPanelZoomEvent> handler) /*-{
        chart.addListener("zoomed", $entry(function (event) {
            handler.@java.util.function.Consumer::accept(*)(event);
        }));
    }-*/;

    public void addPeriodSelectorChangeHandler(Consumer<JsPeriodSelectorChangeEvent> handler) {
        addPeriodSelectorChangeHandler(chart, handler);
    }

    private static native void addPeriodSelectorChangeHandler(JavaScriptObject chart, Consumer<JsPeriodSelectorChangeEvent> handler) /*-{
        if (chart.periodSelector) {
            chart.periodSelector.addListener("changed", $entry(function (event) {
                handler.@java.util.function.Consumer::accept(*)(event);
            }));
        }
    }-*/;

    public void addDataSetSelectorCompareHandler(Consumer<JsDataSetSelectorCompareEvent> handler) {
        addDataSetSelectorCompareHandler(chart, handler);
    }

    private static native void addDataSetSelectorCompareHandler(JavaScriptObject chart, Consumer<JsDataSetSelectorCompareEvent> handler) /*-{
        if (chart.dataSetSelector) {
            chart.dataSetSelector.addListener("dataSetCompared", $entry(function (event) {
                handler.@java.util.function.Consumer::accept(*)(event);
            }));
        }
    }-*/;

    public void addDataSetSelectorSelectHandler(Consumer<JsDataSetSelectorSelectEvent> handler) {
        addDataSetSelectorSelectHandler(chart, handler);
    }

    private static native void addDataSetSelectorSelectHandler(JavaScriptObject chart, Consumer<JsDataSetSelectorSelectEvent> handler) /*-{
        if (chart.dataSetSelector) {
            chart.dataSetSelector.addListener("dataSetSelected", $entry(function (event) {
                handler.@java.util.function.Consumer::accept(*)(event);
            }));
        }
    }-*/;

    public void addDataSetSelectorUnCompareHandler(Consumer<JsDataSetSelectorUnCompareEvent> handler) {
        addDataSetSelectorUnCompareHandler(chart, handler);
    }

    private static native void addDataSetSelectorUnCompareHandler(JavaScriptObject chart, Consumer<JsDataSetSelectorUnCompareEvent> handler) /*-{
        if (chart.dataSetSelector) {
            chart.dataSetSelector.addListener("dataSetUncompared", $entry(function (event) {
                handler.@java.util.function.Consumer::accept(*)(event);
            }));
        }
    }-*/;

    public void addStockGraphClickHandler(Consumer<JsStockGraphClickEvent> handler) {
        addStockGraphClickHandler(chart, handler);
    }

    private static native void addStockGraphClickHandler(JavaScriptObject chart, Consumer<JsStockGraphClickEvent> handler) /*-{
        if (chart.panels) {
            for (var i = 0; i < chart.panels.length; i++) {
                chart.panels[i].addListener("clickGraph", $entry(function (event) {
                    handler.@java.util.function.Consumer::accept(*)(event);
                }));
            }
        }
    }-*/;

    public void addStockGraphRollOutHandler(Consumer<JsStockGraphRollOutEvent> handler) {
        addStockGraphRollOutHandler(chart, handler);
    }

    private static native void addStockGraphRollOutHandler(JavaScriptObject chart, Consumer<JsStockGraphRollOutEvent> handler) /*-{
        if (chart.panels) {
            for (var i = 0; i < chart.panels.length; i++) {
                chart.panels[i].addListener("rollOutGraph", $entry(function (event) {
                    handler.@java.util.function.Consumer::accept(*)(event);
                }));
            }
        }
    }-*/;

    public void addStockGraphRollOverHandler(Consumer<JsStockGraphRollOverEvent> handler) {
        addStockGraphRollOverHandler(chart, handler);
    }

    private static native void addStockGraphRollOverHandler(JavaScriptObject chart, Consumer<JsStockGraphRollOverEvent> handler) /*-{
        if (chart.panels) {
            for (var i = 0; i < chart.panels.length; i++) {
                chart.panels[i].addListener("rollOverGraph", $entry(function (event) {
                    handler.@java.util.function.Consumer::accept(*)(event);
                }));
            }
        }
    }-*/;

    public void addStockGraphItemClickHandler(Consumer<JsStockGraphItemClickEvent> handler) {
        addStockGraphItemClickHandler(chart, handler);
    }

    private static native void addStockGraphItemClickHandler(JavaScriptObject chart, Consumer<JsStockGraphItemClickEvent> handler) /*-{
        if (chart.panels) {
            for (var i = 0; i < chart.panels.length; i++) {
                chart.panels[i].addListener("clickGraphItem", $entry(function (event) {
                    handler.@java.util.function.Consumer::accept(*)(event);
                }));
            }
        }
    }-*/;

    public void addStockGraphItemRightClickHandler(Consumer<JsStockGraphItemRightClickEvent> handler) {
        addStockGraphItemRightClickHandler(chart, handler);
    }

    private static native void addStockGraphItemRightClickHandler(JavaScriptObject chart, Consumer<JsStockGraphItemRightClickEvent> handler) /*-{
        if (chart.panels) {
            for (var i = 0; i < chart.panels.length; i++) {
                chart.panels[i].addListener("rightClickGraphItem", $entry(function (event) {
                    handler.@java.util.function.Consumer::accept(*)(event);
                }));
            }
        }
    }-*/;

    public void addStockGraphItemRollOutHandler(Consumer<JsStockGraphItemRollOutEvent> handler) {
        addStockGraphItemRollOutHandler(chart, handler);
    }

    private static native void addStockGraphItemRollOutHandler(JavaScriptObject chart, Consumer<JsStockGraphItemRollOutEvent> handler) /*-{
        if (chart.panels) {
            for (var i = 0; i < chart.panels.length; i++) {
                chart.panels[i].addListener("rollOutGraphItem", $entry(function (event) {
                    handler.@java.util.function.Consumer::accept(*)(event);
                }));
            }
        }
    }-*/;

    public void addStockGraphItemRollOverHandler(Consumer<JsStockGraphItemRollOverEvent> handler) {
        addStockGraphItemRollOverHandler(chart, handler);
    }

    private static native void addStockGraphItemRollOverHandler(JavaScriptObject chart, Consumer<JsStockGraphItemRollOverEvent> handler) /*-{
        if (chart.panels) {
            for (var i = 0; i < chart.panels.length; i++) {
                chart.panels[i].addListener("rollOverGraphItem", $entry(function (event) {
                    handler.@java.util.function.Consumer::accept(*)(event);
                }));
            }
        }
    }-*/;
}