package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;

import java.time.LocalDateTime;

/**
 * Describes date click. The event is fired when day cell or time cell is clicked.
 */
public class DateClickEvent extends AbstractClickEvent {

    protected final LocalDateTime dateTime;

    protected final boolean allDay;

    protected final ViewInfo viewInfo;

    public DateClickEvent(FullCalendar fullCalendar,
                          boolean fromClient,
                          MouseEventDetails mouseEventDetails,
                          LocalDateTime dateTime,
                          boolean allDay,
                          ViewInfo viewInfo) {
        super(fullCalendar, fromClient, mouseEventDetails);

        this.dateTime = dateTime;
        this.allDay = allDay;
        this.viewInfo = viewInfo;
    }

    /**
     * Returns date-time as is from component without transformation. It means that value corresponds component's
     * TimeZone.
     * <p>
     * Note, if day cell is clicked, the time part will be {@code 00:00:00}.
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
     * @return information about current calendar's view
     */
    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
