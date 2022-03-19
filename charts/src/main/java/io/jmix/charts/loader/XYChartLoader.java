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

package io.jmix.charts.loader;


import io.jmix.charts.component.XYChart;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class XYChartLoader extends RectangularChartLoader<XYChart> {
    @Override
    public void createComponent() {
        resultComponent = factory.create(XYChart.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadConfiguration(resultComponent, element);
    }

    @Override
    protected void loadConfiguration(XYChart chart, Element element) {
        super.loadConfiguration(chart, element);

        String dataDateFormat = element.attributeValue("dataDateFormat");
        if (StringUtils.isNotEmpty(dataDateFormat)) {
            chart.setDataDateFormat(dataDateFormat);
        }

        String hideXScrollbar = element.attributeValue("hideXScrollbar");
        if (StringUtils.isNotEmpty(hideXScrollbar)) {
            chart.setHideXScrollbar(Boolean.valueOf(hideXScrollbar));
        }

        String hideYScrollbar = element.attributeValue("hideYScrollbar");
        if (StringUtils.isNotEmpty(hideYScrollbar)) {
            chart.setHideYScrollbar(Boolean.valueOf(hideYScrollbar));
        }

        String maxValue = element.attributeValue("maxValue");
        if (StringUtils.isNotEmpty(maxValue)) {
            chart.setMaxValue(Integer.valueOf(maxValue));
        }

        String minValue = element.attributeValue("minValue");
        if (StringUtils.isNotEmpty(minValue)) {
            chart.setMinValue(Integer.valueOf(minValue));
        }
    }
}