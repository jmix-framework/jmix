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


import io.jmix.charts.component.RadarChart;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class RadarChartLoader extends CoordinateChartLoader<RadarChart> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(RadarChart.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadConfiguration(resultComponent, element);
    }

    @Override
    protected void loadConfiguration(RadarChart chart, Element element) {
        super.loadConfiguration(chart, element);

        String categoryField = element.attributeValue("categoryField");
        if (StringUtils.isNotEmpty(categoryField)) {
            chart.setCategoryField(categoryField);
        }

        loadMargins(chart, element);

        String radius = element.attributeValue("radius");
        if (StringUtils.isNotEmpty(radius)) {
            chart.setRadius(radius);
        }
    }
}