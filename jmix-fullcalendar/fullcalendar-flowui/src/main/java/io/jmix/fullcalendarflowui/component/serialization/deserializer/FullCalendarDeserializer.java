package io.jmix.fullcalendarflowui.component.serialization.deserializer;

import elemental.json.JsonObject;
import io.jmix.core.common.util.Preconditions;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.contextmenu.event.DayCell;
import io.jmix.fullcalendarflowui.component.contextmenu.event.EventCell;
import io.jmix.fullcalendarflowui.component.contextmenu.event.FullCalendarCellContext;
import io.jmix.fullcalendarflowui.component.data.AbstractEventProviderManager;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import io.jmix.fullcalendarflowui.kit.component.serialization.deserializer.JmixFullCalendarDeserializer;
import io.jmix.fullcalendarflowui.kit.component.serialization.DomCalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.serialization.DomMouseEventDetails;

import java.time.ZoneId;

import static io.jmix.fullcalendarflowui.component.FullCalendarDelegate.parseAndTransform;
import static io.jmix.fullcalendarflowui.component.FullCalendarUtils.getEventProviderManager;

public class FullCalendarDeserializer extends JmixFullCalendarDeserializer {

    public FullCalendarCellContext deserializeCalendarCellContext(JsonObject json, FullCalendar calendar) {
        Preconditions.checkNotNullArgument(json);
        Preconditions.checkNotNullArgument(calendar);

        DayCell dayCell = json.hasKey("dayCell")
                ? deserializeDayCell(json.getObject("dayCell"), calendar.getTimeZone().toZoneId())
                : null;
        EventCell eventCell = json.hasKey("eventCell")
                ? deserializeEventCell(json.getObject("eventCell"), calendar)
                : null;

        DomMouseEventDetails domMouseEventDetails = deserialize(json.getObject("mouseDetails"), DomMouseEventDetails.class);

        return new FullCalendarCellContext(dayCell, eventCell, new MouseEventDetails(domMouseEventDetails));
    }

    public DayCell deserializeDayCell(JsonObject json, ZoneId zoneId) {
        Preconditions.checkNotNullArgument(json);
        return new DayCell(parseAndTransform(json.getString("date"), zoneId),
                json.getBoolean("isDisabled"),
                json.getBoolean("isFuture"),
                json.getBoolean("isMonthStart"),
                json.getBoolean("isOther"),
                json.getBoolean("isPast"),
                json.getBoolean("isToday"));
    }

    public EventCell deserializeEventCell(JsonObject json, FullCalendar calendar) {
        Preconditions.checkNotNullArgument(json);

        DomCalendarEvent domCalendarEvent = deserialize(json.getObject("event"), DomCalendarEvent.class);

        AbstractEventProviderManager eventProviderManager =
                getEventProviderManager(calendar, domCalendarEvent.getSourceId());

        if (eventProviderManager == null) {
            throw new IllegalStateException("Unable to find event provider for sourceId: "
                    + domCalendarEvent.getSourceId());
        }
        CalendarEvent calendarEvent = eventProviderManager.getCalendarEvent(domCalendarEvent.getId());
        if (calendarEvent == null) {
            throw new IllegalStateException("Unable to find calendar event for client id: "
                    + domCalendarEvent.getId());
        }
        return new EventCell(
                json.getBoolean("isFuture"),
                json.getBoolean("isMirror"),
                json.getBoolean("isPast"),
                json.getBoolean("isToday"),
                calendarEvent,
                eventProviderManager.getEventProvider()
        );
    }
}
