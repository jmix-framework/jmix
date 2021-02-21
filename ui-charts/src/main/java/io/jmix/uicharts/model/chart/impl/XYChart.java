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
import io.jmix.uicharts.model.chart.XYChartModel;

/**
 * See documentation for properties of AmXYChart JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmXYChart">http://docs.amcharts.com/3/javascriptcharts/AmXYChart</a>
 */
public class XYChart extends RectangularChart<XYChart> implements XYChartModel<XYChart> {

    private static final long serialVersionUID = 3259485360498054262L;

    private String dataDateFormat;

    private Boolean hideXScrollbar;

    private Boolean hideYScrollbar;

    private Integer maxValue;

    private Integer minValue;

    public XYChart() {
        super(ChartType.XY);
    }

    @Override
    public Boolean getHideXScrollbar() {
        return hideXScrollbar;
    }

    @Override
    public XYChart setHideXScrollbar(Boolean hideXScrollbar) {
        this.hideXScrollbar = hideXScrollbar;
        return this;
    }

    @Override
    public Boolean getHideYScrollbar() {
        return hideYScrollbar;
    }

    @Override
    public XYChart setHideYScrollbar(Boolean hideYScrollbar) {
        this.hideYScrollbar = hideYScrollbar;
        return this;
    }

    @Override
    public String getDataDateFormat() {
        return dataDateFormat;
    }

    @Override
    public XYChart setDataDateFormat(String dataDateFormat) {
        this.dataDateFormat = dataDateFormat;
        return this;
    }

    @Override
    public Integer getMaxValue() {
        return maxValue;
    }

    @Override
    public XYChart setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    @Override
    public Integer getMinValue() {
        return minValue;
    }

    @Override
    public XYChart setMinValue(Integer minValue) {
        this.minValue = minValue;
        return this;
    }
}