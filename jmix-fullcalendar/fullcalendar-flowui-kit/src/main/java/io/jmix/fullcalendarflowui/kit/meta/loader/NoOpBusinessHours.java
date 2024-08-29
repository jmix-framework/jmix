package io.jmix.fullcalendarflowui.kit.meta.loader;

import jakarta.annotation.Nullable;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class NoOpBusinessHours implements Serializable {

    protected Set<Integer> daysOfWeek;

    protected LocalTime startTime;
    protected LocalTime endTime;

    private NoOpBusinessHours(@Nullable LocalTime startTime, @Nullable LocalTime endTime,
                              @Nullable Integer... daysOfWeek) {
        this.startTime = startTime;
        this.endTime = endTime;

        this.daysOfWeek = daysOfWeek == null || daysOfWeek.length == 0
                ? Collections.emptySet()
                : new LinkedHashSet<>(Arrays.asList(daysOfWeek));
    }

    private static NoOpBusinessHours create(@Nullable LocalTime startTime, @Nullable LocalTime endTime,
                                            @Nullable Integer... daysOfWeek) {
        return new NoOpBusinessHours(startTime, endTime, daysOfWeek);
    }

    public static NoOpBusinessHours of(@Nullable LocalTime startTime, @Nullable LocalTime endTime,
                                       @Nullable Integer... daysOfWeek) {
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

    public Set<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }
}
