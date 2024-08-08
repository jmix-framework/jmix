package io.jmix.fullcalendarflowui.component.serialization.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.vaadin.flow.data.provider.KeyMapper;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.model.HasEnumId;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.TimeZone;
import java.util.function.Supplier;

import static io.jmix.fullcalendarflowui.kit.component.CalendarDateTimeTransformations.transformToZDT;

public class CalendarEventSerializer extends StdSerializer<CalendarEvent> {

    protected Supplier<TimeZone> timeZoneSupplier = TimeZone::getDefault;
    protected final KeyMapper<Object> crossEventProviderKeyMapper;
    protected KeyMapper<Object> idMapper;
    protected String sourceId;

    public CalendarEventSerializer(String sourceId,
                                   KeyMapper<Object> idMapper,
                                   KeyMapper<Object> crossEventProviderKeyMapper) {
        super(CalendarEvent.class);

        Preconditions.checkNotNullArgument(idMapper);
        Preconditions.checkNotNullArgument(sourceId);
        Preconditions.checkNotNullArgument(crossEventProviderKeyMapper);

        this.idMapper = idMapper;
        this.sourceId = sourceId;
        this.crossEventProviderKeyMapper = crossEventProviderKeyMapper;
    }

    public void setTimeZoneSupplier(Supplier<TimeZone> timeZoneSupplier) {
        Preconditions.checkNotNullArgument(timeZoneSupplier);
        this.timeZoneSupplier = timeZoneSupplier;
    }

    @Override
    public void serialize(CalendarEvent value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        gen.writeObjectField("id", idMapper.key(value.getId()));
        serializeCrossEventProviderField("groupId", value.getGroupId(), gen, provider);
        serializeCrossEventProviderField("constraint", value.getConstraint(), gen, provider);

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

        serializeNullableValue("extendedProps", value.getAdditionalProperties(), gen, provider);
        serializeNullableValue("daysOfWeek", value.getRecurringDaysOfWeek(), gen, provider);

        serializeNullableValue("startRecur", value.getRecurringStarDate(), gen, provider);
        serializeNullableValue("endRecur", value.getRecurringEndDate(), gen, provider);

        serializeNullableValue("startTime", value.getRecurringStarTime(), gen, provider);
        serializeNullableValue("endTime", value.getRecurringEndTime(), gen, provider);

        gen.writeObjectField("jmixSourceId", sourceId);

        gen.writeEndObject();
    }

    protected void serializeCrossEventProviderField(String property, @Nullable Object value, JsonGenerator gen,
                                                    SerializerProvider provider) throws IOException {
        if (value == null) {
            return;
        }

        String rawValue = getRawGroupIdOrConstraint(value);
        rawValue = rawValue == null ? crossEventProviderKeyMapper.key(value) : rawValue;

        serializeNullableValue(property, rawValue, gen, provider);
    }

    protected void serializeNullableValue(String property, @Nullable Object value, JsonGenerator gen,
                                          SerializerProvider provider) throws IOException {
        if (value == null) {
            return;
        }
        provider.defaultSerializeField(property, value, gen);
    }

    @Nullable
    public static String getRawGroupIdOrConstraint(Object value) {
        Preconditions.checkNotNullArgument(value);

        if (value instanceof String stringValue) {
            return stringValue;
        } else if (value instanceof EnumClass<?> enumClass) {
            return enumClass.getId().toString();
        } else if (value instanceof HasEnumId<?> enumId) {
            return enumId.getId().toString();
        } else if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        } else {
            return null;
        }
    }
}
