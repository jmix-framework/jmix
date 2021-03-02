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

package io.jmix.charts.model.chart;


import io.jmix.charts.model.HasMargins;

public interface RadarChartModel<T extends RadarChartModel> extends CoordinateChartModel<T>, HasMargins<T> {
    /**
     * @return category field
     */
    String getCategoryField();

    /**
     * Sets field from your data provider containing categories.
     *
     * @param categoryField category field string
     */
    T setCategoryField(String categoryField);

    /**
     * @return radius
     */
    String getRadius();

    /**
     * Sets radius of radar. If not set the default value is 35%.
     *
     * @param radius the radius
     */
    T setRadius(String radius);
}