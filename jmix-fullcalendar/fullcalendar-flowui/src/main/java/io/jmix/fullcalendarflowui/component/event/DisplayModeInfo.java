package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayMode;

import java.time.LocalDate;

/**
 * Class contains information about a calendar's display mode, such as a date range.
 */
public class DisplayModeInfo {

    protected final LocalDate activeStart;

    protected final LocalDate activeEnd;

    protected final LocalDate currentStart;

    protected final LocalDate currentEnd;

    protected final CalendarDisplayMode displayMode;

    public DisplayModeInfo(LocalDate activeStart,
                           LocalDate activeEnd,
                           LocalDate currentStart,
                           LocalDate currentEnd,
                           CalendarDisplayMode displayMode) {
        this.activeStart = activeStart;
        this.activeEnd = activeEnd;
        this.currentStart = currentStart;
        this.currentEnd = currentEnd;
        this.displayMode = displayMode;
    }

    /**
     * Returns a date that is the first visible day. In month display mode, this value is often
     * before the first day of the month, because most months do not begin on the first day-of-week.
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
     * Returns a date that marks the beginning of the interval represented by the display mode. For example,
     * in month mode, this will be the first day of the month. This value disregards hidden days.
     *
     * @return a date that marks the beginning of the interval represented by the display mode
     */
    public LocalDate getCurrentStartDate() {
        return currentStart;
    }

    /**
     * Returns a date that marks the end of the interval represented by the display mode.
     * <p>
     * Note, this <strong>value is exclusive</strong>. For example, in month mode, this will be the day
     * after the last day of the month. This value disregards hidden days.
     *
     * @return a date that marks the end of the interval represented by the display mode
     */
    public LocalDate getCurrentEndDate() {
        return currentEnd;
    }

    /**
     * @return current calendar's display mode
     */
    public CalendarDisplayMode getDisplayMode() {
        return displayMode;
    }
}
