package io.jmix.fullcalendarflowui.component.data;

import elemental.json.JsonArray;
import io.jmix.core.annotation.Internal;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.serialization.FullCalendarSerializer;

import java.util.List;

/**
 * INTERNAL.
 * Data provider manager that works with {@link CallbackCalendarDataProvider}.
 */
@Internal
public class CallbackDataProviderManager extends AbstractDataProviderManager {

    protected List<CalendarEvent> lastFetchedEvents;

    public CallbackDataProviderManager(CallbackCalendarDataProvider dataProvider,
                                       FullCalendarSerializer serializer,
                                       FullCalendar fullCalendar) {
        super(dataProvider, serializer, fullCalendar, "_addLazyEventSource");
    }

    @Override
    public CallbackCalendarDataProvider getDataProvider() {
        return (CallbackCalendarDataProvider) super.getDataProvider();
    }

    public JsonArray fetchAndSerialize(CallbackCalendarDataProvider.ItemsFetchContext context) {
        lastFetchedEvents = getDataProvider().onItemsFetch(context);

        return serializeData(lastFetchedEvents);
    }

    public JsonArray serializeData(List<CalendarEvent> calendarEvents) {
        return dataSerializer.serializeData(calendarEvents);
    }

    @Override
    public CalendarEvent getCalendarEvent(String clientId) {
        if (lastFetchedEvents == null || lastFetchedEvents.isEmpty()) {
            return null;
        }
        Object itemId = eventKeyMapper.get(clientId);
        if (itemId != null) {
            return lastFetchedEvents.stream()
                    .filter(ce -> itemId.equals(ce.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
