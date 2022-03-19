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

package io.jmix.charts.serialization;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import io.jmix.charts.model.Color;
import io.jmix.charts.model.JsFunction;
import io.jmix.charts.model.JsonEnum;
import io.jmix.charts.model.chart.impl.AbstractChart;
import io.jmix.charts.model.chart.impl.GanttChartModelImpl;
import io.jmix.charts.model.settings.Rule;
import io.jmix.charts.widget.amcharts.serialization.*;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.DataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component("ui_JmixChartSerializer")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixChartSerializer implements ChartSerializer {

    protected final static Gson chartGson;

    static {
        // GSON is thread safe so we can use shared GSON instance
        chartGson = createChartGsonBuilder().create();
    }

    private static GsonBuilder createChartGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        builder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                Expose expose = f.getAnnotation(Expose.class);
                return expose != null && !expose.serialize() || "dataProvider".equals(f.getName());
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        });
        setDefaultProperties(builder);
        return builder;
    }

    private static void setDefaultProperties(GsonBuilder builder) {
        // uncomment if you wish to debug generated json
        // builder.setPrettyPrinting();
        builder.registerTypeHierarchyAdapter(JsonEnum.class, new JsonEnumSerializer());
        builder.registerTypeHierarchyAdapter(Color.class, new ColorSerializer());
        builder.registerTypeHierarchyAdapter(JsFunction.class, new JsFunctionSerializer());
        builder.registerTypeHierarchyAdapter(Date.class, new ConfigDateSerializer());
        builder.registerTypeHierarchyAdapter(Rule.class, new ResponsiveRuleSerializer());
    }

    protected Function<DataItem, String> itemKeyMapper;
    protected ChartDataItemsSerializer itemsSerializer;

    @Autowired
    protected void setItemsSerializer(ChartDataItemsSerializer chartDataItemsSerializer) {
        this.itemsSerializer = chartDataItemsSerializer;
    }

    @Override
    public String serialize(AbstractChart chart) {
        JsonElement jsonTree = chartGson.toJsonTree(chart);

        ChartJsonSerializationContext context = createChartJsonSerializationContext(chart);
        DataProvider dataProvider = chart.getDataProvider();
        if (dataProvider != null) {
            JsonArray dataProviderElement = itemsSerializer.serialize(dataProvider.getItems(), context);

            // Prevent errors on client for empty data provider
            if (dataProviderElement.size() == 0) {
                dataProviderElement.add(new JsonObject());
            }

            jsonTree.getAsJsonObject().add("dataProvider", dataProviderElement);
        }

        beforeConvertToJson(jsonTree, context);

        return chartGson.toJson(jsonTree);
    }

    protected void beforeConvertToJson(JsonElement jsonTree, ChartJsonSerializationContext context) {
        // By default, export plugin exports all data fields.
        // We need to exclude service fields like '$k' and '$i'.
        if (jsonTree.getAsJsonObject().has("export")) {
            JsonObject export = jsonTree.getAsJsonObject().getAsJsonObject("export");
            JsonArray exportFields = new JsonArray();

            for (String property : context.getProperties()) {
                exportFields.add(property);
            }

            if (context.getChartModel() instanceof GanttChartModelImpl) {
                for (String field : context.getSegmentFields()) {
                    exportFields.add(field);
                }
            }

            export.add("exportFields", exportFields);
        }
    }

    @Override
    public String serializeChanges(AbstractChart chart, ChartIncrementalChanges changes) {
        JsonObject jsonChangedItemsElement = new JsonObject();

        ChartJsonSerializationContext context = createChartJsonSerializationContext(chart);

        if (changes.getAddedItems() != null) {
            jsonChangedItemsElement.add("add", itemsSerializer.serialize(changes.getAddedItems(), context));
        }
        if (changes.getRemovedItems() != null) {
            jsonChangedItemsElement.add("remove", itemsSerializer.serialize(changes.getRemovedItems(), context));
        }
        if (changes.getUpdatedItems() != null) {
            jsonChangedItemsElement.add("update", itemsSerializer.serialize(changes.getUpdatedItems(), context));
        }

        return chartGson.toJson(jsonChangedItemsElement);
    }

    protected ChartJsonSerializationContext createChartJsonSerializationContext(AbstractChart chart) {
        if (itemKeyMapper == null) {
            throw new IllegalStateException("itemKeyMapper can't be null");
        }
        return new ChartJsonSerializationContext(chartGson, chart, itemKeyMapper);
    }

    @Override
    public String toJson(Object value) {
        return chartGson.toJson(value);
    }

    @Override
    public Function<DataItem, String> getDataItemKeyMapper() {
        return this.itemKeyMapper;
    }

    @Override
    public void setDataItemKeyMapper(Function<DataItem, String> itemKeyMapper) {
        this.itemKeyMapper = itemKeyMapper;
    }
}