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


import io.jmix.charts.widget.client.amcharts.events.*;

import java.util.function.Consumer;

public class AmchartsEvents {
    private Consumer<JsChartClickEvent> chartClickHandler;
    private Consumer<JsChartClickEvent> chartRightClickHandler;

    private Consumer<JsGraphClickEvent> graphClickHandler;
    private Consumer<JsGraphItemClickEvent> graphItemClickHandler;
    private Consumer<JsGraphItemClickEvent> graphItemRightClickHandler;

    private Consumer<JsZoomEvent> zoomHandler;

    private Consumer<JsSliceClickEvent> sliceClickHandler;
    private Consumer<JsSliceClickEvent> sliceRightClickHandler;
    private Consumer<JsSlicePullEvent> slicePullInHandler;
    private Consumer<JsSlicePullEvent> slicePullOutHandler;

    private Consumer<JsLegendEvent> legendLabelClickHandler;
    private Consumer<JsLegendEvent> legendMarkerClickHandler;
    private Consumer<JsLegendEvent> legendItemShowHandler;
    private Consumer<JsLegendEvent> legendItemHideHandler;

    private Consumer<JsCursorEvent> cursorPeriodSelectHandler;
    private Consumer<JsCursorEvent> cursorZoomHandler;
    private Consumer<JsAxisZoomedEvent> axisZoomHandler;

    private Consumer<JsCategoryItemClickEvent> categoryItemClickHandler;

    private Consumer<JsRollOutGraphEvent> rollOutGraphHandler;
    private Consumer<JsRollOutGraphItemEvent> rollOutGraphItemHandler;
    private Consumer<JsRollOverGraphEvent> rollOverGraphHandler;
    private Consumer<JsRollOverGraphItemEvent> rollOverGraphItemHandler;

    public Consumer<JsChartClickEvent> getChartClickHandler() {
        return chartClickHandler;
    }

    public void setChartClickHandler(Consumer<JsChartClickEvent> chartClickHandler) {
        this.chartClickHandler = chartClickHandler;
    }

    public Consumer<JsGraphClickEvent> getGraphClickHandler() {
        return graphClickHandler;
    }

    public void setGraphClickHandler(Consumer<JsGraphClickEvent> graphClickHandler) {
        this.graphClickHandler = graphClickHandler;
    }

    public Consumer<JsChartClickEvent> getChartRightClickHandler() {
        return chartRightClickHandler;
    }

    public void setChartRightClickHandler(Consumer<JsChartClickEvent> chartRightClickHandler) {
        this.chartRightClickHandler = chartRightClickHandler;
    }

    public Consumer<JsGraphItemClickEvent> getGraphItemClickHandler() {
        return graphItemClickHandler;
    }

    public void setGraphItemClickHandler(Consumer<JsGraphItemClickEvent> graphItemClickHandler) {
        this.graphItemClickHandler = graphItemClickHandler;
    }

    public Consumer<JsGraphItemClickEvent> getGraphItemRightClickHandler() {
        return graphItemRightClickHandler;
    }

    public void setGraphItemRightClickHandler(Consumer<JsGraphItemClickEvent> graphItemRightClickHandler) {
        this.graphItemRightClickHandler = graphItemRightClickHandler;
    }

    public Consumer<JsZoomEvent> getZoomHandler() {
        return zoomHandler;
    }

    public void setZoomHandler(Consumer<JsZoomEvent> zoomHandler) {
        this.zoomHandler = zoomHandler;
    }

    public Consumer<JsSliceClickEvent> getSliceClickHandler() {
        return sliceClickHandler;
    }

    public void setSliceClickHandler(Consumer<JsSliceClickEvent> sliceClickHandler) {
        this.sliceClickHandler = sliceClickHandler;
    }

    public Consumer<JsSlicePullEvent> getSlicePullInHandler() {
        return slicePullInHandler;
    }

    public void setSlicePullInHandler(Consumer<JsSlicePullEvent> slicePullInHandler) {
        this.slicePullInHandler = slicePullInHandler;
    }

    public Consumer<JsSlicePullEvent> getSlicePullOutHandler() {
        return slicePullOutHandler;
    }

    public void setSlicePullOutHandler(Consumer<JsSlicePullEvent> slicePullOutHandler) {
        this.slicePullOutHandler = slicePullOutHandler;
    }

    public Consumer<JsSliceClickEvent> getSliceRightClickHandler() {
        return sliceRightClickHandler;
    }

    public void setSliceRightClickHandler(Consumer<JsSliceClickEvent> sliceRightClickHandler) {
        this.sliceRightClickHandler = sliceRightClickHandler;
    }

    public Consumer<JsLegendEvent> getLegendItemHideHandler() {
        return legendItemHideHandler;
    }

    public void setLegendItemHideHandler(Consumer<JsLegendEvent> legendItemHideHandler) {
        this.legendItemHideHandler = legendItemHideHandler;
    }

    public Consumer<JsLegendEvent> getLegendItemShowHandler() {
        return legendItemShowHandler;
    }

    public void setLegendItemShowHandler(Consumer<JsLegendEvent> legendItemShowHandler) {
        this.legendItemShowHandler = legendItemShowHandler;
    }

    public Consumer<JsLegendEvent> getLegendLabelClickHandler() {
        return legendLabelClickHandler;
    }

    public void setLegendLabelClickHandler(Consumer<JsLegendEvent> legendLabelClickHandler) {
        this.legendLabelClickHandler = legendLabelClickHandler;
    }

    public Consumer<JsLegendEvent> getLegendMarkerClickHandler() {
        return legendMarkerClickHandler;
    }

    public void setLegendMarkerClickHandler(Consumer<JsLegendEvent> legendMarkerClickHandler) {
        this.legendMarkerClickHandler = legendMarkerClickHandler;
    }

    public Consumer<JsCursorEvent> getCursorPeriodSelectHandler() {
        return cursorPeriodSelectHandler;
    }

    public void setCursorPeriodSelectHandler(Consumer<JsCursorEvent> cursorPeriodSelectHandler) {
        this.cursorPeriodSelectHandler = cursorPeriodSelectHandler;
    }

    public Consumer<JsCursorEvent> getCursorZoomHandler() {
        return cursorZoomHandler;
    }

    public void setCursorZoomHandler(Consumer<JsCursorEvent> cursorZoomHandler) {
        this.cursorZoomHandler = cursorZoomHandler;
    }

    public Consumer<JsAxisZoomedEvent> getAxisZoomHandler() {
        return axisZoomHandler;
    }

    public void setAxisZoomHandler(Consumer<JsAxisZoomedEvent> axisZoomHandler) {
        this.axisZoomHandler = axisZoomHandler;
    }

    public void setCategoryItemClickHandler(Consumer<JsCategoryItemClickEvent> categoryItemClickHandler) {
        this.categoryItemClickHandler = categoryItemClickHandler;
    }

    public Consumer<JsCategoryItemClickEvent> getCategoryItemClickHandler() {
        return categoryItemClickHandler;
    }

    public Consumer<JsRollOutGraphEvent> getRollOutGraphHandler() {
        return rollOutGraphHandler;
    }

    public void setRollOutGraphHandler(Consumer<JsRollOutGraphEvent> rollOutGraphHandler) {
        this.rollOutGraphHandler = rollOutGraphHandler;
    }

    public Consumer<JsRollOutGraphItemEvent> getRollOutGraphItemHandler() {
        return rollOutGraphItemHandler;
    }

    public void setRollOutGraphItemHandler(Consumer<JsRollOutGraphItemEvent> rollOutGraphItemHandler) {
        this.rollOutGraphItemHandler = rollOutGraphItemHandler;
    }

    public Consumer<JsRollOverGraphEvent> getRollOverGraphHandler() {
        return rollOverGraphHandler;
    }

    public void setRollOverGraphHandler(Consumer<JsRollOverGraphEvent> rollOverGraphHandler) {
        this.rollOverGraphHandler = rollOverGraphHandler;
    }

    public Consumer<JsRollOverGraphItemEvent> getRollOverGraphItemHandler() {
        return rollOverGraphItemHandler;
    }

    public void setRollOverGraphItemHandler(Consumer<JsRollOverGraphItemEvent> rollOverGraphItemHandler) {
        this.rollOverGraphItemHandler = rollOverGraphItemHandler;
    }
}