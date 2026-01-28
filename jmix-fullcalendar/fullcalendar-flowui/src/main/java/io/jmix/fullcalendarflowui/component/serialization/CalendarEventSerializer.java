/*
 * Copyright 2024 Haulmont.
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

package io.jmix.fullcalendarflowui.component.serialization;

import com.google.common.base.Splitter;
import com.vaadin.flow.data.provider.KeyMapper;
import io.jmix.core.common.util.Preconditions;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.jmix.fullcalendarflowui.component.serialization.FullCalendarSerializer.getRawGroupIdOrConstraint;
import static io.jmix.fullcalendarflowui.kit.component.CalendarDateTimeUtils.transformToZDT;

public class CalendarEventSerializer extends StdSerializer<CalendarEvent> {

    protected Supplier<TimeZone> timeZoneSupplier = TimeZone::getDefault;
    protected final KeyMapper<Object> crossDataProviderKeyMapper;
    protected KeyMapper<Object> idMapper;
    protected String sourceId;

    public CalendarEventSerializer(String sourceId,
                                   KeyMapper<Object> idMapper,
                                   KeyMapper<Object> crossDataProviderKeyMapper) {
        super(CalendarEvent.class);

        Preconditions.checkNotNullArgument(idMapper);
        Preconditions.checkNotNullArgument(sourceId);
        Preconditions.checkNotNullArgument(crossDataProviderKeyMapper);

        this.idMapper = idMapper;
        this.sourceId = sourceId;
        this.crossDataProviderKeyMapper = crossDataProviderKeyMapper;
    }

    public void setTimeZoneSupplier(Supplier<TimeZone> timeZoneSupplier) {
        Preconditions.checkNotNullArgument(timeZoneSupplier);
        this.timeZoneSupplier = timeZoneSupplier;
    }

    @Override
    public void serialize(CalendarEvent value, JsonGenerator gen, SerializationContext provider) {
        gen.writeStartObject();

        gen.writeStringProperty("id", idMapper.key(value.getId()));
        serializeCrossDataProviderField("groupId", value.getGroupId(), gen, provider);
        serializeCrossDataProviderField("constraint", value.getConstraint(), gen, provider);

        serializeNullableValue("allDay", value.getAllDay(), gen, provider);

        serializeNullableValue("start", value.getStartDateTime() != null
                ? transformToZDT(value.getStartDateTime(), null)
                : null, gen, provider);
        serializeNullableValue("end", value.getEndDateTime() != null
                ? transformToZDT(value.getEndDateTime(), null)
                : null, gen, provider);

        serializeNullableValue("title", value.getTitle(), gen, provider);
        serializeNullableValue("description", value.getDescription(), gen, provider);
        serializeNullableValue("classNames", value.getClassNames(), gen, provider);

        serializeNullableValue("startEditable", value.getStartEditable(), gen, provider);
        serializeNullableValue("durationEditable", value.getDurationEditable(), gen, provider);

        serializeNullableValue("display", value.getDisplay(), gen, provider);
        serializeNullableValue("overlap", value.getOverlap(), gen, provider);

        serializeNullableValue("backgroundColor", value.getBackgroundColor(), gen, provider);
        serializeNullableValue("borderColor", value.getBorderColor(), gen, provider);
        serializeNullableValue("textColor", value.getTextColor(), gen, provider);

        Map<String, Object> additionalProperties = processAdditionalProperties(value.getAdditionalProperties());
        serializeNullableValue("extendedProps", additionalProperties, gen, provider);
        serializeNullableValue("daysOfWeek", value.getRecurringDaysOfWeek(), gen, provider);

        serializeNullableValue("startRecur", value.getRecurringStartDate(), gen, provider);
        serializeNullableValue("endRecur", value.getRecurringEndDate(), gen, provider);

        serializeNullableValue("startTime", value.getRecurringStartTime(), gen, provider);
        serializeNullableValue("endTime", value.getRecurringEndTime(), gen, provider);

        gen.writeStringProperty("jmixSourceId", sourceId);

        gen.writeEndObject();
    }

    protected void serializeCrossDataProviderField(String property, @Nullable Object value, JsonGenerator gen,
                                                   SerializationContext provider) {
        if (value == null) {
            return;
        }

        String rawValue = getRawGroupIdOrConstraint(value);
        rawValue = rawValue == null ? crossDataProviderKeyMapper.key(value) : rawValue;

        serializeNullableValue(property, rawValue, gen, provider);
    }

    protected void serializeNullableValue(String property, @Nullable Object value, JsonGenerator gen,
                                          SerializationContext provider) {
        if (value == null) {
            return;
        }
        provider.defaultSerializeProperty(property, value, gen);
    }

    @Nullable
    protected Map<String, Object> processAdditionalProperties(@Nullable Map<String, Object> additionalProperties) {
        if (additionalProperties == null) {
            return Map.of();
        }
        return additionalProperties.entrySet().stream()
                .collect(Collectors.toMap(e -> formatAdditionalPropertiesKey(e.getKey()), Map.Entry::getValue));
    }

    protected String formatAdditionalPropertiesKey(String key) {
        List<String> parts = Splitter.on(".")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(key);
        if (parts.size() > 1) {
            StringBuilder sb = new StringBuilder();
            parts.forEach(p -> sb.append(StringUtils.capitalize(p)));
            return StringUtils.uncapitalize(sb.toString());
        }
        return parts.get(0);
    }
}
