package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.BaseCalendarEventProvider;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration;

import java.util.List;

/**
 * The event is fired when dragging stops and the event has moved to a different day/time cell.
 */
public class EventDropEvent extends AbstractEventMoveEvent {

    protected final CalendarEvent calendarEvent;

    protected final BaseCalendarEventProvider eventProvider;

    protected final CalendarDuration delta;

    protected final ViewInfo viewInfo;

    public EventDropEvent(FullCalendar fullCalendar, boolean fromClient,
                          CalendarEvent calendarEvent,
                          BaseCalendarEventProvider eventProvider,
                          List<RelatedEventProviderContext> relatedEventProviderContexts,
                          List<CalendarEvent> relatedCalendarEvents,
                          OldValues oldValues,
                          CalendarDuration delta,
                          MouseEventDetails mouseEventDetails,
                          ViewInfo viewInfo) {
        super(fullCalendar, fromClient, mouseEventDetails, relatedEventProviderContexts, relatedCalendarEvents,
                oldValues);

        this.calendarEvent = calendarEvent;
        this.eventProvider = eventProvider;
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
     * Returns all related calendar events even if they are from different event providers. To get related calendar
     * events by event provider, use {@link #getRelatedEventProviderContexts()}.
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
     * @return event provider of dropped calendar event
     */
    public BaseCalendarEventProvider getEventProvider() {
        return eventProvider;
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
