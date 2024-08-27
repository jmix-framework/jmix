package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.kit.component.model.CalendarView;

import java.time.LocalDate;
import java.util.TimeZone;

public class ViewInfo {

    protected final LocalDate activeEnd;

    protected final LocalDate activeStart;

    protected final LocalDate currentEnd;

    protected final LocalDate currentStart;

    protected final CalendarView calendarView;

    public ViewInfo(LocalDate activeEnd,
                    LocalDate activeStart,
                    LocalDate currentEnd,
                    LocalDate currentStart,
                    CalendarView calendarView) {
        this.activeEnd = activeEnd;
        this.activeStart = activeStart;
        this.currentEnd = currentEnd;
        this.currentStart = currentStart;
        this.calendarView = calendarView;
    }

    /**
     * @return active end date time that corresponds to system time zone: {@link TimeZone#getDefault()}
     */
    public LocalDate getActiveEnd() {
        return activeEnd;
    }

    public LocalDate getActiveStart() {
        return activeStart;
    }

    public LocalDate getCurrentEnd() {
        return currentEnd;
    }

    public LocalDate getCurrentStart() {
        return currentStart;
    }

    public CalendarView getCalendarView() {
        return calendarView;
    }
}
