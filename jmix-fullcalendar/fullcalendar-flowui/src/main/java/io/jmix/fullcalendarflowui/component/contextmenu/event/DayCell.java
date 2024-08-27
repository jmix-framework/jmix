package io.jmix.fullcalendarflowui.component.contextmenu.event;

import java.time.LocalDate;

public class DayCell {

    protected LocalDate date;

    protected boolean isFuture;

    protected boolean isPast;

    protected boolean isToday;

    protected boolean isMonthStart;

    protected boolean isOther;

    protected boolean isDisabled;

    public DayCell(LocalDate date, boolean isDisabled, boolean isFuture, boolean isMonthStart,
                   boolean isOther, boolean isPast, boolean isToday) {
        this.date = date;
        this.isDisabled = isDisabled;
        this.isFuture = isFuture;
        this.isMonthStart = isMonthStart;
        this.isOther = isOther;
        this.isPast = isPast;
        this.isToday = isToday;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public boolean isFuture() {
        return isFuture;
    }

    public boolean isMonthStart() {
        return isMonthStart;
    }

    public boolean isOther() {
        return isOther;
    }

    public boolean isPast() {
        return isPast;
    }

    public boolean isToday() {
        return isToday;
    }
}
