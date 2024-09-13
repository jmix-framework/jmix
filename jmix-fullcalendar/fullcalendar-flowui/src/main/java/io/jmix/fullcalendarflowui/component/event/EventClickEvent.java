package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.component.data.CalendarDataProvider;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;

/**
 * The event is fired when the calendar event is clicked.
 */
public class EventClickEvent extends AbstractClickEvent {

    protected final CalendarEvent calendarEvent;

    protected final CalendarDataProvider dataProvider;

    protected final ViewInfo viewInfo;

    public EventClickEvent(FullCalendar fullCalendar,
                           boolean fromClient,
                           MouseEventDetails mouseEventDetails,
                           CalendarEvent calendarEvent,
                           CalendarDataProvider dataProvider,
                           ViewInfo viewInfo) {
        super(fullCalendar, fromClient, mouseEventDetails);

        this.calendarEvent = calendarEvent;
        this.dataProvider = dataProvider;
        this.viewInfo = viewInfo;
    }

    /**
     * @return clicked calendar event
     */
    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    /**
     * @return data provider that contains clicked calendar event
     */
    public CalendarDataProvider getDataProvider() {
        return dataProvider;
    }

    /**
     * @return information about current calendar's view
     */
    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
