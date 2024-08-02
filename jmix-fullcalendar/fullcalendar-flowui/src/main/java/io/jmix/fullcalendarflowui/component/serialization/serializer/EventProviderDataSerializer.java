package io.jmix.fullcalendarflowui.component.serialization.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vaadin.flow.data.provider.KeyMapper;
import io.jmix.fullcalendarflowui.kit.component.serialization.serializer.JmixEventProviderDataSerializer;
import org.springframework.lang.Nullable;

public class EventProviderDataSerializer extends JmixEventProviderDataSerializer {

    public EventProviderDataSerializer(KeyMapper<Object> keyMapper,
                                       String sourceId,
                                       @Nullable KeyMapper<Object> crossEventProviderKeyMapper) {
        super(keyMapper, sourceId);

        setupCalendarEventSerializer(objectMapper, crossEventProviderKeyMapper);
    }

    protected void setupCalendarEventSerializer(ObjectMapper objectMapper,
                                                @Nullable KeyMapper<Object> crossEventProviderKeyMapper) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(
                new CalendarEventSerializer(keyMapper, sourceId, crossEventProviderKeyMapper));
        objectMapper.registerModule(module);
    }
}
