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

package io.jmix.charts.widget.amcharts.serialization;


import io.jmix.charts.model.chart.impl.AbstractChart;

public interface ChartSerializer extends HasDataItemKeyMapper {

    String serialize(AbstractChart chart);

    String serializeChanges(AbstractChart chart, ChartIncrementalChanges changes);

    String toJson(Object value);
}
