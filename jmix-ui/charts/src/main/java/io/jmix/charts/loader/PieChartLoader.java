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


import io.jmix.charts.component.PieChart;
import io.jmix.charts.model.GradientType;
import io.jmix.charts.model.JsFunction;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class PieChartLoader extends SlicedChartLoader<PieChart> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(PieChart.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadConfiguration(resultComponent, element);
    }

    @Override
    protected void loadConfiguration(PieChart chart, Element element) {
        super.loadConfiguration(chart, element);

        String adjustPrecision = element.attributeValue("adjustPrecision");
        if (StringUtils.isNotEmpty(adjustPrecision)) {
            chart.setAdjustPrecision(Boolean.valueOf(adjustPrecision));
        }

        String angle = element.attributeValue("angle");
        if (StringUtils.isNotEmpty(angle)) {
            chart.setAngle(Integer.valueOf(angle));
        }

        String balloonFunction = element.elementText("balloonFunction");
        if (StringUtils.isNotEmpty(balloonFunction)) {
            chart.setBalloonFunction(new JsFunction(balloonFunction));
        }

        String balloonText = element.attributeValue("balloonText");
        if (StringUtils.isNotEmpty(balloonText)) {
            chart.setBalloonText(loadResourceString(balloonText));
        }

        String depth3D = element.attributeValue("depth3D");
        if (StringUtils.isNotEmpty(depth3D)) {
            chart.setDepth3D(Integer.valueOf(depth3D));
        }

        String gradientType = element.attributeValue("gradientType");
        if (StringUtils.isNotEmpty(gradientType)) {
            chart.setGradientType(GradientType.valueOf("gradientType"));
        }

        String innerRadius = element.attributeValue("innerRadius");
        if (StringUtils.isNotEmpty(innerRadius)) {
            chart.setInnerRadius(innerRadius);
        }

        String labelRadius = element.attributeValue("labelRadius");
        if (StringUtils.isNotEmpty(labelRadius)) {
            chart.setLabelRadius(Integer.valueOf(labelRadius));
        }

        String labelRadiusField = element.attributeValue("labelRadiusField");
        if (StringUtils.isNotEmpty(labelRadiusField)) {
            chart.setLabelRadiusField(labelRadiusField);
        }

        String labelText = element.attributeValue("labelText");
        if (StringUtils.isNotEmpty(labelText)) {
            chart.setLabelText(loadResourceString(labelText));
        }

        String minRadius = element.attributeValue("minRadius");
        if (StringUtils.isNotEmpty(minRadius)) {
            chart.setMinRadius(Integer.valueOf(minRadius));
        }

        String pieAlpha = element.attributeValue("pieAlpha");
        if (StringUtils.isNotEmpty(pieAlpha)) {
            chart.setPieAlpha(Double.valueOf(pieAlpha));
        }

        String pieX = element.attributeValue("pieX");
        if (StringUtils.isNotEmpty(pieX)) {
            chart.setPieX(pieX);
        }

        String pieY = element.attributeValue("pieY");
        if (StringUtils.isNotEmpty(pieY)) {
            chart.setPieY(pieY);
        }

        String pullOutRadius = element.attributeValue("pullOutRadius");
        if (StringUtils.isNotEmpty(pullOutRadius)) {
            chart.setPullOutRadius(pullOutRadius);
        }

        String radius = element.attributeValue("radius");
        if (StringUtils.isNotEmpty(radius)) {
            chart.setRadius(radius);
        }

        String startAngle = element.attributeValue("startAngle");
        if (StringUtils.isNotEmpty(startAngle)) {
            chart.setStartAngle(Integer.valueOf(startAngle));
        }

        String startRadius = element.attributeValue("startRadius");
        if (StringUtils.isNotEmpty(startRadius)) {
            chart.setStartRadius(startRadius);
        }
    }
}