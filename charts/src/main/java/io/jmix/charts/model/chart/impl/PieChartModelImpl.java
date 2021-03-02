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


import io.jmix.charts.model.GradientType;
import io.jmix.charts.model.chart.ChartType;
import io.jmix.charts.model.chart.PieChartModel;
import io.jmix.charts.model.JsFunction;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * See documentation for properties of AmPieChart JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmPieChart">http://docs.amcharts.com/3/javascriptcharts/AmPieChart</a>
 */
public class PieChartModelImpl extends SlicedChartModelImpl<PieChartModelImpl> implements PieChartModel<PieChartModelImpl> {

    private static final long serialVersionUID = 7721119324768771106L;

    private Boolean adjustPrecision;

    private Integer angle;

    private JsFunction balloonFunction;

    private String balloonText;

    private Integer depth3D;

    private GradientType gradientType;

    private String innerRadius;

    private Integer labelRadius;

    private String labelRadiusField;

    private String labelText;

    private Integer minRadius;

    private Double pieAlpha;

    private String pieX;

    private String pieY;

    private String pullOutRadius;

    private String radius;

    private Integer startAngle;

    private String startRadius;

    public PieChartModelImpl() {
        super(ChartType.PIE);
    }

    @Override
    public Integer getAngle() {
        return angle;
    }

    @Override
    public PieChartModelImpl setAngle(Integer angle) {
        this.angle = angle;
        return this;
    }

    @Override
    public String getBalloonText() {
        return balloonText;
    }

    @Override
    public PieChartModelImpl setBalloonText(String balloonText) {
        this.balloonText = balloonText;
        return this;
    }

    @Override
    public Integer getDepth3D() {
        return depth3D;
    }

    @Override
    public PieChartModelImpl setDepth3D(Integer depth3D) {
        this.depth3D = depth3D;
        return this;
    }

    @Override
    public String getInnerRadius() {
        return innerRadius;
    }

    @Override
    public PieChartModelImpl setInnerRadius(String innerRadius) {
        this.innerRadius = innerRadius;
        return this;
    }

    @Override
    public Integer getLabelRadius() {
        return labelRadius;
    }

    @Override
    public PieChartModelImpl setLabelRadius(Integer labelRadius) {
        this.labelRadius = labelRadius;
        return this;
    }

    @Override
    public String getLabelRadiusField() {
        return labelRadiusField;
    }

    @Override
    public PieChartModelImpl setLabelRadiusField(String labelRadiusField) {
        this.labelRadiusField = labelRadiusField;
        return this;
    }

    @Override
    public String getLabelText() {
        return labelText;
    }

    @Override
    public PieChartModelImpl setLabelText(String labelText) {
        this.labelText = labelText;
        return this;
    }

    @Override
    public Integer getMinRadius() {
        return minRadius;
    }

    @Override
    public PieChartModelImpl setMinRadius(Integer minRadius) {
        this.minRadius = minRadius;
        return this;
    }

    @Override
    public Double getPieAlpha() {
        return pieAlpha;
    }

    @Override
    public PieChartModelImpl setPieAlpha(Double pieAlpha) {
        this.pieAlpha = pieAlpha;
        return this;
    }

    @Override
    public String getPieX() {
        return pieX;
    }

    @Override
    public PieChartModelImpl setPieX(String pieX) {
        this.pieX = pieX;
        return this;
    }

    @Override
    public String getPieY() {
        return pieY;
    }

    @Override
    public PieChartModelImpl setPieY(String pieY) {
        this.pieY = pieY;
        return this;
    }

    @Override
    public String getPullOutRadius() {
        return pullOutRadius;
    }

    @Override
    public PieChartModelImpl setPullOutRadius(String pullOutRadius) {
        this.pullOutRadius = pullOutRadius;
        return this;
    }

    @Override
    public String getRadius() {
        return radius;
    }

    @Override
    public PieChartModelImpl setRadius(String radius) {
        this.radius = radius;
        return this;
    }

    @Override
    public Integer getStartAngle() {
        return startAngle;
    }

    @Override
    public PieChartModelImpl setStartAngle(Integer startAngle) {
        this.startAngle = startAngle;
        return this;
    }

    @Override
    public String getStartRadius() {
        return startRadius;
    }

    @Override
    public PieChartModelImpl setStartRadius(String startRadius) {
        this.startRadius = startRadius;
        return this;
    }

    @Override
    public Boolean getAdjustPrecision() {
        return adjustPrecision;
    }

    @Override
    public PieChartModelImpl setAdjustPrecision(Boolean adjustPrecision) {
        this.adjustPrecision = adjustPrecision;
        return this;
    }

    @Override
    public JsFunction getBalloonFunction() {
        return balloonFunction;
    }

    @Override
    public PieChartModelImpl setBalloonFunction(JsFunction balloonFunction) {
        this.balloonFunction = balloonFunction;
        return this;
    }

    @Override
    public GradientType getGradientType() {
        return gradientType;
    }

    @Override
    public PieChartModelImpl setGradientType(GradientType gradientType) {
        this.gradientType = gradientType;
        return this;
    }

    @Override
    public List<String> getWiredFields() {
        List<String> wiredFields = new ArrayList<>(super.getWiredFields());

        if (StringUtils.isNotEmpty(labelRadiusField)) {
            wiredFields.add(labelRadiusField);
        }

        return wiredFields;
    }
}