package io.jmix.fullcalendarflowui.component.contextmenu.event;

import io.jmix.fullcalendar.DayOfWeek;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayModes;

import java.time.LocalDate;

/**
 * Describes day cell from day-grid:
 * <ul>
 *     <li>
 *          {@link CalendarDisplayModes#DAY_GRID_DAY}
 *     </li>
 *     <li>
 *         {@link CalendarDisplayModes#DAY_GRID_WEEK},
 *     </li>
 *     <li>
 *          {@link CalendarDisplayModes#DAY_GRID_MONTH}
 *     </li>
 *     <li>
 *         {@link CalendarDisplayModes#DAY_GRID_YEAR}
 *     </li>
 * </ul>
 */
public class DayCell {

    protected final LocalDate date;

    protected final boolean isFuture;

    protected final boolean isPast;

    protected final boolean isToday;

    protected final boolean isOther;

    protected final boolean isDisabled;

    protected final DayOfWeek dayOfWeek;

    public DayCell(LocalDate date, boolean isDisabled, boolean isFuture,
                   boolean isOther, boolean isPast, boolean isToday, DayOfWeek dayOfWeek) {
        this.date = date;
        this.isDisabled = isDisabled;
        this.isFuture = isFuture;
        this.isOther = isOther;
        this.isPast = isPast;
        this.isToday = isToday;
        this.dayOfWeek = dayOfWeek;
    }

    /**
     * @return date that corresponds to day cell
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * The cell can be disabled, for instance, if it is not in valid date range. See
     * {@link FullCalendar#setValidRange(LocalDate, LocalDate)}
     *
     * @return whether day cell is disabled
     */
    public boolean isDisabled() {
        return isDisabled;
    }

    /**
     * @return whether the cell's date is in future compared with today's date
     */
    public boolean isFuture() {
        return isFuture;
    }

    /**
     * @return whether the cell's date is in other month
     */
    public boolean isOther() {
        return isOther;
    }

    /**
     * @return whether the cell's date is in past compared with today's date
     */
    public boolean isPast() {
        return isPast;
    }

    /**
     * @return whether the cell's date is today
     */
    public boolean isToday() {
        return isToday;
    }

    /**
     * @return cell's day of week
     */
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }
}
