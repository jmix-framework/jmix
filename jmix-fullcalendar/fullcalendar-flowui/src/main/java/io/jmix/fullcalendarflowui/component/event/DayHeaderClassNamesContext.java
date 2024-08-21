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

import java.time.LocalDateTime;

public class DayHeaderClassNamesContext {

    // todo rp only date?
    protected LocalDateTime dateTime;

    protected DayOfWeek dayOfWeek;

    protected boolean isDisabled;

    protected boolean isFuture;

    protected boolean isOther;

    protected boolean isPast;

    protected boolean isToday;

    public DayHeaderClassNamesContext(LocalDateTime dateTime, DayOfWeek dayOfWeek, boolean isDisabled, boolean isFuture,
                                      boolean isOther, boolean isPast, boolean isToday) {
        this.dateTime = dateTime;
        this.dayOfWeek = dayOfWeek;
        this.isDisabled = isDisabled;
        this.isFuture = isFuture;
        this.isOther = isOther;
        this.isPast = isPast;
        this.isToday = isToday;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
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
}
