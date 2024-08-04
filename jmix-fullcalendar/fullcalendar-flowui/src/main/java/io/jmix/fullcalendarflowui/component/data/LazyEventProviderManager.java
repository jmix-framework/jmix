package io.jmix.fullcalendarflowui.component.data;

import elemental.json.JsonArray;
import io.jmix.fullcalendarflowui.component.serialization.serializer.FullCalendarSerializer;

import java.util.List;

public class LazyEventProviderManager extends AbstractEventProviderManager {

    protected List<CalendarEvent> lastFetchedEvents;

    public LazyEventProviderManager(LazyCalendarEventProvider eventProvider,
                                    FullCalendarSerializer serializer) {
        super(eventProvider, serializer, "_addLazyEventSource");
    }

    @Override
    public LazyCalendarEventProvider getEventProvider() {
        return (LazyCalendarEventProvider) super.getEventProvider();
    }

    public JsonArray fetchAndSerialize(LazyCalendarEventProvider.ItemsFetchContext context) {
        lastFetchedEvents = getEventProvider().onItemsFetch(context);

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
