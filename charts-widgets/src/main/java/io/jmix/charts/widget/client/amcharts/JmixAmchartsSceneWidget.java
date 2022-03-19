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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import io.jmix.ui.widget.client.JsDate;
import io.jmix.charts.widget.client.amcharts.events.JsChartClickEvent;
import io.jmix.charts.widget.client.utils.JsUtils;

import java.util.function.Consumer;

public class JmixAmchartsSceneWidget extends Widget {

    protected static final String PIE_TYPE = "pie";
    protected static final String FUNNEL_TYPE = "funnel";

    protected JmixAmchartsJsOverlay jsOverlay;

    protected Consumer<JsChartClickEvent> chartClickHandler;
    protected Consumer<JsChartClickEvent> chartRightClickHandler;

    public JmixAmchartsSceneWidget() {
        setElement(Document.get().createDivElement());
        setStyleName("jmix-amcharts-chart");

        sinkEvents(Event.ONCONTEXTMENU | Event.ONCLICK);
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCONTEXTMENU) {
            if (chartRightClickHandler != null) {
                int x = MouseHelper.getX(event);
                int y = MouseHelper.getY(event);

                JsChartClickEvent clickEvent = jsOverlay.getClickEvent(x, y, event.getClientX(), event.getClientY());
                chartRightClickHandler.accept(clickEvent);
            }

            event.preventDefault();
        }
        if (event.getTypeInt() == Event.ONCLICK && chartClickHandler != null) {
            int x = MouseHelper.getX(event);
            int y = MouseHelper.getY(event);

            JsChartClickEvent clickEvent = jsOverlay.getClickEvent(x, y, event.getClientX(), event.getClientY());
            chartClickHandler.accept(clickEvent);

            event.preventDefault();
        }

        super.onBrowserEvent(event);
    }

    public void updateSize() {
        if (jsOverlay != null) {
            jsOverlay.updateSize();
        }
    }

    public void init(AmchartsConfig config, AmchartsEvents amchartsEvents) {
        if (jsOverlay != null) {
            jsOverlay.destroy();
        }

        jsOverlay = JmixAmchartsJsOverlay.makeChart(getElement(), config);

        if (amchartsEvents.getChartClickHandler() != null) {
            this.chartClickHandler = amchartsEvents.getChartClickHandler();
        }
        if (amchartsEvents.getChartRightClickHandler() != null) {
            this.chartRightClickHandler = amchartsEvents.getChartRightClickHandler();
        }
        if (amchartsEvents.getGraphClickHandler() != null) {
            jsOverlay.addGraphClickHandler(amchartsEvents.getGraphClickHandler());
        }
        if (amchartsEvents.getGraphItemClickHandler() != null) {
            jsOverlay.addGraphItemClickHandler(amchartsEvents.getGraphItemClickHandler());
        }
        if (amchartsEvents.getGraphItemRightClickHandler() != null) {
            jsOverlay.addGraphItemRightClickHandler(amchartsEvents.getGraphItemRightClickHandler());
        }
        if (amchartsEvents.getZoomHandler() != null) {
            jsOverlay.addZoomHandler(amchartsEvents.getZoomHandler());
        }
        if (amchartsEvents.getSliceClickHandler() != null) {
            jsOverlay.addSliceClickHandler(amchartsEvents.getSliceClickHandler());
        }
        if (amchartsEvents.getSliceRightClickHandler() != null) {
            jsOverlay.addSliceRightClickHandler(amchartsEvents.getSliceRightClickHandler());
        }
        if (amchartsEvents.getSlicePullInHandler() != null) {
            jsOverlay.addSlicePullInHandler(amchartsEvents.getSlicePullInHandler());
        }
        if (amchartsEvents.getSlicePullOutHandler() != null) {
            jsOverlay.addSlicePullOutHandler(amchartsEvents.getSlicePullOutHandler());
        }
        if (amchartsEvents.getLegendLabelClickHandler() != null) {
            jsOverlay.addLegendLabelClickHandler(amchartsEvents.getLegendLabelClickHandler());
        }
        if (amchartsEvents.getLegendMarkerClickHandler() != null) {
            jsOverlay.addLegendMarkerClickHandler(amchartsEvents.getLegendMarkerClickHandler());
        }
        if (amchartsEvents.getLegendItemShowHandler() != null) {
            jsOverlay.addLegendItemShowHandler(amchartsEvents.getLegendItemShowHandler());
        }
        if (amchartsEvents.getLegendItemHideHandler() != null) {
            jsOverlay.addLegendItemHideHandler(amchartsEvents.getLegendItemHideHandler());
        }
        if (amchartsEvents.getCursorPeriodSelectHandler() != null) {
            jsOverlay.addCursorPeriodSelectHandler(amchartsEvents.getCursorPeriodSelectHandler());
        }
        if (amchartsEvents.getCursorZoomHandler() != null) {
            jsOverlay.addCursorZoomHandler(amchartsEvents.getCursorZoomHandler());
        }
        if (amchartsEvents.getAxisZoomHandler() != null) {
            jsOverlay.addAxisZoomHandler(amchartsEvents.getAxisZoomHandler());
        }
        if (amchartsEvents.getCategoryItemClickHandler() != null) {
            jsOverlay.addCategoryItemClickHandler(amchartsEvents.getCategoryItemClickHandler());
        }
        if (amchartsEvents.getRollOutGraphHandler() != null) {
            jsOverlay.addRollOutGraphHandler(amchartsEvents.getRollOutGraphHandler());
        }
        if (amchartsEvents.getRollOutGraphItemHandler() != null) {
            jsOverlay.addRollOutGraphItemHandler(amchartsEvents.getRollOutGraphItemHandler());
        }
        if (amchartsEvents.getRollOverGraphHandler() != null) {
            jsOverlay.addRollOverGraphHandler(amchartsEvents.getRollOverGraphHandler());
        }
        if (amchartsEvents.getRollOverGraphItemHandler() != null) {
            jsOverlay.addRollOverGraphItemHandler(amchartsEvents.getRollOverGraphItemHandler());
        }

        // do animation once for pie or funnel charts, because every call chart.invalidateSize()
        // breaks animation in these charts
        String type = config.getChartType();
        if (PIE_TYPE.equals(type) || FUNNEL_TYPE.equals(type)) {
            Double startDuration = (Double) JsUtils.getValueByKey(config, "startDuration");
            startDuration = startDuration == null ? Double.valueOf(1) : startDuration;
            if (startDuration > 0) {
                jsOverlay.animateOnce();
            }
        }

        Scheduler.get().scheduleDeferred(this::updateSize);
    }

    public void updatePoints(JavaScriptObject jsObj) {
        jsOverlay.updatePoints(jsObj);
    }

    public void zoomOut() {
        if (jsOverlay != null) {
            jsOverlay.zoomOut();
        }
    }

    public void zoomToIndexes(final int start, final int end) {
        if (jsOverlay != null) {
            jsOverlay.zoomToIndexes(start, end);
        }
    }

    public void zoomToDates(final JsDate start, final JsDate end) {
        if (jsOverlay != null) {
            jsOverlay.zoomToDates(start, end);
        }
    }

    public void zoomOutValueAxes() {
        if (jsOverlay != null) {
            jsOverlay.zoomOutValueAxes();
        }
    }

    public void zoomOutValueAxis(String id) {
        if (jsOverlay != null) {
            jsOverlay.zoomOutValueAxis(id);
        }
    }

    public void zoomOutValueAxis(int index) {
        if (jsOverlay != null) {
            jsOverlay.zoomOutValueAxis(index);
        }
    }

    public void zoomValueAxisToValues(String id, String startValue, String endValue) {
        if (jsOverlay != null) {
            jsOverlay.zoomValueAxisToValues(id, startValue, endValue);
        }
    }

    public void zoomValueAxisToValues(int index, String startValue, String endValue) {
        if (jsOverlay != null) {
            jsOverlay.zoomValueAxisToValues(index, startValue, endValue);
        }
    }

    public void zoomValueAxisToValues(String id, JsDate start, JsDate end) {
        if (jsOverlay != null) {
            jsOverlay.zoomValueAxisToValues(id, start, end);
        }
    }

    public void zoomValueAxisToValues(int index, JsDate start, JsDate end) {
        if (jsOverlay != null) {
            jsOverlay.zoomValueAxisToValues(index, start, end);
        }
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        resetMouseOver();
    }

    private static native void resetMouseOver() /*-{
        $wnd.AmCharts.resetMouseOver();
    }-*/;
}