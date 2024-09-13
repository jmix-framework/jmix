package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * The event is fired when resizing stops and the calendar event has changed in duration.
 */
public class EventResizeEvent extends AbstractEventMoveEvent {

    protected final CalendarEvent calendarEvent;

    protected final CalendarDataProvider dataProvider;

    protected final CalendarDuration startDelta;

    protected final CalendarDuration endDelta;

    protected final ViewInfo viewInfo;

    public EventResizeEvent(FullCalendar fullCalendar, boolean fromClient,
                            CalendarEvent calendarEvent,
                            CalendarDataProvider dataProvider,
                            List<RelatedDataProviderContext> relatedDataProviderContexts,
                            List<CalendarEvent> relatedCalendarEvents,
                            OldValues oldValues,
                            @Nullable CalendarDuration startDelta,
                            @Nullable CalendarDuration endDelta,
                            MouseEventDetails mouseEventDetails,
                            ViewInfo viewInfo) {
        super(fullCalendar, fromClient, mouseEventDetails, relatedDataProviderContexts, relatedCalendarEvents,
                oldValues);

        this.calendarEvent = calendarEvent;
        this.dataProvider = dataProvider;
        this.viewInfo = viewInfo;
        this.startDelta = startDelta;
        this.endDelta = endDelta;
    }

    /**
     * @return calendar event that contains new values after the resize
     */
    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    /**
     * Returns all related calendar events even if they are from different data providers. To get related calendar
     * events by data provider, use {@link #getRelatedDataProviderContexts()}.
     * <p>
     * An event might have other recurring event instances or might be linked to other events with the same group ID.
     * So these events will be related with resized one.
     *
     * @return all related calendar events that were also resized
     */
    public List<CalendarEvent> getRelatedCalendarEvents() {
        return relatedCalendarEvents;
    }

    /**
     * @return data provider of resized calendar event
     */
    public CalendarDataProvider getDataProvider() {
        return dataProvider;
    }

    /**
     * @return the amount of time the event’s start date was moved by or {@code null} if start date was not moved
     */
    @Nullable
    public CalendarDuration getStartDelta() {
        return startDelta;
    }

    /**
     * @return the amount of time the event’s end date was moved by or {@code null} if end date was not moved
     */
    @Nullable
    public CalendarDuration getEndDelta() {
        return endDelta;
    }

    /**
     * @return information about current calendar's view
     */
    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
