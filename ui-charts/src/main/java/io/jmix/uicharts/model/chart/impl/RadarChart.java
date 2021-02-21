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

import io.jmix.uicharts.model.chart.ChartType;
import io.jmix.uicharts.model.chart.RadarChartModel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * See documentation for properties of AmRadarChart JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmRadarChart">http://docs.amcharts.com/3/javascriptcharts/AmRadarChart</a>
 */
public class RadarChart extends CoordinateChart<RadarChart> implements RadarChartModel<RadarChart> {

    private static final long serialVersionUID = 7721119324768771106L;

    private String categoryField;

    private Integer marginBottom;

    private Integer marginLeft;

    private Integer marginRight;

    private Integer marginTop;

    private String radius;

    public RadarChart() {
        super(ChartType.RADAR);
    }

    @Override
    public String getCategoryField() {
        return categoryField;
    }

    @Override
    public RadarChart setCategoryField(String categoryField) {
        this.categoryField = categoryField;
        return this;
    }

    @Override
    public Integer getMarginBottom() {
        return marginBottom;
    }

    @Override
    public RadarChart setMarginBottom(Integer marginBottom) {
        this.marginBottom = marginBottom;
        return this;
    }

    @Override
    public Integer getMarginLeft() {
        return marginLeft;
    }

    @Override
    public RadarChart setMarginLeft(Integer marginLeft) {
        this.marginLeft = marginLeft;
        return this;
    }

    @Override
    public Integer getMarginRight() {
        return marginRight;
    }

    @Override
    public RadarChart setMarginRight(Integer marginRight) {
        this.marginRight = marginRight;
        return this;
    }

    @Override
    public Integer getMarginTop() {
        return marginTop;
    }

    @Override
    public RadarChart setMarginTop(Integer marginTop) {
        this.marginTop = marginTop;
        return this;
    }

    @Override
    public String getRadius() {
        return radius;
    }

    @Override
    public RadarChart setRadius(String radius) {
        this.radius = radius;
        return this;
    }

    @Override
    public List<String> getWiredFields() {
        List<String> wiredFields = new ArrayList<>(super.getWiredFields());

        if (StringUtils.isNotEmpty(categoryField)) {
            wiredFields.add(categoryField);
        }

        return wiredFields;
    }
}