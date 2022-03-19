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


import io.jmix.charts.component.SerialChart;
import io.jmix.charts.model.chart.impl.SerialChartModelImpl;

public class SerialChartImpl extends SeriesBasedChartImpl<SerialChart, SerialChartModelImpl>
        implements SerialChart {

    @Override
    protected SerialChartModelImpl createChartConfiguration() {
        return new SerialChartModelImpl();
    }

    @Override
    public Integer getBezierX() {
        return getModel().getBezierX();
    }

    @Override
    public void setBezierX(Integer bezierX) {
        getModel().setBezierX(bezierX);
    }

    @Override
    public Integer getBezierY() {
        return getModel().getBezierY();
    }

    @Override
    public void setBezierY(Integer bezierY) {
        getModel().setBezierY(bezierY);
    }
}