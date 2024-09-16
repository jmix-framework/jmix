package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;

import java.time.LocalDateTime;

/**
 * The event is fired when a day cell or time cell is clicked.
 */
public class DateClickEvent extends AbstractClickEvent {

    protected final LocalDateTime dateTime;

    protected final boolean allDay;

    protected final DisplayModeInfo displayModeInfo;

    public DateClickEvent(FullCalendar fullCalendar,
                          boolean fromClient,
                          MouseEventDetails mouseEventDetails,
                          LocalDateTime dateTime,
                          boolean allDay,
                          DisplayModeInfo displayModeInfo) {
        super(fullCalendar, fromClient, mouseEventDetails);

        this.dateTime = dateTime;
        this.allDay = allDay;
        this.displayModeInfo = displayModeInfo;
    }

    /**
     * Returns date-time as is from component without transformation. It means that value corresponds component's
     * TimeZone.
     * <p>
     * Note, if the day cell is clicked, the time part will be {@code 00:00:00}.
     *
     * @return date-time of clicked cell
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * @return {@code true} if day cell is clicked
     */
    public boolean isAllDay() {
        return allDay;
    }

    /**
     * @return information about current calendar's display mode
     */
    public DisplayModeInfo getDisplayModeInfo() {
        return displayModeInfo;
    }
}
