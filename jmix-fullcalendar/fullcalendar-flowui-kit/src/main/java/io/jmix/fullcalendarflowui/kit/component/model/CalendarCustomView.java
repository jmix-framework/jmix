/*
 * Copyright 2024 Haulmont.
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

package io.jmix.fullcalendarflowui.kit.component.model;

import jakarta.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class CalendarCustomView implements Serializable {

    protected CalendarView calendarView;

    protected CalendarViewType type;

    protected CalendarDuration duration;

    protected Integer dayCount;

    public CalendarCustomView(String id) {
        this(id, null);
    }

    public CalendarCustomView(String id, @Nullable CalendarViewType type) {
        Objects.requireNonNull(id);

        this.calendarView = () -> id;
        this.type = type;
    }

    public CalendarView getCalendarView() {
        return calendarView;
    }

    public CalendarViewType getType() {
        return type == null ? CalendarViewType.DAY_GRID : type;
    }

    @Nullable
    public CalendarDuration getDuration() {
        return duration;
    }

    public CalendarCustomView withDuration(@Nullable CalendarDuration duration) {
        this.duration = duration;
        return this;
    }

    @Nullable
    public Integer getDayCount() {
        return dayCount;
    }

    public CalendarCustomView withDayCount(@Nullable Integer dayCount) {
        this.dayCount = dayCount;
        return this;
    }
}
