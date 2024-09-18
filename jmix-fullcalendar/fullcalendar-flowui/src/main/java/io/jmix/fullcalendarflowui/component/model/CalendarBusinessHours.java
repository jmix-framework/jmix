package io.jmix.fullcalendarflowui.component.model;

import io.jmix.core.common.util.Preconditions;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A business hours entry.
 */
public class CalendarBusinessHours implements Serializable {

    public static final DayOfWeek[] ALL_DAYS = DayOfWeek.values();

    protected Set<DayOfWeek> daysOfWeek;

    protected LocalTime startTime;
    protected LocalTime endTime;

    private CalendarBusinessHours(@Nullable LocalTime startTime, @Nullable LocalTime endTime,
                                  @Nullable DayOfWeek... daysOfWeek) {
        this.startTime = startTime;
        this.endTime = endTime;

        this.daysOfWeek = daysOfWeek == null || daysOfWeek.length == 0
                ? Collections.emptySet()
                : new LinkedHashSet<>(Arrays.asList(daysOfWeek));
    }

    private static CalendarBusinessHours create(@Nullable LocalTime startTime, @Nullable LocalTime endTime,
                                                @Nullable DayOfWeek... daysOfWeek) {
        return new CalendarBusinessHours(startTime, endTime, daysOfWeek);
    }

    /**
     * Creates business hours entry from days-of-week. The duration of business hours will be calculated from
     * start of the day to {@link FullCalendar#getDefaultTimedEventDuration()}.
     *
     * @param daysOfWeek days of week
     * @return business hours entry
     */
    public static CalendarBusinessHours of(DayOfWeek... daysOfWeek) {
        Preconditions.checkNotNullArgument(daysOfWeek);
        return create(null, null, daysOfWeek);
    }

    /**
     * Creates business hours entry from start time. The duration of business hours will be equal to
     * {@link FullCalendar#getDefaultTimedEventDuration()}. This time will be applied for all days of week.
     *
     * @param startTime start time of business hours
     * @return business hours entry
     */
    public static CalendarBusinessHours of(LocalTime startTime) {
        Preconditions.checkNotNullArgument(startTime);
        return create(startTime, null, ALL_DAYS);
    }

    /**
     * Creates business hours entry from start and end times. This time will be applied for all days of week.
     *
     * @param startTime start time of business hours
     * @param endTime   end time of business hours
     * @return business hours entry
     */
    public static CalendarBusinessHours of(LocalTime startTime, LocalTime endTime) {
        Preconditions.checkNotNullArgument(startTime);
        Preconditions.checkNotNullArgument(endTime);
        return create(startTime, endTime, ALL_DAYS);
    }

    /**
     * Creates business hours entry from start time and days of week. The duration of business hours will be equal to
     * {@link FullCalendar#getDefaultTimedEventDuration()}. This time will be applied for the provided days.
     *
     * @param startTime  start time of business hours
     * @param daysOfWeek days of week
     * @return business hours entry
     */
    public static CalendarBusinessHours of(LocalTime startTime, DayOfWeek... daysOfWeek) {
        Preconditions.checkNotNullArgument(startTime);
        Preconditions.checkNotNullArgument(daysOfWeek);
        return create(startTime, null, daysOfWeek);
    }

    /**
     * Creates business hours entry from start and end times and days of week. This time will be applied for the
     * provided days.
     *
     * @param startTime  start time of business hours
     * @param endTime    end time of business hours
     * @param daysOfWeek days of week
     * @return business hours entry
     */
    public static CalendarBusinessHours of(LocalTime startTime, LocalTime endTime, DayOfWeek... daysOfWeek) {
        Preconditions.checkNotNullArgument(startTime);
        Preconditions.checkNotNullArgument(endTime);
        Preconditions.checkNotNullArgument(daysOfWeek);
        return create(startTime, endTime, daysOfWeek);
    }

    /**
     * @return start time of business hours or {@code null} if not set
     */
    @Nullable
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * @return end time of business hours or {@code null} if not set
     */
    @Nullable
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * @return days of business hours
     */
    public Set<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }
}
