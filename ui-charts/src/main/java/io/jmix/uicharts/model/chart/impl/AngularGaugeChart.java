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

package io.jmix.uicharts.model.chart.impl;

import io.jmix.uicharts.model.*;
import io.jmix.uicharts.model.GaugeArrow;
import io.jmix.uicharts.model.GaugeAxis;
import io.jmix.uicharts.model.animation.AnimationEffect;
import io.jmix.uicharts.model.chart.AngularGaugeChartModel;
import io.jmix.uicharts.model.chart.ChartType;
import io.jmix.uicharts.model.graph.Pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * See documentation for properties of AmAngularGauge JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmAngularGauge">http://docs.amcharts.com/3/javascriptcharts/AmAngularGauge</a>
 */
public class AngularGaugeChart extends AbstractChart<AngularGaugeChart>
        implements AngularGaugeChartModel<AngularGaugeChart> {

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

    public AngularGaugeChart() {
        super(ChartType.GAUGE);
    }

    @Override
    public Boolean getAdjustSize() {
        return adjustSize;
    }

    @Override
    public AngularGaugeChart setAdjustSize(Boolean adjustSize) {
        this.adjustSize = adjustSize;
        return this;
    }

    @Override
    public List<GaugeArrow> getArrows() {
        return arrows;
    }

    @Override
    public AngularGaugeChart setArrows(List<GaugeArrow> arrows) {
        this.arrows = arrows;
        return this;
    }

    @Override
    public AngularGaugeChart addArrows(GaugeArrow... arrows) {
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
    public AngularGaugeChart setAxes(List<GaugeAxis> axes) {
        this.axes = axes;
        return this;
    }

    @Override
    public AngularGaugeChart addAxes(GaugeAxis... axes) {
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
    public AngularGaugeChart setClockWiseOnly(Boolean clockWiseOnly) {
        this.clockWiseOnly = clockWiseOnly;
        return this;
    }

    @Override
    public Double getFaceAlpha() {
        return faceAlpha;
    }

    @Override
    public AngularGaugeChart setFaceAlpha(Double faceAlpha) {
        this.faceAlpha = faceAlpha;
        return this;
    }

    @Override
    public Double getFaceBorderAlpha() {
        return faceBorderAlpha;
    }

    @Override
    public AngularGaugeChart setFaceBorderAlpha(Double faceBorderAlpha) {
        this.faceBorderAlpha = faceBorderAlpha;
        return this;
    }

    @Override
    public Color getFaceBorderColor() {
        return faceBorderColor;
    }

    @Override
    public AngularGaugeChart setFaceBorderColor(Color faceBorderColor) {
        this.faceBorderColor = faceBorderColor;
        return this;
    }

    @Override
    public Integer getFaceBorderWidth() {
        return faceBorderWidth;
    }

    @Override
    public AngularGaugeChart setFaceBorderWidth(Integer faceBorderWidth) {
        this.faceBorderWidth = faceBorderWidth;
        return this;
    }

    @Override
    public Color getFaceColor() {
        return faceColor;
    }

    @Override
    public AngularGaugeChart setFaceColor(Color faceColor) {
        this.faceColor = faceColor;
        return this;
    }

    @Override
    public Pattern getFacePattern() {
        return facePattern;
    }

    @Override
    public AngularGaugeChart setFacePattern(Pattern facePattern) {
        this.facePattern = facePattern;
        return this;
    }

    @Override
    public String getGaugeX() {
        return gaugeX;
    }

    @Override
    public AngularGaugeChart setGaugeX(String gaugeX) {
        this.gaugeX = gaugeX;
        return this;
    }

    @Override
    public String getGaugeY() {
        return gaugeY;
    }

    @Override
    public AngularGaugeChart setGaugeY(String gaugeY) {
        this.gaugeY = gaugeY;
        return this;
    }

    @Override
    public Integer getMarginBottom() {
        return marginBottom;
    }

    @Override
    public AngularGaugeChart setMarginBottom(Integer marginBottom) {
        this.marginBottom = marginBottom;
        return this;
    }

    @Override
    public Integer getMarginLeft() {
        return marginLeft;
    }

    @Override
    public AngularGaugeChart setMarginLeft(Integer marginLeft) {
        this.marginLeft = marginLeft;
        return this;
    }

    @Override
    public Integer getMarginRight() {
        return marginRight;
    }

    @Override
    public AngularGaugeChart setMarginRight(Integer marginRight) {
        this.marginRight = marginRight;
        return this;
    }

    @Override
    public Integer getMarginTop() {
        return marginTop;
    }

    @Override
    public AngularGaugeChart setMarginTop(Integer marginTop) {
        this.marginTop = marginTop;
        return this;
    }

    @Override
    public Integer getMinRadius() {
        return minRadius;
    }

    @Override
    public AngularGaugeChart setMinRadius(Integer minRadius) {
        this.minRadius = minRadius;
        return this;
    }

    @Override
    public Double getStartDuration() {
        return startDuration;
    }

    @Override
    public AngularGaugeChart setStartDuration(Double startDuration) {
        this.startDuration = startDuration;
        return this;
    }

    @Override
    public AnimationEffect getStartEffect() {
        return startEffect;
    }

    @Override
    public AngularGaugeChart setStartEffect(AnimationEffect startEffect) {
        this.startEffect = startEffect;
        return this;
    }
}