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

import io.jmix.chartsflowui.data.item.EntityDataItem;
import io.jmix.chartsflowui.kit.component.serialization.AbstractSerializer;
import io.jmix.chartsflowui.kit.data.chart.DataItem;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.accesscontext.UiEntityAttributeContext;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractDataSerializer<T> extends AbstractSerializer<T> {

    protected static final String ITEM_KEY_PROPERTY_NAME = "$k";
    protected static final String VIEW_PERMISSIONS_ATTRIBUTE = "charts_viewPermissions";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss.SSS";
    public static final String DEFAULT_DATE_TIME_FORMAT = DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT;

    protected static final FastDateFormat DATE_FORMATTER
            = FastDateFormat.getInstance(DEFAULT_DATE_TIME_FORMAT);

    protected static final DateTimeFormatter TEMPORAL_DATE_FORMATTER
            = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

    protected static final DateTimeFormatter TEMPORAL_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);

    protected Function<DataItem, String> itemKeyMapper;

    protected Messages messages;
    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected AccessManager accessManager;

    public AbstractDataSerializer(Class<T> aClass, Function<DataItem, String> itemKeyMapper) {
        super(aClass);
        this.itemKeyMapper = itemKeyMapper;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    protected void serializeDataItem(DataItem dataItem, JsonGenerator gen, SerializationContext provider,
                                     String categoryField, List<String> fields) throws JacksonException {
        gen.writeStartObject();
        writeDataItemValue(dataItem, categoryField, gen, provider);

        for (String field : fields) {
            writeDataItemValue(dataItem, field, gen, provider);
        }

        // Store the key as the last column
        writeIfNotNull(ITEM_KEY_PROPERTY_NAME, itemKeyMapper.apply(dataItem), gen, provider);

        gen.writeEndObject();
    }

    protected void writeDataItemValue(DataItem dataItem, String field, JsonGenerator gen,
                                      SerializationContext provider) throws JacksonException {
        if (isFieldViewPermitted(dataItem, field, provider)) {
            writeIfNotNull(field, formatValue(dataItem.getValue(field)), gen, provider);
        } else {
            // ECharts takes the dataset dimensions from the keys of the first row, so the field is kept
            // with a null value: omitting it would shift the series that are bound to the dimensions by order.
            gen.writeNullProperty(field);
        }
    }

    protected boolean isFieldViewPermitted(DataItem dataItem, String field, SerializationContext provider) {
        if (!(dataItem instanceof EntityDataItem entityDataItem)) {
            return true;
        }

        MetaClass metaClass = metadata.getClass(entityDataItem.getItem());
        return getViewPermissions(provider)
                .computeIfAbsent(metaClass, key -> new HashMap<>())
                .computeIfAbsent(field, key -> isPropertyViewPermitted(metaClass, key));
    }

    protected boolean isPropertyViewPermitted(MetaClass metaClass, String property) {
        MetaPropertyPath propertyPath = metadataTools.resolveMetaPropertyPathOrNull(metaClass, property);
        if (propertyPath == null) {
            return true;
        }

        UiEntityAttributeContext context = new UiEntityAttributeContext(propertyPath);
        accessManager.applyRegisteredConstraints(context);
        return context.canView();
    }

    /**
     * Returns the attribute view permissions checked during the current serialization call. They are kept
     * as a per-call attribute, so the policies are checked once per field rather than once per item,
     * and each data update checks them anew.
     */
    @SuppressWarnings("unchecked")
    protected Map<MetaClass, Map<String, Boolean>> getViewPermissions(SerializationContext provider) {
        Map<MetaClass, Map<String, Boolean>> viewPermissions =
                (Map<MetaClass, Map<String, Boolean>>) provider.getAttribute(VIEW_PERMISSIONS_ATTRIBUTE);

        if (viewPermissions == null) {
            viewPermissions = new HashMap<>();
            provider.setAttribute(VIEW_PERMISSIONS_ATTRIBUTE, viewPermissions);
        }

        return viewPermissions;
    }

    @Nullable
    protected Object formatValue(@Nullable Object valueToFormat) {
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
