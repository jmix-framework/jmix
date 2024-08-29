package io.jmix.fullcalendarflowui.component.model;

import io.jmix.fullcalendar.DayOfWeek;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class BusinessHours implements Serializable {

    public static final DayOfWeek[] ALL_DAYS = DayOfWeek.values();

    protected Set<DayOfWeek> daysOfWeek;

    protected LocalTime startTime;
    protected LocalTime endTime;

    private BusinessHours(@Nullable LocalTime startTime, @Nullable LocalTime endTime,
                          @Nullable DayOfWeek... daysOfWeek) {
        this.startTime = startTime;
        this.endTime = endTime;

        this.daysOfWeek = daysOfWeek == null || daysOfWeek.length == 0
                ? Collections.emptySet()
                : new LinkedHashSet<>(Arrays.asList(daysOfWeek));
    }

    private static BusinessHours create(@Nullable LocalTime startTime, @Nullable LocalTime endTime,
                                        @Nullable DayOfWeek... daysOfWeek) {
        return new BusinessHours(startTime, endTime, daysOfWeek);
    }

    public static BusinessHours of(@Nullable DayOfWeek... daysOfWeek) {
        return create(null, null, daysOfWeek);
    }

    public static BusinessHours of(@Nullable LocalTime startTime) {
        return create(startTime, null, ALL_DAYS);
    }

    public static BusinessHours of(@Nullable LocalTime startTime, @Nullable LocalTime endTime) {
        return create(startTime, endTime, ALL_DAYS);
    }

    public static BusinessHours of(@Nullable LocalTime startTime, @Nullable DayOfWeek... daysOfWeek) {
        return create(startTime, null, daysOfWeek);
    }

    public static BusinessHours of(@Nullable LocalTime startTime, @Nullable LocalTime endTime,
                                   @Nullable DayOfWeek... daysOfWeek) {
        return create(startTime, endTime, daysOfWeek);
    }

    @Nullable
    public LocalTime getStartTime() {
        return startTime;
    }

    @Nullable
    public LocalTime getEndTime() {
        return endTime;
    }

    public Set<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }
}
