package io.jmix.fullcalendarflowui.component.serialization.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vaadin.flow.data.provider.KeyMapper;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.component.serialization.IncrementalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.List;

public class EventProviderDataSerializer extends FullCalendarSerializer {
    private static final Logger log = LoggerFactory.getLogger(EventProviderDataSerializer.class);

    protected KeyMapper<Object> keyMapper;
    protected String sourceId;

    public EventProviderDataSerializer(String sourceId,
                                       KeyMapper<Object> keyMapper,
                                       @Nullable KeyMapper<Object> crossEventProviderKeyMapper) {
        this.keyMapper = keyMapper;
        this.sourceId = sourceId;

        setupCalendarEventSerializer(objectMapper, crossEventProviderKeyMapper);
    }

    public JsonValue serializeIncrementalData(IncrementalData incrementalData) {
        String dataJson;
        try {
            dataJson = objectMapper.writeValueAsString(incrementalData);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize calendar data", e);
        }

        JsonObject json = jsonFactory.parse(dataJson);

        log.debug("Serialized data {}", json.toJson());

        return json;
    }

    public JsonArray serializeData(List<? extends CalendarEvent> items) {

        log.debug("Starting serialize calendar's data: {} items", items.size());

        String rawJson;
        try {
            rawJson = objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize calendar's data", e);
        }

        JsonArray json = jsonFactory.parse(rawJson);

        log.debug("Serialized data: {} items", json.toJson());

        return json;
    }

    protected void setupCalendarEventSerializer(ObjectMapper objectMapper,
                                                @Nullable KeyMapper<Object> crossEventProviderKeyMapper) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(
                new CalendarEventSerializer(keyMapper, sourceId, crossEventProviderKeyMapper));
        objectMapper.registerModule(module);
    }
}
