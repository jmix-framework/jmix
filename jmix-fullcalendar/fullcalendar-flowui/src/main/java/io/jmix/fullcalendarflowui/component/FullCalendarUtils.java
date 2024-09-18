package io.jmix.fullcalendarflowui.component;

import io.jmix.core.annotation.Internal;
import io.jmix.fullcalendarflowui.component.data.AbstractDataProviderManager;
import org.springframework.lang.Nullable;

/**
 * INTERNAL.
 */
@Internal
public final class FullCalendarUtils {

    private FullCalendarUtils() {
    }

    @Nullable
    public static AbstractDataProviderManager getDataProviderManager(FullCalendar fullCalendar, String sourceId) {
        return fullCalendar.dataProvidersMap.values().stream()
                .filter(em -> em.getSourceId().equals(sourceId))
                .findFirst()
                .orElse(null);
    }
}
