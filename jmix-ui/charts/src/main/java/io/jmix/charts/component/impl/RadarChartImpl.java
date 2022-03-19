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


import io.jmix.charts.component.RadarChart;
import io.jmix.charts.model.chart.impl.RadarChartModelImpl;

public class RadarChartImpl extends CoordinateChartImpl<RadarChart, RadarChartModelImpl>
        implements RadarChart {

    @Override
    protected RadarChartModelImpl createChartConfiguration() {
        return new RadarChartModelImpl();
    }

    @Override
    public String getCategoryField() {
        return getModel().getCategoryField();
    }

    @Override
    public RadarChart setCategoryField(String categoryField) {
        getModel().setCategoryField(categoryField);
        return this;
    }

    @Override
    public Integer getMarginBottom() {
        return getModel().getMarginBottom();
    }

    @Override
    public RadarChart setMarginBottom(Integer marginBottom) {
        getModel().setMarginBottom(marginBottom);
        return this;
    }

    @Override
    public Integer getMarginLeft() {
        return getModel().getMarginLeft();
    }

    @Override
    public RadarChart setMarginLeft(Integer marginLeft) {
        getModel().setMarginLeft(marginLeft);
        return this;
    }

    @Override
    public Integer getMarginRight() {
        return getModel().getMarginRight();
    }

    @Override
    public RadarChart setMarginRight(Integer marginRight) {
        getModel().setMarginRight(marginRight);
        return this;
    }

    @Override
    public Integer getMarginTop() {
        return getModel().getMarginTop();
    }

    @Override
    public RadarChart setMarginTop(Integer marginTop) {
        getModel().setMarginTop(marginTop);
        return this;
    }

    @Override
    public String getRadius() {
        return getModel().getRadius();
    }

    @Override
    public RadarChart setRadius(String radius) {
        getModel().setRadius(radius);
        return this;
    }
}