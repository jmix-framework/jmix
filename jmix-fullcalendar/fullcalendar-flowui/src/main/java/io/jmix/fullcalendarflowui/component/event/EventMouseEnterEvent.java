package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.component.data.BaseCalendarEventProvider;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;

public class EventMouseEnterEvent extends AbstractClickEvent {

    protected final CalendarEvent calendarEvent;

    protected final BaseCalendarEventProvider eventProvider;

    protected final ViewInfo viewInfo;

    public EventMouseEnterEvent(FullCalendar fullCalendar,
                                boolean fromClient,
                                MouseEventDetails mouseEventDetails,
                                CalendarEvent calendarEvent,
                                BaseCalendarEventProvider eventProvider,
                                ViewInfo viewInfo) {
        super(fullCalendar, fromClient, mouseEventDetails);

        this.calendarEvent = calendarEvent;
        this.eventProvider = eventProvider;
        this.viewInfo = viewInfo;
    }

    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    public BaseCalendarEventProvider getEventProvider() {
        return eventProvider;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
