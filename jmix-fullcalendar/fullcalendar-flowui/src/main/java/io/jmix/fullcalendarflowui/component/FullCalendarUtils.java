package io.jmix.fullcalendarflowui.component;

import io.jmix.fullcalendarflowui.component.data.AbstractEventProviderManager;
import org.springframework.lang.Nullable;

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
}
