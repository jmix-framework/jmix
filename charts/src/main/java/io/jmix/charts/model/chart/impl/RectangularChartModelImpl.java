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

package io.jmix.charts.model.chart.impl;

import io.jmix.charts.model.*;
import io.jmix.charts.model.chart.ChartType;
import io.jmix.charts.model.chart.RectangularChartModel;
import io.jmix.charts.model.cursor.Cursor;
import io.jmix.charts.model.trendline.TrendLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * See documentation for properties of AmRectangularChart JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmRectangularChart">http://docs.amcharts.com/3/javascriptcharts/AmRectangularChart</a>
 */
@SuppressWarnings("unchecked")
public abstract class RectangularChartModelImpl<T extends RectangularChartModelImpl> extends CoordinateChartModelImpl<T>
        implements RectangularChartModel<T> {

    private static final long serialVersionUID = -5847502638455406714L;

    private Integer angle;

    private Integer autoMarginOffset;

    private Boolean autoMargins;

    private Cursor chartCursor;

    private Scrollbar chartScrollbar;

    private Integer depth3D;

    private Integer marginBottom;

    private Integer marginLeft;

    private Integer marginRight;

    private Boolean marginsUpdated;

    private Integer marginTop;

    private Integer maxZoomFactor;

    private Integer minMarginBottom;

    private Integer minMarginLeft;

    private Integer minMarginRight;

    private Integer minMarginTop;

    private Double plotAreaBorderAlpha;

    private Color plotAreaBorderColor;

    private Double plotAreaFillAlphas;

    private List<Color> plotAreaFillColors;

    private Integer plotAreaGradientAngle;

    private List<TrendLine> trendLines;

    private Double zoomOutButtonAlpha;

    private Color zoomOutButtonColor;

    private String zoomOutButtonImage;

    private Integer zoomOutButtonImageSize;

    private Integer zoomOutButtonPadding;

    private Double zoomOutButtonRollOverAlpha;

    private Integer zoomOutButtonTabIndex;

    private String zoomOutText;

    public RectangularChartModelImpl(ChartType type) {
        super(type);
    }

    @Override
    public Cursor getChartCursor() {
        return chartCursor;
    }

    @Override
    public T setChartCursor(Cursor chartCursor) {
        this.chartCursor = chartCursor;
        return (T) this;
    }

    @Override
    public Scrollbar getChartScrollbar() {
        return chartScrollbar;
    }

    @Override
    public T setChartScrollbar(Scrollbar chartScrollbar) {
        this.chartScrollbar = chartScrollbar;
        return (T) this;
    }

    @Override
    public List<TrendLine> getTrendLines() {
        return trendLines;
    }

    @Override
    public T setTrendLines(List<TrendLine> trendLines) {
        this.trendLines = trendLines;
        return (T) this;
    }

    @Override
    public T addTrendLines(TrendLine... trendLines) {
        if (trendLines != null) {
            if (this.trendLines == null) {
                this.trendLines = new ArrayList<>();
            }
            this.trendLines.addAll(Arrays.asList(trendLines));
        }
        return (T) this;
    }

    @Override
    public Integer getAngle() {
        return angle;
    }

    @Override
    public T setAngle(Integer angle) {
        this.angle = angle;
        return (T) this;
    }

    @Override
    public Integer getAutoMarginOffset() {
        return autoMarginOffset;
    }

    @Override
    public T setAutoMarginOffset(Integer autoMarginOffset) {
        this.autoMarginOffset = autoMarginOffset;
        return (T) this;
    }

    @Override
    public Boolean getAutoMargins() {
        return autoMargins;
    }

    @Override
    public T setAutoMargins(Boolean autoMargins) {
        this.autoMargins = autoMargins;
        return (T) this;
    }

    @Override
    public Integer getDepth3D() {
        return depth3D;
    }

    @Override
    public T setDepth3D(Integer depth3D) {
        this.depth3D = depth3D;
        return (T) this;
    }

    @Override
    public Integer getMarginBottom() {
        return marginBottom;
    }

    @Override
    public T setMarginBottom(Integer marginBottom) {
        this.marginBottom = marginBottom;
        return (T) this;
    }

    @Override
    public Integer getMarginLeft() {
        return marginLeft;
    }

    @Override
    public T setMarginLeft(Integer marginLeft) {
        this.marginLeft = marginLeft;
        return (T) this;
    }

    @Override
    public Integer getMarginRight() {
        return marginRight;
    }

    @Override
    public T setMarginRight(Integer marginRight) {
        this.marginRight = marginRight;
        return (T) this;
    }

    @Override
    public Integer getMarginTop() {
        return marginTop;
    }

    @Override
    public T setMarginTop(Integer marginTop) {
        this.marginTop = marginTop;
        return (T) this;
    }

    @Override
    public Boolean getMarginsUpdated() {
        return marginsUpdated;
    }

    @Override
    public T setMarginsUpdated(Boolean marginsUpdated) {
        this.marginsUpdated = marginsUpdated;
        return (T) this;
    }

    @Override
    public Double getPlotAreaBorderAlpha() {
        return plotAreaBorderAlpha;
    }

    @Override
    public T setPlotAreaBorderAlpha(Double plotAreaBorderAlpha) {
        this.plotAreaBorderAlpha = plotAreaBorderAlpha;
        return (T) this;
    }

    @Override
    public Color getPlotAreaBorderColor() {
        return plotAreaBorderColor;
    }

    @Override
    public T setPlotAreaBorderColor(Color plotAreaBorderColor) {
        this.plotAreaBorderColor = plotAreaBorderColor;
        return (T) this;
    }

    @Override
    public Double getPlotAreaFillAlphas() {
        return plotAreaFillAlphas;
    }

    @Override
    public T setPlotAreaFillAlphas(Double plotAreaFillAlphas) {
        this.plotAreaFillAlphas = plotAreaFillAlphas;
        return (T) this;
    }

    @Override
    public List<Color> getPlotAreaFillColors() {
        return plotAreaFillColors;
    }

    @Override
    public T setPlotAreaFillColors(List<Color> plotAreaFillColors) {
        this.plotAreaFillColors = plotAreaFillColors;
        return (T) this;
    }

    @Override
    public Integer getPlotAreaGradientAngle() {
        return plotAreaGradientAngle;
    }

    @Override
    public T setPlotAreaGradientAngle(Integer plotAreaGradientAngle) {
        this.plotAreaGradientAngle = plotAreaGradientAngle;
        return (T) this;
    }

    @Override
    public Double getZoomOutButtonAlpha() {
        return zoomOutButtonAlpha;
    }

    @Override
    public T setZoomOutButtonAlpha(Double zoomOutButtonAlpha) {
        this.zoomOutButtonAlpha = zoomOutButtonAlpha;
        return (T) this;
    }

    @Override
    public Color getZoomOutButtonColor() {
        return zoomOutButtonColor;
    }

    @Override
    public T setZoomOutButtonColor(Color zoomOutButtonColor) {
        this.zoomOutButtonColor = zoomOutButtonColor;
        return (T) this;
    }

    @Override
    public String getZoomOutButtonImage() {
        return zoomOutButtonImage;
    }

    @Override
    public T setZoomOutButtonImage(String zoomOutButtonImage) {
        this.zoomOutButtonImage = zoomOutButtonImage;
        return (T) this;
    }

    @Override
    public Integer getZoomOutButtonImageSize() {
        return zoomOutButtonImageSize;
    }

    @Override
    public T setZoomOutButtonImageSize(Integer zoomOutButtonImageSize) {
        this.zoomOutButtonImageSize = zoomOutButtonImageSize;
        return (T) this;
    }

    @Override
    public Integer getZoomOutButtonPadding() {
        return zoomOutButtonPadding;
    }

    @Override
    public T setZoomOutButtonPadding(Integer zoomOutButtonPadding) {
        this.zoomOutButtonPadding = zoomOutButtonPadding;
        return (T) this;
    }

    @Override
    public Double getZoomOutButtonRollOverAlpha() {
        return zoomOutButtonRollOverAlpha;
    }

    @Override
    public T setZoomOutButtonRollOverAlpha(Double zoomOutButtonRollOverAlpha) {
        this.zoomOutButtonRollOverAlpha = zoomOutButtonRollOverAlpha;
        return (T) this;
    }

    @Override
    public String getZoomOutText() {
        return zoomOutText;
    }

    @Override
    public T setZoomOutText(String zoomOutText) {
        this.zoomOutText = zoomOutText;
        return (T) this;
    }

    @Override
    public Integer getMaxZoomFactor() {
        return maxZoomFactor;
    }

    @Override
    public T setMaxZoomFactor(Integer maxZoomFactor) {
        this.maxZoomFactor = maxZoomFactor;
        return (T) this;
    }

    @Override
    public Integer getMinMarginBottom() {
        return minMarginBottom;
    }

    @Override
    public T setMinMarginBottom(Integer minMarginBottom) {
        this.minMarginBottom = minMarginBottom;
        return (T) this;
    }

    @Override
    public Integer getMinMarginLeft() {
        return minMarginLeft;
    }

    @Override
    public T setMinMarginLeft(Integer minMarginLeft) {
        this.minMarginLeft = minMarginLeft;
        return (T) this;
    }

    @Override
    public Integer getMinMarginRight() {
        return minMarginRight;
    }

    @Override
    public T setMinMarginRight(Integer minMarginRight) {
        this.minMarginRight = minMarginRight;
        return (T) this;
    }

    @Override
    public Integer getMinMarginTop() {
        return minMarginTop;
    }

    @Override
    public T setMinMarginTop(Integer minMarginTop) {
        this.minMarginTop = minMarginTop;
        return (T) this;
    }

    @Override
    public Integer getZoomOutButtonTabIndex() {
        return zoomOutButtonTabIndex;
    }

    @Override
    public T setZoomOutButtonTabIndex(Integer zoomOutButtonTabIndex) {
        this.zoomOutButtonTabIndex = zoomOutButtonTabIndex;
        return (T) this;
    }
}