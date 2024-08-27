package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration;

import java.util.List;

public class EventResizeEvent extends AbstractEventChangeEvent {

    protected final CalendarEvent calendarEvent;

    protected final List<CalendarEvent> relatedEvents;

    protected final ViewInfo viewInfo;

    protected final CalendarDuration startDelta;

    protected final CalendarDuration endDelta;

    public EventResizeEvent(FullCalendar fullCalendar,
                            boolean fromClient,
                            MouseEventDetails mouseEventDetails,
                            OldValues oldValues,
                            CalendarEvent calendarEvent,
                            List<CalendarEvent> relatedEvents,
                            ViewInfo viewInfo,
                            CalendarDuration startDelta,
                            CalendarDuration endDelta) {
        super(fullCalendar, fromClient, mouseEventDetails, oldValues);

        this.calendarEvent = calendarEvent;
        this.relatedEvents = relatedEvents;
        this.viewInfo = viewInfo;
        this.startDelta = startDelta;
        this.endDelta = endDelta;
    }

    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    public List<CalendarEvent> getRelatedEvents() {
        return relatedEvents;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }

    public CalendarDuration getStartDelta() {
        return startDelta;
    }

    public CalendarDuration getEndDelta() {
        return endDelta;
    }
}
