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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.jmix.charts.model.chart.impl.GanttChartModelImpl;
import io.jmix.charts.widget.amcharts.serialization.ChartJsonSerializationContext;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.data.DataItem;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component("ui_ChartDataItemsSerializer")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ChartDataItemsSerializer {

    protected static final String ITEM_KEY_PROPERTY_NAME = "$k";
    protected static final FastDateFormat DATE_FORMATTER
            = FastDateFormat.getInstance(ChartJsonSerializationContext.DEFAULT_DATE_TIME_FORMAT);

    protected static final DateTimeFormatter TEMPORAL_DATE_FORMATTER
            = DateTimeFormatter.ofPattern(ChartJsonSerializationContext.DEFAULT_DATE_FORMAT);

    protected static final DateTimeFormatter TEMPORAL_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern(ChartJsonSerializationContext.DEFAULT_DATE_TIME_FORMAT);

    protected Messages messages;
    protected MetadataTools metadataTools;

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    public JsonArray serialize(List<DataItem> items, ChartJsonSerializationContext context) {
        JsonArray serialized = new JsonArray();

        Function<DataItem, String> itemKeyMapper = context.getItemKeyMapper();

        for (DataItem item : items) {
            JsonObject itemElement = new JsonObject();

            String itemKey = itemKeyMapper.apply(item);
            if (itemKey != null) {
                itemElement.add(ITEM_KEY_PROPERTY_NAME, context.serialize(itemKey));
            }

            for (String property : context.getProperties()) {
                Object propertyValue = item.getValue(property);

                addProperty(itemElement, property, propertyValue, context);
            }

            if (context.getChartModel() instanceof GanttChartModelImpl) {
                GanttChartModelImpl chart = (GanttChartModelImpl) context.getChartModel();

                String segmentsField = chart.getSegmentsField();

                Object value = item.getValue(segmentsField);
                if (value != null && !(value instanceof Collection)) {
                    throw new RuntimeException("Gantt chart segments field must be a collection");
                }

                JsonArray segments = new JsonArray();

                if (value != null) {
                    int segmentIndex = 0;
                    //noinspection unchecked
                    for (DataItem dataItem : (Collection<DataItem>) value) {
                        JsonObject segment = new JsonObject();
                        segment.add("$i", context.serialize(segmentIndex));

                        for (String field : context.getSegmentFields()) {
                            Object propertyValue = dataItem.getValue(field);

                            if (propertyValue != null) {
                                addProperty(segment, field, propertyValue, context);
                            }
                        }
                        segments.add(segment);

                        segmentIndex++;
                    }
                }
                itemElement.add(segmentsField, segments);
            }

            serialized.add(itemElement);
        }

        return serialized;
    }

    protected void addProperty(JsonObject jsonObject, String property, Object value, JsonSerializationContext context) {
        Object formattedValue;
        if (EntityValues.isEntity(value)) {
            formattedValue = metadataTools.getInstanceName(value);
        } else if (value instanceof Enum) {
            formattedValue = messages.getMessage((Enum) value);
        } else if (value instanceof Date) {
            formattedValue = DATE_FORMATTER.format((Date) value);
        } else if (value instanceof LocalDateTime) {
            formattedValue = TEMPORAL_DATE_TIME_FORMATTER.format((LocalDateTime) value);
        } else if (value instanceof LocalDate) {
            formattedValue = TEMPORAL_DATE_FORMATTER.format((LocalDate) value);
        } else {
            formattedValue = value;
        }
        jsonObject.add(property, context.serialize(formattedValue));
    }
}