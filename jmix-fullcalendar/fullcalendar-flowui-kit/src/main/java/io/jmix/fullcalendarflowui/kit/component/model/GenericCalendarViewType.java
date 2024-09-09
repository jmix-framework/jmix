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

import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;

import java.time.LocalDate;

/**
 * A "generic" value, can be used as a type for {@link CustomCalendarView}, or with
 * {@link JmixFullCalendar#setVisibleRange(LocalDate, LocalDate)}.
 */
public enum GenericCalendarViewType implements CalendarView {

    DAY_GRID("dayGrid"),
    TIME_GRID("timeGrid"),
    LIST("list"),
    MULTI_MONTH("multiMonth");

    private String id;

    GenericCalendarViewType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public static GenericCalendarViewType fromId(String id) {
        for (GenericCalendarViewType calendarView : GenericCalendarViewType.values()) {
            if (calendarView.getId().equals(id)) {
                return calendarView;
            }
        }
        return null;
    }
}
