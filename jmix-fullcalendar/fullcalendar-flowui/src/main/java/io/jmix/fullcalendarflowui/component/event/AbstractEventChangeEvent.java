package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.TimeZone;

public class AbstractEventChangeEvent extends AbstractClickEvent {

    protected final OldValues oldValues;

    public AbstractEventChangeEvent(FullCalendar fullCalendar, boolean fromClient,
                                    MouseEventDetails mouseEventDetails,
                                    OldValues oldValues) {
        super(fullCalendar, fromClient, mouseEventDetails);

        this.oldValues = oldValues;
    }

    public OldValues getOldValues() {
        return oldValues;
    }

    public static class OldValues {

        protected LocalDateTime startDateTime;

        protected LocalDateTime endDateTime;

        protected boolean allDay;

        public OldValues(LocalDateTime startDateTime, @Nullable LocalDateTime endDateTime, boolean allDay) {
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.allDay = allDay;
        }

        /**
         * @return old value of start date time that correspond to system time zone: {@link TimeZone#getDefault()}
         */
        public LocalDateTime getStartDateTime() {
            return startDateTime;
        }

        @Nullable
        public LocalDateTime getEndDateTime() {
            return endDateTime;
        }

        public boolean isAllDay() {
            return allDay;
        }
    }
}
