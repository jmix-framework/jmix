package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.component.data.CalendarDataProvider;
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

    protected final List<DataProviderContext> dataProviderContexts;

    public MoreLinkClickEvent(FullCalendar fullCalendar, boolean fromClient,
                              boolean allDay,
                              LocalDateTime dateTime,
                              List<CalendarEvent> visibleCalendarEvents,
                              List<CalendarEvent> hiddenCalendarEvents,
                              List<DataProviderContext> dataProviderContexts,
                              MouseEventDetails mouseEventDetails,
                              ViewInfo viewInfo) {
        super(fullCalendar, fromClient, mouseEventDetails);
        this.allDay = allDay;
        this.dateTime = dateTime;
        this.viewInfo = viewInfo;
        this.visibleCalendarEvents = visibleCalendarEvents;
        this.hiddenCalendarEvents = hiddenCalendarEvents;
        this.dataProviderContexts = dataProviderContexts;
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
     * Returns all visible calendar events event if they are from different data providers. To get visible calendar
     * events by data providers use {@link #getDataProviderContexts()}.
     *
     * @return visible calendar events
     */
    public List<CalendarEvent> getVisibleCalendarEvents() {
        return visibleCalendarEvents;
    }

    /**
     * Returns all hidden calendar events event if they are from different data providers. To get hidden calendar
     * events by data providers use {@link #getDataProviderContexts()}.
     *
     * @return hidden calendar events
     */
    public List<CalendarEvent> getHiddenCalendarEvents() {
        return hiddenCalendarEvents;
    }

    /**
     * @return list of data providers contexts that contain information about calendar events
     */
    public List<DataProviderContext> getDataProviderContexts() {
        return dataProviderContexts;
    }

    /**
     * @return information about current calendar's view
     */
    public ViewInfo getViewInfo() {
        return viewInfo;
    }

    /**
     * Groups visible and hidden calendar events by data provider.
     */
    public static class DataProviderContext {

        protected final CalendarDataProvider dataProvider;

        protected final List<CalendarEvent> visibleEvents;

        protected final List<CalendarEvent> hiddenEvents;

        public DataProviderContext(CalendarDataProvider dataProvider,
                                   List<CalendarEvent> visibleEvents,
                                   List<CalendarEvent> hiddenEvents) {
            this.dataProvider = dataProvider;
            this.visibleEvents = visibleEvents;
            this.hiddenEvents = hiddenEvents;
        }

        /**
         * @return data provider
         */
        public CalendarDataProvider getDataProvider() {
            return dataProvider;
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
