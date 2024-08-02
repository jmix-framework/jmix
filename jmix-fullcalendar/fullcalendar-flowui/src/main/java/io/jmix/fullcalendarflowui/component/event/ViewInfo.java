package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.kit.component.model.CalendarView;

import java.time.LocalDateTime;
import java.util.TimeZone;

public class ViewInfo {

    protected final LocalDateTime activeEnd;
    protected final LocalDateTime activeStart;
    protected final LocalDateTime currentEnd;
    protected final LocalDateTime currentStart;
    protected final CalendarView calendarView;
    protected final String title;

    public ViewInfo(LocalDateTime activeEnd,
                    LocalDateTime activeStart,
                    LocalDateTime currentEnd,
                    LocalDateTime currentStart,
                    CalendarView calendarView,
                    String title) {
        this.activeEnd = activeEnd;
        this.activeStart = activeStart;
        this.currentEnd = currentEnd;
        this.currentStart = currentStart;
        this.calendarView = calendarView;
        this.title = title;
    }

    /**
     * @return active end date time that corresponds to system time zone: {@link TimeZone#getDefault()}
     */
    public LocalDateTime getActiveEnd() {
        return activeEnd;
    }

    public LocalDateTime getActiveStart() {
        return activeStart;
    }

    public LocalDateTime getCurrentEnd() {
        return currentEnd;
    }

    public LocalDateTime getCurrentStart() {
        return currentStart;
    }

    public CalendarView getCalendarView() {
        return calendarView;
    }

    public String getTitle() {
        return title;
    }
}
