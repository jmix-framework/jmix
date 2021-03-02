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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import io.jmix.ui.data.DataItem;
import io.jmix.charts.model.chart.impl.ChartModelImpl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ChartJsonSerializationContext implements JsonSerializationContext {

    public static final String DEFAULT_JS_DATE_FORMAT = "YYYY-MM-DD JJ:NN:SS:QQQ";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss:S";
    public static final String DEFAULT_DATE_TIME_FORMAT = DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT;

    protected Gson gson;
    protected ChartModelImpl chartModel;
    protected Function<DataItem, String> itemKeyMapper;

    public ChartJsonSerializationContext(Gson gson, ChartModelImpl chartModel, Function<DataItem, String> itemKeyMapper) {
        this.gson = gson;
        this.chartModel = chartModel;
        this.itemKeyMapper = itemKeyMapper;
    }

    @Override
    public JsonElement serialize(Object src) {
        return gson.toJsonTree(src);
    }

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc) {
        return gson.toJsonTree(src, typeOfSrc);
    }

    public ChartModelImpl getChartModel() {
        return chartModel;
    }

    public Function<DataItem, String> getItemKeyMapper() {
        return itemKeyMapper;
    }

    public List<String> getProperties() {
        List<String> properties = new ArrayList<>();
        for (String property : chartModel.getWiredFields()) {
            if (!properties.contains(property)) {
                properties.add(property);
            }
        }
        return properties;
    }
}