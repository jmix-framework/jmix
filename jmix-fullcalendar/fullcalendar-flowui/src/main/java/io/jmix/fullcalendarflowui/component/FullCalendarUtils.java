package io.jmix.fullcalendarflowui.component;

import io.jmix.core.annotation.Internal;
import io.jmix.fullcalendarflowui.component.data.AbstractEventProviderManager;
import io.jmix.fullcalendarflowui.component.serialization.serializer.FullCalendarSerializer;
import org.springframework.lang.Nullable;

/**
 * INTERNAL.
 */
@Internal
public final class FullCalendarUtils {
    private FullCalendarUtils() {
    }

    @Nullable
    public static AbstractEventProviderManager getEventProviderManager(FullCalendar fullCalendar, String sourceId) {
        return fullCalendar.getEventProvidersMap().values().stream()
                .filter(em -> em.getSourceId().equals(sourceId))
                .findFirst()
                .orElse(null);
    }

    public static FullCalendarSerializer getSerializer(FullCalendar fullCalendar) {
        return fullCalendar.getSerializer();
    }
}
