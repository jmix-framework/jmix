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

package io.jmix.charts.component.impl;


import io.jmix.charts.model.chart.impl.RectangularChartModelImpl;
import io.jmix.core.common.event.Subscription;
import io.jmix.charts.component.RectangularChart;
import io.jmix.charts.model.Color;
import io.jmix.charts.model.Scrollbar;
import io.jmix.charts.model.cursor.Cursor;
import io.jmix.charts.model.trendline.TrendLine;
import io.jmix.charts.widget.amcharts.events.cursor.listener.CursorPeriodSelectListener;
import io.jmix.charts.widget.amcharts.events.cursor.listener.CursorZoomListener;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class RectangularChartImpl<T extends RectangularChart, M extends RectangularChartModelImpl>
        extends CoordinateChartImpl<T, M> implements RectangularChart<T> {

    protected CursorPeriodSelectListener periodSelectHandler;

    protected CursorZoomListener cursorZoomHandler;

    @Override
    protected void setupDefaults(M chart) {
        super.setupDefaults(chart);

        setupRectangularChartDefaults(chart);
    }

    protected void setupRectangularChartDefaults(RectangularChartModelImpl chart) {
        chart.setZoomOutText(messages.getMessage("amcharts.zoomOutText"));
    }

    @Override
    public Cursor getChartCursor() {
        return getModel().getChartCursor();
    }

    @Override
    public T setChartCursor(Cursor chartCursor) {
        if (chartCursor != null) {
            if (StringUtils.isEmpty(chartCursor.getCategoryBalloonDateFormat())) {
                String format = messages.getMessage("amcharts.rectangularChart.categoryBalloonDateFormat");
                chartCursor.setCategoryBalloonDateFormat(format);
            }
        }

        getModel().setChartCursor(chartCursor);
        return (T) this;
    }

    @Override
    public Scrollbar getChartScrollbar() {
        return getModel().getChartScrollbar();
    }

    @Override
    public T setChartScrollbar(Scrollbar chartScrollbar) {
        getModel().setChartScrollbar(chartScrollbar);
        return (T) this;
    }

    @Override
    public List<TrendLine> getTrendLines() {
        return getModel().getTrendLines();
    }

    @Override
    public T setTrendLines(List list) {
        getModel().setTrendLines(list);
        return (T) this;
    }

    @Override
    public T addTrendLines(TrendLine... trendLines) {
        getModel().addTrendLines(trendLines);
        return (T) this;
    }

    @Override
    public Integer getAngle() {
        return getModel().getAngle();
    }

    @Override
    public T setAngle(Integer angle) {
        getModel().setAngle(angle);
        return (T) this;
    }

    @Override
    public Integer getAutoMarginOffset() {
        return getModel().getAutoMarginOffset();
    }

    @Override
    public T setAutoMarginOffset(Integer autoMarginOffset) {
        getModel().setAutoMarginOffset(autoMarginOffset);
        return (T) this;
    }

    @Override
    public Boolean getAutoMargins() {
        return getModel().getAutoMargins();
    }

    @Override
    public T setAutoMargins(Boolean autoMargins) {
        getModel().setAutoMargins(autoMargins);
        return (T) this;
    }

    @Override
    public Integer getDepth3D() {
        return getModel().getDepth3D();
    }

    @Override
    public T setDepth3D(Integer depth3D) {
        getModel().setDepth3D(depth3D);
        return (T) this;
    }

    @Override
    public Integer getMarginBottom() {
        return getModel().getMarginBottom();
    }

    @Override
    public T setMarginBottom(Integer marginBottom) {
        getModel().setMarginBottom(marginBottom);
        return (T) this;
    }

    @Override
    public Integer getMarginLeft() {
        return getModel().getMarginLeft();
    }

    @Override
    public T setMarginLeft(Integer marginLeft) {
        getModel().setMarginLeft(marginLeft);
        return (T) this;
    }

    @Override
    public Integer getMarginRight() {
        return getModel().getMarginRight();
    }

    @Override
    public T setMarginRight(Integer marginRight) {
        getModel().setMarginRight(marginRight);
        return (T) this;
    }

    @Override
    public Integer getMarginTop() {
        return getModel().getMarginTop();
    }

    @Override
    public T setMarginTop(Integer marginTop) {
        getModel().setMarginTop(marginTop);
        return (T) this;
    }

    @Override
    public Boolean getMarginsUpdated() {
        return getModel().getMarginsUpdated();
    }

    @Override
    public T setMarginsUpdated(Boolean marginsUpdated) {
        getModel().setMarginsUpdated(marginsUpdated);
        return (T) this;
    }

    @Override
    public Double getPlotAreaBorderAlpha() {
        return getModel().getPlotAreaBorderAlpha();
    }

    @Override
    public T setPlotAreaBorderAlpha(Double plotAreaBorderAlpha) {
        getModel().setPlotAreaBorderAlpha(plotAreaBorderAlpha);
        return (T) this;
    }

    @Override
    public Color getPlotAreaBorderColor() {
        return getModel().getPlotAreaBorderColor();
    }

    @Override
    public T setPlotAreaBorderColor(Color plotAreaBorderColor) {
        getModel().setPlotAreaBorderColor(plotAreaBorderColor);
        return (T) this;
    }

    @Override
    public Double getPlotAreaFillAlphas() {
        return getModel().getPlotAreaFillAlphas();
    }

    @Override
    public T setPlotAreaFillAlphas(Double plotAreaFillAlphas) {
        getModel().setPlotAreaFillAlphas(plotAreaFillAlphas);
        return (T) this;
    }

    @Override
    public List<Color> getPlotAreaFillColors() {
        return getModel().getPlotAreaFillColors();
    }

    @Override
    public T setPlotAreaFillColors(List<Color> plotAreaFillColors) {
        getModel().setPlotAreaFillColors(plotAreaFillColors);
        return (T) this;
    }

    @Override
    public Integer getPlotAreaGradientAngle() {
        return getModel().getPlotAreaGradientAngle();
    }

    @Override
    public T setPlotAreaGradientAngle(Integer plotAreaGradientAngle) {
        getModel().setPlotAreaGradientAngle(plotAreaGradientAngle);
        return (T) this;
    }

    @Override
    public Double getZoomOutButtonAlpha() {
        return getModel().getZoomOutButtonAlpha();
    }

    @Override
    public T setZoomOutButtonAlpha(Double zoomOutButtonAlpha) {
        getModel().setZoomOutButtonAlpha(zoomOutButtonAlpha);
        return (T) this;
    }

    @Override
    public Color getZoomOutButtonColor() {
        return getModel().getZoomOutButtonColor();
    }

    @Override
    public T setZoomOutButtonColor(Color zoomOutButtonColor) {
        getModel().setZoomOutButtonColor(zoomOutButtonColor);
        return (T) this;
    }

    @Override
    public String getZoomOutButtonImage() {
        return getModel().getZoomOutButtonImage();
    }

    @Override
    public T setZoomOutButtonImage(String zoomOutButtonImage) {
        getModel().setZoomOutButtonImage(zoomOutButtonImage);
        return (T) this;
    }

    @Override
    public Integer getZoomOutButtonImageSize() {
        return getModel().getZoomOutButtonImageSize();
    }

    @Override
    public T setZoomOutButtonImageSize(Integer zoomOutButtonImageSize) {
        getModel().setZoomOutButtonImageSize(zoomOutButtonImageSize);
        return (T) this;
    }

    @Override
    public Integer getZoomOutButtonPadding() {
        return getModel().getZoomOutButtonPadding();
    }

    @Override
    public T setZoomOutButtonPadding(Integer zoomOutButtonPadding) {
        getModel().setZoomOutButtonPadding(zoomOutButtonPadding);
        return (T) this;
    }

    @Override
    public Double getZoomOutButtonRollOverAlpha() {
        return getModel().getZoomOutButtonRollOverAlpha();
    }

    @Override
    public T setZoomOutButtonRollOverAlpha(Double zoomOutButtonRollOverAlpha) {
        getModel().setZoomOutButtonRollOverAlpha(zoomOutButtonRollOverAlpha);
        return (T) this;
    }

    @Override
    public String getZoomOutText() {
        return getModel().getZoomOutText();
    }

    @Override
    public T setZoomOutText(String zoomOutText) {
        getModel().setZoomOutText(zoomOutText);
        return (T) this;
    }

    @Override
    public Integer getMaxZoomFactor() {
        return getModel().getMaxZoomFactor();
    }

    @Override
    public T setMaxZoomFactor(Integer maxZoomFactor) {
        getModel().setMaxZoomFactor(maxZoomFactor);
        return (T) this;
    }

    @Override
    public Integer getMinMarginBottom() {
        return getModel().getMinMarginBottom();
    }

    @Override
    public T setMinMarginBottom(Integer minMarginBottom) {
        getModel().setMinMarginBottom(minMarginBottom);
        return (T) this;
    }

    @Override
    public Integer getMinMarginLeft() {
        return getModel().getMinMarginLeft();
    }

    @Override
    public T setMinMarginLeft(Integer minMarginLeft) {
        getModel().setMinMarginLeft(minMarginLeft);
        return (T) this;
    }

    @Override
    public Integer getMinMarginRight() {
        return getModel().getMinMarginRight();
    }

    @Override
    public T setMinMarginRight(Integer minMarginRight) {
        getModel().setMinMarginRight(minMarginRight);
        return (T) this;
    }

    @Override
    public Integer getMinMarginTop() {
        return getModel().getMinMarginTop();
    }

    @Override
    public T setMinMarginTop(Integer minMarginTop) {
        getModel().setMinMarginTop(minMarginTop);
        return (T) this;
    }

    @Override
    public Integer getZoomOutButtonTabIndex() {
        return getModel().getZoomOutButtonTabIndex();
    }

    @Override
    public T setZoomOutButtonTabIndex(Integer zoomOutButtonTabIndex) {
        getModel().setZoomOutButtonTabIndex(zoomOutButtonTabIndex);
        return (T) this;
    }

    @Override
    public Subscription addCursorPeriodSelectListener(Consumer<CursorPeriodSelectEvent> listener) {
        if (periodSelectHandler == null) {
            periodSelectHandler = this::onCursorPeriodSelect;
            component.addCursorPeriodSelectListener(periodSelectHandler);
        }

        return getEventHub().subscribe(CursorPeriodSelectEvent.class, listener);
    }

    protected void onCursorPeriodSelect(io.jmix.charts.widget.amcharts.events.cursor.CursorPeriodSelectEvent e) {
        publish(CursorPeriodSelectEvent.class, new CursorPeriodSelectEvent(this, e.getStart(), e.getEnd()));
    }

    @Override
    public Subscription addCursorZoomListener(Consumer<CursorZoomEvent> listener) {
        if (cursorZoomHandler == null) {
            cursorZoomHandler = this::onCursorZoom;
            component.addCursorZoomListener(cursorZoomHandler);
        }

        return getEventHub().subscribe(CursorZoomEvent.class, listener);
    }

    protected void onCursorZoom(io.jmix.charts.widget.amcharts.events.cursor.CursorZoomEvent e) {
        publish(CursorZoomEvent.class, new CursorZoomEvent(this, e.getStart(), e.getEnd()));
    }
}