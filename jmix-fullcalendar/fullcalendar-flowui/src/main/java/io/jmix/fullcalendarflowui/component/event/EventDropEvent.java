package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration;

import java.util.List;

public class EventDropEvent extends AbstractEventChangeEvent {

    protected final CalendarEvent calendarEvent;
    protected final List<CalendarEvent> relatedEvents;

    protected final ViewInfo viewInfo;
    protected final CalendarDuration delta;

    public EventDropEvent(FullCalendar source,
                          boolean fromClient,
                          MouseEventDetails mouseEventDetails,
                          OldValues oldValues,
                          CalendarEvent calendarEvent,
                          List<CalendarEvent> relatedEvents,
                          ViewInfo viewInfo,
                          CalendarDuration delta) {
        super(source, fromClient, mouseEventDetails, oldValues);

        this.calendarEvent = calendarEvent;
        this.relatedEvents = relatedEvents;
        this.viewInfo = viewInfo;
        this.delta = delta;
    }

    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    public List<CalendarEvent> getRelatedEvents() {
        return relatedEvents;
    }

    public CalendarDuration getDelta() {
        return delta;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
