package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.data.CalendarEventProvider;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;

public class EventMouseLeaveEvent extends AbstractClickEvent {

    protected final CalendarEvent calendarEvent;
    protected final CalendarEventProvider eventProvider;
    protected final ViewInfo viewInfo;

    public EventMouseLeaveEvent(FullCalendar source,
                                boolean fromClient,
                                MouseEventDetails mouseEventDetails,
                                CalendarEvent calendarEvent,
                                CalendarEventProvider eventProvider,
                                ViewInfo viewInfo) {
        super(source, fromClient, mouseEventDetails);

        this.calendarEvent = calendarEvent;
        this.eventProvider = eventProvider;
        this.viewInfo = viewInfo;
    }

    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    public CalendarEventProvider getEventProvider() {
        return eventProvider;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
