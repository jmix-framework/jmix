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

import io.jmix.charts.component.SeriesBasedChart;
import io.jmix.charts.model.axis.CategoryAxis;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public abstract class AbstractSerialChartLoader<T extends SeriesBasedChart> extends RectangularChartLoader<T> {

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadConfiguration(resultComponent, element);

        String byDate = element.attributeValue("byDate");
        if (StringUtils.isNotEmpty(byDate)) {
            if (resultComponent.getCategoryAxis() == null) {
                resultComponent.setCategoryAxis(new CategoryAxis());
            }

            resultComponent.getCategoryAxis().setParseDates(Boolean.valueOf(byDate));
        }
    }

    @Override
    protected void loadConfiguration(T chart, Element element) {
        super.loadConfiguration(chart, element);

        loadSeriesBasedProperties(chart, element);
    }
}