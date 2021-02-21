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

package io.jmix.uicharts.component.impl;


import io.jmix.uicharts.component.PieChart;
import io.jmix.uicharts.model.GradientType;
import io.jmix.uichartscommon.charts.model.JsFunction;

public class PieChartImpl extends SlicedChartImpl<PieChart, io.jmix.uicharts.model.chart.impl.PieChart>
        implements io.jmix.uicharts.component.PieChart {

    @Override
    protected io.jmix.uicharts.model.chart.impl.PieChart createChartConfiguration() {
        return new io.jmix.uicharts.model.chart.impl.PieChart();
    }

    @Override
    public Integer getAngle() {
        return getModel().getAngle();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setAngle(Integer angle) {
        getModel().setAngle(angle);
        return this;
    }

    @Override
    public String getBalloonText() {
        return getModel().getBalloonText();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setBalloonText(String balloonText) {
        getModel().setBalloonText(balloonText);
        return this;
    }

    @Override
    public Integer getDepth3D() {
        return getModel().getDepth3D();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setDepth3D(Integer depth3D) {
        getModel().setDepth3D(depth3D);
        return this;
    }

    @Override
    public String getInnerRadius() {
        return getModel().getInnerRadius();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setInnerRadius(String innerRadius) {
        getModel().setInnerRadius(innerRadius);
        return this;
    }

    @Override
    public Integer getLabelRadius() {
        return getModel().getLabelRadius();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setLabelRadius(Integer labelRadius) {
        getModel().setLabelRadius(labelRadius);
        return this;
    }

    @Override
    public String getLabelRadiusField() {
        return getModel().getLabelRadiusField();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setLabelRadiusField(String labelRadiusField) {
        getModel().setLabelRadiusField(labelRadiusField);
        return this;
    }

    @Override
    public String getLabelText() {
        return getModel().getLabelText();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setLabelText(String labelText) {
        getModel().setLabelText(labelText);
        return this;
    }

    @Override
    public Integer getMinRadius() {
        return getModel().getMinRadius();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setMinRadius(Integer minRadius) {
        getModel().setMinRadius(minRadius);
        return this;
    }

    @Override
    public Double getPieAlpha() {
        return getModel().getPieAlpha();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setPieAlpha(Double pieAlpha) {
        getModel().setPieAlpha(pieAlpha);
        return this;
    }

    @Override
    public String getPieX() {
        return getModel().getPieX();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setPieX(String pieX) {
        getModel().setPieX(pieX);
        return this;
    }

    @Override
    public String getPieY() {
        return getModel().getPieY();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setPieY(String pieY) {
        getModel().setPieY(pieY);
        return this;
    }

    @Override
    public String getPullOutRadius() {
        return getModel().getPullOutRadius();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setPullOutRadius(String pullOutRadius) {
        getModel().setPullOutRadius(pullOutRadius);
        return this;
    }

    @Override
    public String getRadius() {
        return getModel().getRadius();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setRadius(String radius) {
        getModel().setRadius(radius);
        return this;
    }

    @Override
    public Integer getStartAngle() {
        return getModel().getStartAngle();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setStartAngle(Integer startAngle) {
        getModel().setStartAngle(startAngle);
        return this;
    }

    @Override
    public String getStartRadius() {
        return getModel().getStartRadius();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setStartRadius(String startRadius) {
        getModel().setStartRadius(startRadius);
        return this;
    }

    @Override
    public Boolean getAdjustPrecision() {
        return getModel().getAdjustPrecision();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setAdjustPrecision(Boolean adjustPrecision) {
        getModel().setAdjustPrecision(adjustPrecision);
        return this;
    }

    @Override
    public JsFunction getBalloonFunction() {
        return getModel().getBalloonFunction();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setBalloonFunction(JsFunction balloonFunction) {
        getModel().setBalloonFunction(balloonFunction);
        return this;
    }

    @Override
    public GradientType getGradientType() {
        return getModel().getGradientType();
    }

    @Override
    public io.jmix.uicharts.component.PieChart setGradientType(GradientType gradientType) {
        getModel().setGradientType(gradientType);
        return this;
    }
}