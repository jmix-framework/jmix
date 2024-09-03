package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.component.data.BaseCalendarEventProvider;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The event is fired when the user clicks on "more" link. The "more" link can be activated by the following
 * properties:
 * <ul>
 *     <li>
 *         {@link FullCalendar#setDefaultDayMaxEventRowsEnabled(boolean)}
 *     </li>
 *     <li>
 *         {@link FullCalendar#setDayMaxEventRows(Integer)}
 *     </li>
 *     <li>
 *         {@link FullCalendar#setDefaultDayMaxEventsEnabled(boolean)}
 *     </li>
 *     <li>
 *         {@link FullCalendar#setDayMaxEvents(Integer)}
 *     </li>
 * </ul>
 */
public class MoreLinkClickEvent extends AbstractClickEvent {

    protected final boolean allDay;

    protected final LocalDateTime dateTime;

    protected final ViewInfo viewInfo;

    protected final List<CalendarEvent> visibleCalendarEvents;

    protected final List<CalendarEvent> hiddenCalendarEvents;

    protected final List<EventProviderContext> eventProviderContexts;

    public MoreLinkClickEvent(FullCalendar fullCalendar, boolean fromClient,
                              boolean allDay,
                              LocalDateTime dateTime,
                              List<CalendarEvent> visibleCalendarEvents,
                              List<CalendarEvent> hiddenCalendarEvents,
                              List<EventProviderContext> eventProviderContexts,
                              MouseEventDetails mouseEventDetails,
                              ViewInfo viewInfo) {
        super(fullCalendar, fromClient, mouseEventDetails);
        this.allDay = allDay;
        this.dateTime = dateTime;
        this.viewInfo = viewInfo;
        this.visibleCalendarEvents = visibleCalendarEvents;
        this.hiddenCalendarEvents = hiddenCalendarEvents;
        this.eventProviderContexts = eventProviderContexts;
    }

    /**
     * @return {@code true} if "more" link is clicked from day cell
     */
    public boolean isAllDay() {
        return allDay;
    }

    /**
     * Returns date-time as is from component without transformation. It means that value corresponds component's
     * TimeZone.
     * <p>
     * Note, if "more" link is clicked in day cell, the time part will be {@code 00:00:00}.
     *
     * @return date-time
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Returns all visible calendar events event if they are from different event providers. To get visible calendar
     * events by event providers use {@link #getEventProviderContexts()}.
     *
     * @return visible calendar events
     */
    public List<CalendarEvent> getVisibleCalendarEvents() {
        return visibleCalendarEvents;
    }

    /**
     * Returns all hidden calendar events event if they are from different event providers. To get hidden calendar
     * events by event providers use {@link #getEventProviderContexts()}.
     *
     * @return hidden calendar events
     */
    public List<CalendarEvent> getHiddenCalendarEvents() {
        return hiddenCalendarEvents;
    }

    /**
     * @return list of event providers contexts that contain information about calendar events
     */
    public List<EventProviderContext> getEventProviderContexts() {
        return eventProviderContexts;
    }

    /**
     * @return information about current calendar's view
     */
    public ViewInfo getViewInfo() {
        return viewInfo;
    }

    /**
     * Groups visible and hidden calendar events by event provider.
     */
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

        /**
         * @return event provider
         */
        public BaseCalendarEventProvider getEventProvider() {
            return eventProvider;
        }

        /**
         * @return visible calendar events
         */
        public List<CalendarEvent> getVisibleEvents() {
            return visibleEvents;
        }

        /**
         * @return hidden calendar events
         */
        public List<CalendarEvent> getHiddenEvents() {
            return hiddenEvents;
        }
    }
}
