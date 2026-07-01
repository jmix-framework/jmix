/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.fullcalendarflowui.kit.meta.loader;

import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.*;

public class NoOpBusinessHours implements Serializable {
    private static final Integer[] ALL_DAYS = {0, 1, 2, 3, 4, 5, 6};

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

    public static NoOpBusinessHours of(Integer... daysOfWeek) {
        return create(null, null, Objects.requireNonNull(daysOfWeek));
    }

    public static NoOpBusinessHours of(LocalTime startTime) {
        return create(Objects.requireNonNull(startTime), null, ALL_DAYS);
    }

    public static NoOpBusinessHours of(LocalTime startTime, LocalTime endTime) {
        return create(Objects.requireNonNull(startTime), Objects.requireNonNull(endTime), ALL_DAYS);
    }

    public static NoOpBusinessHours of(LocalTime startTime, Integer... daysOfWeek) {
        return create(Objects.requireNonNull(startTime), null, Objects.requireNonNull(daysOfWeek));
    }

    public static NoOpBusinessHours of(LocalTime startTime, LocalTime endTime, Integer... daysOfWeek) {
        return create(
                Objects.requireNonNull(startTime),
                Objects.requireNonNull(endTime),
                Objects.requireNonNull(daysOfWeek));
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
