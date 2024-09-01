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

import io.jmix.fullcalendar.DayOfWeek;
import io.jmix.fullcalendarflowui.component.FullCalendar;

import java.time.LocalDate;

public class DayHeaderClassNamesContext extends AbstractFullCalendarContext {

    protected final LocalDate date;

    protected final DayOfWeek dayOfWeek;

    protected final boolean isDisabled;

    protected final boolean isFuture;

    protected final boolean isOther;

    protected final boolean isPast;

    protected final boolean isToday;

    protected final ViewInfo viewInfo;

    public DayHeaderClassNamesContext(FullCalendar fullCalendar,
                                      LocalDate date,
                                      DayOfWeek dayOfWeek,
                                      boolean isDisabled,
                                      boolean isFuture,
                                      boolean isOther,
                                      boolean isPast,
                                      boolean isToday,
                                      ViewInfo viewInfo) {
        super(fullCalendar);
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.isDisabled = isDisabled;
        this.isFuture = isFuture;
        this.isOther = isOther;
        this.isPast = isPast;
        this.isToday = isToday;
        this.viewInfo = viewInfo;
    }

    /**
     * @return date that corresponds to header cell
     */
    public LocalDate getDate() {
        return date;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public boolean isFuture() {
        return isFuture;
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

    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
