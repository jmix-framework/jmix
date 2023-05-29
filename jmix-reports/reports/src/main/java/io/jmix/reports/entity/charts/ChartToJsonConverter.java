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

package io.jmix.reports.entity.charts;

import com.google.common.base.Strings;
import com.google.gson.*;
import io.jmix.core.InstanceNameProvider;
import io.jmix.core.Entity;
import io.jmix.reports.app.EntityMap;
import io.jmix.reports.entity.charts.serialization.DateSerializer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component("report_ChartToJsonConverter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ChartToJsonConverter {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss:S";
    public static final String DEFAULT_DATE_TIME_FORMAT = DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT;

    protected static final DateTimeFormatter TEMPORAL_DATE_FORMATTER
            = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

    protected static final DateTimeFormatter TEMPORAL_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);

    @Autowired
    protected InstanceNameProvider instanceNameProvider;

    protected final static Gson gson;

    static {
        // GSON is thread safe so we can use shared GSON instance
        gson = createGsonBuilder().create();
    }

    public static Gson getSharedGson() {
        return gson;
    }

    /**
     * @return default GSON builder for configuration serializer.
     */
    public static GsonBuilder createGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        setDefaultProperties(builder);
        return builder;
    }

    private static void setDefaultProperties(GsonBuilder builder) {
        builder.registerTypeAdapter(Date.class, new DateSerializer());
    }

    protected String resultFileName;

    public ChartToJsonConverter() {
    }

    public ChartToJsonConverter(String resultFileName) {
        this.resultFileName = resultFileName;
    }

    public String convertSerialChart(SerialChartDescription description, List<Map<String, Object>> data) {
        HashMap<String, Object> chart = new HashMap<>();
        List<String> fields = new ArrayList<>();

        chart.put("type", "serial");
        chart.put("categoryField", description.getCategoryField());
        addField(fields, description.getCategoryField());
        chart.put("chartScrollbar", Collections.emptyMap());
        chart.put("valueScrollbar", Collections.emptyMap());
        exportConfig(chart);

        if (Boolean.TRUE.equals(description.getShowLegend())) {
            Map<String, Object> legend = new HashMap<>();
            legend.put("useGraphSettings", true);
            legend.put("markerSize", 10);
            chart.put("legend", legend);
        }

        HashMap<Object, Object> valueAxis = new HashMap<>();
        valueAxis.put("gridColor", "#000");
        valueAxis.put("gridAlpha", 0.1);
        valueAxis.put("dashLength", 0);
        valueAxis.put("title", description.getValueAxisCaption());
        valueAxis.put("unit", join(" ", description.getValueAxisUnits()));
        if (description.getValueStackType() != null) {
            valueAxis.put("stackType", description.getValueStackType().getId());
        }
        chart.put("valueAxes", Collections.singletonList(valueAxis));

        HashMap<Object, Object> categoryAxis = new HashMap<>();
        categoryAxis.put("title", description.getCategoryAxisCaption());
        categoryAxis.put("gridColor", "#000");
        categoryAxis.put("gridAlpha", 0.1);
        categoryAxis.put("labelRotation", description.getCategoryAxisLabelRotation());
        if (isByDate(description.getCategoryField(), data)) {
            categoryAxis.put("parseDates", true);
        }
        chart.put("categoryAxis", categoryAxis);

        ArrayList<Object> graphs = new ArrayList<>();
        chart.put("graphs", graphs);
        for (ChartSeries series : description.getSeries()) {
            HashMap<Object, Object> graph = new HashMap<>();
            graph.put("type", series.getType() != null ? series.getType().getId() : SeriesType.COLUMN);
            graph.put("valueField", series.getValueField());
            addField(fields, series.getValueField());
            if (series.getType() == SeriesType.COLUMN || series.getType() == SeriesType.STEP) {
                graph.put("fillColorsField", series.getColorField());
                addField(fields, series.getColorField());
                graph.put("fillAlphas", 0.5);
                graph.put("columnWidth", 0.4);
            } else {
                graph.put("lineColorField", series.getColorField());
                addField(fields, series.getColorField());
                graph.put("lineAlpha", 1);
                graph.put("lineThickness", 2);
            }

            graph.put("balloonText", join(series.getName(), series.getName() != null ? " : " : "", "[[value]]"));
            graph.put("title", series.getName());
            graph.put("order", series.getOrder() == null ? 1.0 : series.getOrder());

            graphs.add(graph);
        }

        sortByCategoryFieldIfNeed(data, description.getCategoryField());

        JsonElement jsonTree = gson.toJsonTree(chart);
        jsonTree.getAsJsonObject().add("dataProvider", serializeData(data, fields));

        String customJsonConfig = description.getCustomJsonConfig();
        if (!Strings.isNullOrEmpty(customJsonConfig)) {
            mergeJsonObjects((JsonObject) jsonTree, gson.fromJson(customJsonConfig, JsonObject.class));
        }

        return gson.toJson(jsonTree);
    }

    private void sortByCategoryFieldIfNeed(List<Map<String, Object>> data, String categoryField) {
        if (data.isEmpty() || !(data.get(0) instanceof EntityMap))
            return;

        data.sort((entityMap1, entityMap2) -> {
            int compareResult;

            Object objToCompare1 = entityMap1.get(categoryField);
            Object objToCompare2 = entityMap2.get(categoryField);

            if (Objects.isNull(objToCompare1) && Objects.nonNull(objToCompare2)) {
                compareResult = -1;
            } else if (Objects.isNull(objToCompare1)) {
                compareResult = -1;
            } else if (Objects.isNull(objToCompare2)) {
                compareResult = 1;
            } else {
                if (objToCompare1 instanceof Comparable) {
                    Comparable comparable1 = (Comparable) objToCompare1;
                    Comparable comparable2 = (Comparable) objToCompare2;
                    compareResult = comparable1.compareTo(comparable2);
                } else {
                    throw new UnsupportedOperationException("Chart category entity field type must implement Comparable interface");
                }
            }
            return compareResult;
        });
    }

    private boolean isByDate(String categoryField, List<Map<String, Object>> data) {
        if (CollectionUtils.isNotEmpty(data)) {
            Map<String, Object> map = data.get(0);
            Object categoryFieldValue = map.get(categoryField);

            return categoryFieldValue instanceof Date
                    || categoryFieldValue instanceof LocalDate
                    || categoryFieldValue instanceof LocalDateTime;
        }

        return false;
    }

    protected void exportConfig(HashMap<String, Object> chart) {
        HashMap<Object, Object> export = new HashMap<>();
        export.put("enabled", true);
        if (StringUtils.isNotBlank(resultFileName)) {
            export.put("fileName", resultFileName);
        }
        chart.put("export", export);
    }

    public String convertPieChart(PieChartDescription description, List<Map<String, Object>> data) {
        HashMap<String, Object> chart = new HashMap<>();
        List<String> fields = new ArrayList<>();

        chart.put("type", "pie");
        chart.put("titleField", description.getTitleField());
        addField(fields, description.getTitleField());
        chart.put("valueField", description.getValueField());
        addField(fields, description.getValueField());
        chart.put("colorField", description.getColorField());
        addField(fields, description.getColorField());
        exportConfig(chart);

        if (Boolean.TRUE.equals(description.getShowLegend())) {
            Map<String, Object> legend = new HashMap<>();
            legend.put("markerType", "circle");
            legend.put("position", "right");
            legend.put("autoMargins", false);
            legend.put("valueText", join("[[value]] ", description.getUnits()));
            legend.put("valueWidth", 100);

            chart.put("legend", legend);
        }

        JsonElement jsonTree = gson.toJsonTree(chart);
        jsonTree.getAsJsonObject().add("dataProvider", serializeData(data, fields));

        String customJsonConfig = description.getCustomJsonConfig();
        if (!Strings.isNullOrEmpty(customJsonConfig)) {
            mergeJsonObjects((JsonObject) jsonTree, gson.fromJson(customJsonConfig, JsonObject.class));
        }

        return gson.toJson(jsonTree);
    }

    protected void mergeJsonObjects(JsonObject source, JsonObject config) {
        Set<String> sourceKeys = source.keySet();
        for (String key : config.keySet()) {
            if (sourceKeys.contains(key)) {
                JsonElement sourceElem = source.get(key);
                JsonElement configElem = config.get(key);
                if (sourceElem.isJsonObject() && configElem.isJsonObject()) {
                    mergeJsonObjects((JsonObject) sourceElem, (JsonObject) configElem);
                } else if (sourceElem.isJsonArray() && configElem.isJsonArray()) {
                    mergeJsonArrays((JsonArray) sourceElem, (JsonArray) configElem);
                } else {
                    source.add(key, config.get(key));
                }
            } else {
                source.add(key, config.get(key));
            }
        }
    }

    protected void mergeJsonArrays(JsonArray source, JsonArray config) {
        for (int i = 0; i < Math.min(source.size(), config.size()); ++i) {
            JsonElement sourceElem = source.get(i);
            JsonElement configElem = config.get(i);
            if (sourceElem.isJsonObject() && configElem.isJsonObject()) {
                mergeJsonObjects((JsonObject) sourceElem, (JsonObject) configElem);
            } else if (sourceElem.isJsonArray() && configElem.isJsonArray()) {
                mergeJsonArrays((JsonArray) sourceElem, (JsonArray) configElem);
            } else {
                source.set(i, configElem);
            }
        }
    }

    private JsonElement serializeData(List<Map<String, Object>> data, List<String> fields) {
        JsonArray dataArray = new JsonArray();
        for (Map<String, Object> map : data) {
            JsonObject itemElement = new JsonObject();
            for (String field : fields) {
                Object value = map.get(field);
                addProperty(itemElement, field, value);
            }
            dataArray.add(itemElement);
        }
        return dataArray;
    }

    protected void addProperty(JsonObject jsonObject, String property, Object value) {
        if (value instanceof Entity) {
            value = instanceNameProvider.getInstanceName(value);
        } else if (value instanceof LocalDate) {
            value = TEMPORAL_DATE_FORMATTER.format((LocalDate) value);
        } else if (value instanceof LocalDateTime) {
            value = TEMPORAL_DATE_TIME_FORMATTER.format((LocalDateTime) value);
        }

        jsonObject.add(property, gson.toJsonTree(value));
    }

    protected void addField(List<String> fields, @Nullable String field) {
        if (field != null) {
            fields.add(field);
        }
    }

    protected String join(Object... objects) {
        return StringUtils.join(objects, "");
    }
}
