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
import io.jmix.charts.model.chart.impl.StockChartGroup;
import io.jmix.charts.model.dataset.DataSet;
import io.jmix.charts.model.settings.Rule;
import io.jmix.charts.widget.amcharts.serialization.*;
import io.jmix.ui.data.DataItem;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component("ui_JmixStockChartSerializer")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixStockChartSerializer implements StockChartSerializer {

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

    protected static void setDefaultProperties(GsonBuilder builder) {
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

    private final Logger logger = LoggerFactory.getLogger(JmixStockChartSerializer.class);

    @Autowired
    protected void setItemsSerializer(ChartDataItemsSerializer chartDataItemsSerializer) {
        this.itemsSerializer = chartDataItemsSerializer;
    }

    @Override
    public String serialize(StockChartGroup chart) {
        JsonElement jsonTree = chartGson.toJsonTree(chart);

        if (CollectionUtils.isNotEmpty(chart.getDataSets())) {
            ChartJsonSerializationContext context = createChartJsonSerializationContext(chart);

            JsonArray jsonDataSets = (JsonArray) jsonTree.getAsJsonObject().get("dataSets");
            for (JsonElement dataSetElement : jsonDataSets) {
                JsonObject dataSetObject = (JsonObject) dataSetElement;
                String dataSetId = dataSetObject.get("id").getAsString();
                DataSet dataSet = chart.getDataSet(dataSetId);
                if (dataSet != null && dataSet.getDataProvider() != null) {
                    JsonArray dataProviderElement = itemsSerializer.serialize(dataSet.getDataProvider().getItems(), context);

                    // Prevent errors on client for empty data provider
                    if (dataProviderElement.size() == 0) {
                        dataProviderElement.add(new JsonObject());
                    }

                    dataSetObject.add("dataProvider", dataProviderElement);
                }
            }
        }

        return chartGson.toJson(jsonTree);
    }

    @Override
    public String serializeChanges(StockChartGroup chart, Map<DataSet, ChartIncrementalChanges> changedItems) {
        JsonObject jsonChangedDataSetElement = new JsonObject();

        ChartJsonSerializationContext context = createChartJsonSerializationContext(chart);

        for (Map.Entry<DataSet, ChartIncrementalChanges> changesEntry : changedItems.entrySet()) {
            JsonObject jsonChangedItemsElement = new JsonObject();

            ChartIncrementalChanges changes = changesEntry.getValue();
            if (changes.getAddedItems() != null) {
                jsonChangedItemsElement.add("add", itemsSerializer.serialize(changes.getAddedItems(), context));
            }
            if (changes.getRemovedItems() != null) {
                jsonChangedItemsElement.add("remove", itemsSerializer.serialize(changes.getRemovedItems(), context));
            }
            if (changes.getUpdatedItems() != null) {
                jsonChangedItemsElement.add("update", itemsSerializer.serialize(changes.getUpdatedItems(), context));
            }

            String dataSetId = changesEntry.getKey().getId();
            if (dataSetId != null) {
                jsonChangedDataSetElement.add(dataSetId, jsonChangedItemsElement);
            } else {
                logger.warn("DataSet of StockChart does not have id. Incremental updated will not be performed.");
            }
        }

        return chartGson.toJson(jsonChangedDataSetElement);
    }

    protected ChartJsonSerializationContext createChartJsonSerializationContext(StockChartGroup chart) {
        if (itemKeyMapper == null) {
            throw new IllegalStateException("itemKeyMapper can't be null");
        }
        return new ChartJsonSerializationContext(chartGson, chart, itemKeyMapper);
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