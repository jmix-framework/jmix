package io.jmix.fullcalendarflowui.component.data;

import com.vaadin.flow.data.provider.KeyMapper;
import io.jmix.fullcalendarflowui.component.serialization.serializer.EventProviderDataSerializer;
import io.jmix.fullcalendarflowui.kit.component.data.AbstractItemEventProviderManager;
import io.jmix.fullcalendarflowui.kit.component.data.ItemCalendarEventProvider;
import io.jmix.fullcalendarflowui.kit.component.serialization.serializer.JmixEventProviderDataSerializer;
import org.springframework.lang.Nullable;

public class ItemEventProviderManager extends AbstractItemEventProviderManager {

    public ItemEventProviderManager(ItemCalendarEventProvider eventProvider) {
        super(eventProvider);
    }

    @Override
    public void setCrossEventProviderKeyMapper(@Nullable KeyMapper<Object> crossEventProviderKeyMapper) {
        super.setCrossEventProviderKeyMapper(crossEventProviderKeyMapper);

        dataSerializer = createDataSerializer(keyMapper, sourceId, crossEventProviderKeyMapper);
    }

    @Override
    protected JmixEventProviderDataSerializer createDataSerializer(KeyMapper<Object> keyMapper,
                                                                   String sourceId,
                                                                   @Nullable KeyMapper<Object> crossEventProviderKeyMapper) {
        return new EventProviderDataSerializer(keyMapper, sourceId, crossEventProviderKeyMapper);
    }
}
