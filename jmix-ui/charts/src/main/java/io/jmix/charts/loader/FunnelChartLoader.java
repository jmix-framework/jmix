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


import io.jmix.charts.component.FunnelChart;
import io.jmix.charts.model.FunnelValueRepresentation;
import io.jmix.charts.model.label.LabelPosition;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class FunnelChartLoader extends SlicedChartLoader<FunnelChart> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(FunnelChart.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadConfiguration(resultComponent, element);
    }

    @Override
    protected void loadConfiguration(FunnelChart chart, Element element) {
        super.loadConfiguration(chart, element);

        String angle = element.attributeValue("angle");
        if (StringUtils.isNotEmpty(angle)) {
            chart.setAngle(Integer.valueOf(angle));
        }

        String balloonText = element.attributeValue("balloonText");
        if (StringUtils.isNotEmpty(balloonText)) {
            chart.setBalloonText(loadResourceString(balloonText));
        }

        String baseWidth = element.attributeValue("baseWidth");
        if (StringUtils.isNotEmpty(baseWidth)) {
            chart.setBaseWidth(baseWidth);
        }

        String depth3D = element.attributeValue("depth3D");
        if (StringUtils.isNotEmpty(depth3D)) {
            chart.setDepth3D(Integer.valueOf(depth3D));
        }

        String labelPosition = element.attributeValue("labelPosition");
        if (StringUtils.isNotEmpty(labelPosition)) {
            chart.setLabelPosition(LabelPosition.valueOf(labelPosition));
        }

        String labelText = element.attributeValue("labelText");
        if (StringUtils.isNotEmpty(labelText)) {
            chart.setLabelText(loadResourceString(labelText));
        }

        String neckHeight = element.attributeValue("neckHeight");
        if (StringUtils.isNotEmpty(neckHeight)) {
            chart.setNeckHeight(neckHeight);
        }

        String neckWidth = element.attributeValue("neckWidth");
        if (StringUtils.isNotEmpty(neckWidth)) {
            chart.setNeckWidth(neckWidth);
        }

        String pullDistance = element.attributeValue("pullDistance");
        if (StringUtils.isNotEmpty(pullDistance)) {
            chart.setPullDistance(pullDistance);
        }

        String rotate = element.attributeValue("rotate");
        if (StringUtils.isNotEmpty(rotate)) {
            chart.setRotate(Boolean.valueOf(rotate));
        }

        String startX = element.attributeValue("startX");
        if (StringUtils.isNotEmpty(startX)) {
            chart.setStartX(Integer.valueOf(startX));
        }

        String startY = element.attributeValue("startY");
        if (StringUtils.isNotEmpty(startY)) {
            chart.setStartY(Integer.valueOf(startY));
        }

        String valueRepresents = element.attributeValue("valueRepresents");
        if (StringUtils.isNotEmpty(valueRepresents)) {
            chart.setValueRepresents(FunnelValueRepresentation.valueOf(valueRepresents));
        }
    }
}