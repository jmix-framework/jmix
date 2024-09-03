package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.component.data.BaseCalendarEventProvider;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;

/**
 * The class describes a calendar event click event. The event is fired when the calendar event is clicked.
 */
public class EventClickEvent extends AbstractClickEvent {

    protected final CalendarEvent calendarEvent;

    protected final BaseCalendarEventProvider eventProvider;

    protected final ViewInfo viewInfo;

    public EventClickEvent(FullCalendar fullCalendar,
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

    /**
     * @return clicked calendar event
     */
    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    /**
     * @return event provider that contains clicked calendar event
     */
    public BaseCalendarEventProvider getEventProvider() {
        return eventProvider;
    }

    /**
     * @return information about current calendar's view
     */
    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
