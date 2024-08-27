package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.component.data.BaseCalendarEventProvider;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

public class MoreLinkClickEvent extends AbstractClickEvent {

    protected final boolean allDay;

    protected final LocalDateTime dateTime;

    protected final ViewInfo viewInfo;

    protected final List<EventProviderContext> eventProviderContexts;

    public MoreLinkClickEvent(FullCalendar fullCalendar,
                              boolean fromClient,
                              boolean allDay,
                              LocalDateTime dateTime,
                              ViewInfo viewInfo,
                              List<EventProviderContext> eventProviderContexts,
                              MouseEventDetails mouseEventDetails) {
        super(fullCalendar, fromClient, mouseEventDetails);
        this.allDay = allDay;
        this.dateTime = dateTime;
        this.viewInfo = viewInfo;
        this.eventProviderContexts = eventProviderContexts;
    }

    public boolean isAllDay() {
        return allDay;
    }

    /**
     * Returns date-time as is from component without transformation.
     *
     * @return date-time
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }

    public List<EventProviderContext> getEventProviderContexts() {
        return eventProviderContexts;
    }

    public static class EventProviderContext {
        protected final BaseCalendarEventProvider eventProvider;
        protected final List<CalendarEvent> visibleEvents;
        protected final List<CalendarEvent> hiddenEvents;

        public EventProviderContext(BaseCalendarEventProvider eventProvider,
                                    List<CalendarEvent> visibleEvents,
                                    List<CalendarEvent> hiddenEvents) {
            this.eventProvider = eventProvider;
            this.visibleEvents = visibleEvents;
            this.hiddenEvents = hiddenEvents;
        }

        public BaseCalendarEventProvider getEventProvider() {
            return eventProvider;
        }

        public List<CalendarEvent> getVisibleEvents() {
            return visibleEvents;
        }

        public List<CalendarEvent> getHiddenEvents() {
            return hiddenEvents;
        }
    }
}
