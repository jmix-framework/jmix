package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration;

import java.util.List;

/**
 * The event is fired when dragging stops and the event has moved to a different day/time cell.
 */
public class EventDropEvent extends AbstractEventMoveEvent {

    protected final CalendarEvent calendarEvent;

    protected final CalendarDataProvider dataProvider;

    protected final CalendarDuration delta;

    protected final ViewInfo viewInfo;

    public EventDropEvent(FullCalendar fullCalendar, boolean fromClient,
                          CalendarEvent calendarEvent,
                          CalendarDataProvider dataProvider,
                          List<RelatedDataProviderContext> relatedDataProviderContexts,
                          List<CalendarEvent> relatedCalendarEvents,
                          OldValues oldValues,
                          CalendarDuration delta,
                          MouseEventDetails mouseEventDetails,
                          ViewInfo viewInfo) {
        super(fullCalendar, fromClient, mouseEventDetails, relatedDataProviderContexts, relatedCalendarEvents,
                oldValues);

        this.calendarEvent = calendarEvent;
        this.dataProvider = dataProvider;
        this.viewInfo = viewInfo;
        this.delta = delta;
    }

    /**
     * @return calendar event that contains new values after the drop
     */
    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    /**
     * Returns all related calendar events even if they are from different data providers. To get related calendar
     * events by data provider, use {@link #getRelatedDataProviderContexts()}.
     * <p>
     * An event might have other recurring event instances or might be linked to other events with the same group ID.
     * So these events will be related with dropped one.
     *
     * @return related calendar events that were also dropped
     */
    public List<CalendarEvent> getRelatedCalendarEvents() {
        return relatedCalendarEvents;
    }

    /**
     * @return data provider of dropped calendar event
     */
    public CalendarDataProvider getDataProvider() {
        return dataProvider;
    }

    /**
     * @return the amount of time the event was moved by
     */
    public CalendarDuration getDelta() {
        return delta;
    }

    /**
     * @return information about current calendar's view
     */
    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
