package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * The event is fired when the resizing stops and the calendar event's duration has changed.
 */
public class EventResizeEvent extends AbstractEventMoveEvent {

    protected final CalendarEvent calendarEvent;

    protected final CalendarDataProvider dataProvider;

    protected final CalendarDuration startDelta;

    protected final CalendarDuration endDelta;

    protected final DisplayModeInfo displayModeInfo;

    public EventResizeEvent(FullCalendar fullCalendar, boolean fromClient,
                            CalendarEvent calendarEvent,
                            CalendarDataProvider dataProvider,
                            List<RelatedDataProviderContext> relatedDataProviderContexts,
                            List<CalendarEvent> relatedCalendarEvents,
                            OldValues oldValues,
                            @Nullable CalendarDuration startDelta,
                            @Nullable CalendarDuration endDelta,
                            MouseEventDetails mouseEventDetails,
                            DisplayModeInfo displayModeInfo) {
        super(fullCalendar, fromClient, mouseEventDetails, relatedDataProviderContexts, relatedCalendarEvents,
                oldValues);

        this.calendarEvent = calendarEvent;
        this.dataProvider = dataProvider;
        this.displayModeInfo = displayModeInfo;
        this.startDelta = startDelta;
        this.endDelta = endDelta;
    }

    /**
     * @return the changed calendar event
     */
    @SuppressWarnings("unchecked")
    public <T extends CalendarEvent> T getCalendarEvent() {
        return (T) calendarEvent;
    }

    /**
     * Returns all related calendar events even if they are from different data providers. To get related calendar
     * events by data provider, use {@link #getRelatedDataProviderContexts()}.
     * <p>
     * An event might be linked to other events with the same group ID. So these events will be related with
     * resized one.
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
     * @return information about current calendar's display mode
     */
    public DisplayModeInfo getDisplayModeInfo() {
        return displayModeInfo;
    }
}
