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


import io.jmix.charts.component.SerialChart;
import org.dom4j.Element;

public class SerialChartLoader extends AbstractSerialChartLoader<SerialChart> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(SerialChart.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadBezierX(resultComponent, element);
        loadBezierY(resultComponent, element);
    }

    private void loadBezierX(SerialChart serialChart, Element element) {
        String bezierX = element.attributeValue("bezierX");
        if (bezierX != null && !bezierX.isEmpty()) {
            serialChart.setBezierX(Integer.valueOf(bezierX));
        }
    }

    private void loadBezierY(SerialChart serialChart, Element element) {
        String bezierY = element.attributeValue("bezierY");
        if (bezierY != null && !bezierY.isEmpty()) {
            serialChart.setBezierY(Integer.valueOf(bezierY));
        }
    }
}