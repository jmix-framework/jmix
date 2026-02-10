package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

/**
 * Base class for {@link CalendarEvent} change events.
 */
public class AbstractEventMoveEvent extends AbstractClickEvent {

    protected final List<RelatedDataProviderContext> relatedDataProviderContexts;

    protected final List<CalendarEvent> relatedCalendarEvents;

    protected final OldValues oldValues;

    public AbstractEventMoveEvent(FullCalendar fullCalendar, boolean fromClient,
                                  MouseEventDetails mouseEventDetails,
                                  List<RelatedDataProviderContext> relatedDataProviderContexts,
                                  List<CalendarEvent> relatedCalendarEvents,
                                  OldValues oldValues) {
        super(fullCalendar, fromClient, mouseEventDetails);

        this.relatedDataProviderContexts = relatedDataProviderContexts;
        this.relatedCalendarEvents = relatedCalendarEvents;
        this.oldValues = oldValues;
    }

    /**
     * @return data provider contexts of related calendar events
     */
    public List<RelatedDataProviderContext> getRelatedDataProviderContexts() {
        return relatedDataProviderContexts;
    }

    /**
     * @return related calendar events that were also changed
     */
    public List<CalendarEvent> getRelatedCalendarEvents() {
        return relatedCalendarEvents;
    }

    /**
     * @return previous values of {@link CalendarEvent}
     */
    public OldValues getOldValues() {
        return oldValues;
    }

    /**
     * Class contains information about previous values of {@link CalendarEvent}.
     */
    public static class OldValues {

        protected final LocalDateTime startDateTime;

        protected final LocalDateTime endDateTime;

        protected final boolean allDay;

        public OldValues(LocalDateTime startDateTime, @Nullable LocalDateTime endDateTime, boolean allDay) {
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.allDay = allDay;
        }

        /**
         * @return previous value of start date-time that correspond to system time zone: {@link TimeZone#getDefault()}
         */
        public LocalDateTime getStartDateTime() {
            return startDateTime;
        }

        /**
         * @return previous value of end date-time that correspond to system time zone: {@link TimeZone#getDefault()}
         * or {@code null} if end date-time is not set
         */
        @Nullable
        public LocalDateTime getEndDateTime() {
            return endDateTime;
        }

        /**
         * @return previous value of all-day
         */
        public boolean isAllDay() {
            return allDay;
        }
    }

    /**
     * Provides related calendar events and their data provider.
     */
    public static class RelatedDataProviderContext {

        protected final CalendarDataProvider dataProvider;
        protected final List<CalendarEvent> calendarEvents;

        public RelatedDataProviderContext(CalendarDataProvider dataProvider,
                                          List<CalendarEvent> calendarEvents) {
            this.dataProvider = dataProvider;
            this.calendarEvents = calendarEvents;
        }

        /**
         * @return data provider
         */
        public CalendarDataProvider getDataProvider() {
            return dataProvider;
        }

        /**
         * @return related calendar events
         */
        public List<CalendarEvent> getCalendarEvents() {
            return calendarEvents;
        }
    }
}
