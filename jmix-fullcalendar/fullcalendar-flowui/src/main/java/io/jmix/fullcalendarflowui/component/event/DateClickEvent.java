package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;

import java.time.LocalDateTime;

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
     * Returns date-time as is from component without transformation.
     *
     * @return date-time of clicked cell
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
