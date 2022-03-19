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


import io.jmix.charts.component.XYChart;
import io.jmix.charts.model.chart.impl.XYChartModelImpl;
import io.jmix.charts.widget.amcharts.serialization.ChartJsonSerializationContext;

public class XYChartImpl extends RectangularChartImpl<XYChart, XYChartModelImpl>
        implements XYChart {

    @Override
    protected XYChartModelImpl createChartConfiguration() {
        return new XYChartModelImpl();
    }

    @Override
    protected void setupDefaults(XYChartModelImpl chart) {
        super.setupDefaults(chart);

        setupXYChartDefaults(chart);
    }

    protected void setupXYChartDefaults(XYChartModelImpl chart) {
        chart.setDataDateFormat(ChartJsonSerializationContext.DEFAULT_JS_DATE_FORMAT);
    }

    @Override
    public Boolean getHideXScrollbar() {
        return getModel().getHideXScrollbar();
    }

    @Override
    public XYChart setHideXScrollbar(Boolean hideXScrollbar) {
        getModel().setHideXScrollbar(hideXScrollbar);
        return this;
    }

    @Override
    public Boolean getHideYScrollbar() {
        return getModel().getHideYScrollbar();
    }

    @Override
    public XYChart setHideYScrollbar(Boolean hideYScrollbar) {
        getModel().setHideYScrollbar(hideYScrollbar);
        return this;
    }

    @Override
    public String getDataDateFormat() {
        return getModel().getDataDateFormat();
    }

    @Override
    public XYChart setDataDateFormat(String dataDateFormat) {
        getModel().setDataDateFormat(dataDateFormat);
        return this;
    }

    @Override
    public Integer getMaxValue() {
        return getModel().getMaxValue();
    }

    @Override
    public XYChart setMaxValue(Integer maxValue) {
        getModel().setMaxValue(maxValue);
        return this;
    }

    @Override
    public Integer getMinValue() {
        return getModel().getMinValue();
    }

    @Override
    public XYChart setMinValue(Integer minValue) {
        getModel().setMinValue(minValue);
        return this;
    }
}