package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.kit.component.model.CalendarView;

import java.time.LocalDate;

public class ViewInfo {

    protected final LocalDate activeStart;

    protected final LocalDate activeEnd;

    protected final LocalDate currentStart;

    protected final LocalDate currentEnd;

    protected final CalendarView calendarView;

    public ViewInfo(LocalDate activeStart,
                    LocalDate activeEnd,
                    LocalDate currentStart,
                    LocalDate currentEnd,
                    CalendarView calendarView) {
        this.activeStart = activeStart;
        this.activeEnd = activeEnd;
        this.currentStart = currentStart;
        this.currentEnd = currentEnd;
        this.calendarView = calendarView;
    }

    /**
     * Returns a date that is the first visible day. In month view, this value is often before the first day of
     * the month, because most months do not begin on the first day-of-week.
     *
     * @return a date that is the first visible day
     */
    public LocalDate getActiveStartDate() {
        return activeStart;
    }

    /**
     * Returns a date that is the last visible day.
     * <p>
     * Note, this <strong>value is exclusive</strong>.
     *
     * @return a date that is the last visible day
     */
    public LocalDate getActiveEndDate() {
        return activeEnd;
    }

    /**
     * Returns a date that is the start of the interval the view is trying to represent. For example,
     * in month view, this will be the first day of the month. This value disregards hidden days.
     *
     * @return a date that is the start of the interval the view is trying to represent
     */
    public LocalDate getCurrentStartDate() {
        return currentStart;
    }

    /**
     * Returns a date that is the end of the interval the view is trying to represent.
     * <p>
     * Note, this <strong>value is exclusive</strong>. For example, in month view, this will be the day
     * after the last day of the month. This value disregards hidden days.
     *
     * @return a date that is the end of the interval the view is trying to represent
     */
    public LocalDate getCurrentEndDate() {
        return currentEnd;
    }

    /**
     * @return current calendar view
     */
    public CalendarView getCalendarView() {
        return calendarView;
    }
}
