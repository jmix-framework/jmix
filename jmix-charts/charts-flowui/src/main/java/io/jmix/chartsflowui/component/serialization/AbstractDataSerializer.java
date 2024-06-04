/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.component.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.jmix.chartsflowui.kit.component.serialization.AbstractSerializer;
import io.jmix.chartsflowui.kit.data.chart.DataItem;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractDataSerializer<T> extends AbstractSerializer<T> {

    protected static final String ITEM_KEY_PROPERTY_NAME = "$k";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss:S";
    public static final String DEFAULT_DATE_TIME_FORMAT = DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT;

    protected static final FastDateFormat DATE_FORMATTER
            = FastDateFormat.getInstance(DEFAULT_DATE_TIME_FORMAT);

    protected static final DateTimeFormatter TEMPORAL_DATE_FORMATTER
            = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

    protected static final DateTimeFormatter TEMPORAL_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);

    protected Function<DataItem, String> itemKeyMapper;

    protected Messages messages;
    protected MetadataTools metadataTools;

    public AbstractDataSerializer(Class<T> aClass, Function<DataItem, String> itemKeyMapper) {
        super(aClass);
        this.itemKeyMapper = itemKeyMapper;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    protected void serializeDataItem(DataItem dataItem, JsonGenerator gen, SerializerProvider provider,
                                     String categoryField, List<String> fields) throws IOException {
        gen.writeStartObject();
        writeIfNotNull(categoryField, formatValue(dataItem.getValue(categoryField)), gen, provider);

        for (String field : fields) {
            writeIfNotNull(field, formatValue(dataItem.getValue(field)), gen, provider);
        }

        // Store the key as the last column
        writeIfNotNull(ITEM_KEY_PROPERTY_NAME, itemKeyMapper.apply(dataItem), gen, provider);

        gen.writeEndObject();
    }

    protected Object formatValue(Object valueToFormat) {
        Object formattedValue;
        if (EntityValues.isEntity(valueToFormat)) {
            formattedValue = metadataTools.getInstanceName(valueToFormat);
        } else if (valueToFormat instanceof Enum<?> enumValue) {
            formattedValue = messages.getMessage(enumValue);
        } else if (valueToFormat instanceof Date dateValue) {
            formattedValue = DATE_FORMATTER.format(dateValue);
        } else if (valueToFormat instanceof LocalDateTime localDateTimeValue) {
            formattedValue = TEMPORAL_DATE_TIME_FORMATTER.format(localDateTimeValue);
        } else if (valueToFormat instanceof LocalDate localDateValue) {
            formattedValue = TEMPORAL_DATE_FORMATTER.format(localDateValue);
        } else {
            formattedValue = valueToFormat;
        }
        return formattedValue;
    }
}
