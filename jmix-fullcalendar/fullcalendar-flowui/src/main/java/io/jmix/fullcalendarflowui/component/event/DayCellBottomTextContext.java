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

package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.model.DayOfWeek;

import java.time.LocalDate;

/**
 * The context for generating text to be displayed at the bottom of the day cell.
 */
public class DayCellBottomTextContext extends AbstractFullCalendarContext {

    protected final LocalDate date;

    protected final DayOfWeek dayOfWeek;

    protected final boolean isDisabled;

    protected final boolean isFuture;

    protected final boolean isOther;

    protected final boolean isPast;

    protected final boolean isToday;

    protected final DisplayModeInfo displayModeInfo;

    public DayCellBottomTextContext(FullCalendar fullCalendar,
                                    LocalDate date,
                                    DayOfWeek dayOfWeek,
                                    boolean isDisabled,
                                    boolean isFuture,
                                    boolean isOther,
                                    boolean isPast,
                                    boolean isToday,
                                    DisplayModeInfo displayModeInfo) {
        super(fullCalendar);

        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.isDisabled = isDisabled;
        this.isFuture = isFuture;
        this.isOther = isOther;
        this.isPast = isPast;
        this.isToday = isToday;
        this.displayModeInfo = displayModeInfo;
    }

    /**
     * @return date that corresponds to day cell
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @return cell's day of week
     */
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Cell can be disabled, for instance, if it is not in valid date range. See
     * {@link FullCalendar#setValidRange(LocalDate, LocalDate)}.
     *
     * @return {@code true} if day cell is disabled
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
     * @return information about current calendar's display mode
     */
    public DisplayModeInfo getDisplayModeInfo() {
        return displayModeInfo;
    }
}
