package io.jmix.fullcalendarflowui.component;

import io.jmix.core.annotation.Internal;
import io.jmix.fullcalendarflowui.component.data.AbstractEventProviderManager;
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
        return fullCalendar.eventProvidersMap.values().stream()
                .filter(em -> em.getSourceId().equals(sourceId))
                .findFirst()
                .orElse(null);
    }
}
