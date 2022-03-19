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
import io.jmix.charts.model.GaugeArrow;
import io.jmix.charts.model.GaugeAxis;
import io.jmix.charts.model.animation.AnimationEffect;
import io.jmix.charts.model.chart.AngularGaugeChartModel;
import io.jmix.charts.model.chart.ChartType;
import io.jmix.charts.model.graph.Pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * See documentation for properties of AmAngularGauge JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmAngularGauge">http://docs.amcharts.com/3/javascriptcharts/AmAngularGauge</a>
 */
public class AngularGaugeChartModelImpl extends AbstractChart<AngularGaugeChartModelImpl>
        implements AngularGaugeChartModel<AngularGaugeChartModelImpl> {

    private static final long serialVersionUID = -6090793752941909292L;

    private Boolean adjustSize;

    private List<GaugeArrow> arrows;

    private List<GaugeAxis> axes;

    private Boolean clockWiseOnly;

    private Double faceAlpha;

    private Double faceBorderAlpha;

    private Color faceBorderColor;

    private Integer faceBorderWidth;

    private Color faceColor;

    private Pattern facePattern;

    private String gaugeX;

    private String gaugeY;

    private Integer marginBottom;

    private Integer marginLeft;

    private Integer marginRight;

    private Integer marginTop;

    private Integer minRadius;

    private Double startDuration;

    private AnimationEffect startEffect;

    public AngularGaugeChartModelImpl() {
        super(ChartType.GAUGE);
    }

    @Override
    public Boolean getAdjustSize() {
        return adjustSize;
    }

    @Override
    public AngularGaugeChartModelImpl setAdjustSize(Boolean adjustSize) {
        this.adjustSize = adjustSize;
        return this;
    }

    @Override
    public List<GaugeArrow> getArrows() {
        return arrows;
    }

    @Override
    public AngularGaugeChartModelImpl setArrows(List<GaugeArrow> arrows) {
        this.arrows = arrows;
        return this;
    }

    @Override
    public AngularGaugeChartModelImpl addArrows(GaugeArrow... arrows) {
        if (arrows != null) {
            if (this.arrows == null) {
                this.arrows = new ArrayList<>();
            }
            this.arrows.addAll(Arrays.asList(arrows));
        }
        return this;
    }

    @Override
    public List<GaugeAxis> getAxes() {
        return axes;
    }

    @Override
    public AngularGaugeChartModelImpl setAxes(List<GaugeAxis> axes) {
        this.axes = axes;
        return this;
    }

    @Override
    public AngularGaugeChartModelImpl addAxes(GaugeAxis... axes) {
        if (axes != null) {
            if (this.axes == null) {
                this.axes = new ArrayList<>();
            }
            this.axes.addAll(Arrays.asList(axes));
        }
        return this;
    }

    @Override
    public Boolean getClockWiseOnly() {
        return clockWiseOnly;
    }

    @Override
    public AngularGaugeChartModelImpl setClockWiseOnly(Boolean clockWiseOnly) {
        this.clockWiseOnly = clockWiseOnly;
        return this;
    }

    @Override
    public Double getFaceAlpha() {
        return faceAlpha;
    }

    @Override
    public AngularGaugeChartModelImpl setFaceAlpha(Double faceAlpha) {
        this.faceAlpha = faceAlpha;
        return this;
    }

    @Override
    public Double getFaceBorderAlpha() {
        return faceBorderAlpha;
    }

    @Override
    public AngularGaugeChartModelImpl setFaceBorderAlpha(Double faceBorderAlpha) {
        this.faceBorderAlpha = faceBorderAlpha;
        return this;
    }

    @Override
    public Color getFaceBorderColor() {
        return faceBorderColor;
    }

    @Override
    public AngularGaugeChartModelImpl setFaceBorderColor(Color faceBorderColor) {
        this.faceBorderColor = faceBorderColor;
        return this;
    }

    @Override
    public Integer getFaceBorderWidth() {
        return faceBorderWidth;
    }

    @Override
    public AngularGaugeChartModelImpl setFaceBorderWidth(Integer faceBorderWidth) {
        this.faceBorderWidth = faceBorderWidth;
        return this;
    }

    @Override
    public Color getFaceColor() {
        return faceColor;
    }

    @Override
    public AngularGaugeChartModelImpl setFaceColor(Color faceColor) {
        this.faceColor = faceColor;
        return this;
    }

    @Override
    public Pattern getFacePattern() {
        return facePattern;
    }

    @Override
    public AngularGaugeChartModelImpl setFacePattern(Pattern facePattern) {
        this.facePattern = facePattern;
        return this;
    }

    @Override
    public String getGaugeX() {
        return gaugeX;
    }

    @Override
    public AngularGaugeChartModelImpl setGaugeX(String gaugeX) {
        this.gaugeX = gaugeX;
        return this;
    }

    @Override
    public String getGaugeY() {
        return gaugeY;
    }

    @Override
    public AngularGaugeChartModelImpl setGaugeY(String gaugeY) {
        this.gaugeY = gaugeY;
        return this;
    }

    @Override
    public Integer getMarginBottom() {
        return marginBottom;
    }

    @Override
    public AngularGaugeChartModelImpl setMarginBottom(Integer marginBottom) {
        this.marginBottom = marginBottom;
        return this;
    }

    @Override
    public Integer getMarginLeft() {
        return marginLeft;
    }

    @Override
    public AngularGaugeChartModelImpl setMarginLeft(Integer marginLeft) {
        this.marginLeft = marginLeft;
        return this;
    }

    @Override
    public Integer getMarginRight() {
        return marginRight;
    }

    @Override
    public AngularGaugeChartModelImpl setMarginRight(Integer marginRight) {
        this.marginRight = marginRight;
        return this;
    }

    @Override
    public Integer getMarginTop() {
        return marginTop;
    }

    @Override
    public AngularGaugeChartModelImpl setMarginTop(Integer marginTop) {
        this.marginTop = marginTop;
        return this;
    }

    @Override
    public Integer getMinRadius() {
        return minRadius;
    }

    @Override
    public AngularGaugeChartModelImpl setMinRadius(Integer minRadius) {
        this.minRadius = minRadius;
        return this;
    }

    @Override
    public Double getStartDuration() {
        return startDuration;
    }

    @Override
    public AngularGaugeChartModelImpl setStartDuration(Double startDuration) {
        this.startDuration = startDuration;
        return this;
    }

    @Override
    public AnimationEffect getStartEffect() {
        return startEffect;
    }

    @Override
    public AngularGaugeChartModelImpl setStartEffect(AnimationEffect startEffect) {
        this.startEffect = startEffect;
        return this;
    }
}