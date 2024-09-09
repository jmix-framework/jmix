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

/**
 * Configuration properties of day-grid month view {@link CalendarViewType#DAY_GRID_MONTH}.
 * <p>
 * The view properties can be retrieved from {@link JmixFullCalendar#getCalendarViewProperties(CalendarViewType)}.
 * For instance:
 * <pre>{@code
 * calendar.getCalendarViewProperties(CalendarViewType.DAY_GRID_MONTH);
 * }</pre>
 */
public class DayGridMonthViewProperties extends AbstractDayGridViewProperties {

    protected boolean fixedWeekCount = true;

    protected boolean showNonCurrentDates = true;

    public DayGridMonthViewProperties() {
        super(CalendarViewType.DAY_GRID_MONTH.getId());
    }

    /**
     * @return {@code true} if calendar displays fixed week count
     */
    public boolean isFixedWeekCount() {
        return fixedWeekCount;
    }

    /**
     * Determines the number of displayed weeks.
     * <p>
     * If {@code true}, the calendar will always be {@code 6} weeks tall. If {@code false}, the calendar will have
     * either {@code 4}, {@code 5}, or {@code 6} weeks, depending on the month.
     * <p>
     * The default value is {@code true}.
     *
     * @param fixedWeekCount whether to display fixed week count
     */
    public void setFixedWeekCount(boolean fixedWeekCount) {
        this.fixedWeekCount = fixedWeekCount;

        markAsDirty();
    }

    /**
     * @return {@code true} if calendar shows dates from next/previous months
     */
    public boolean isShowNonCurrentDates() {
        return showNonCurrentDates;
    }

    /**
     * Determines displaying dates from previous and next months.
     * <p>
     * The default value is {@code true}.
     *
     * @param showNonCurrentDates whether to show dates from next/previous months
     */
    public void setShowNonCurrentDates(boolean showNonCurrentDates) {
        this.showNonCurrentDates = showNonCurrentDates;

        markAsDirty();
    }
}
