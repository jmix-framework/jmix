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

public abstract class AbstractDayGridViewProperties extends AbstractCalendarViewProperties {

    protected String dayPopoverFormat;

    protected String dayHeaderFormat;

    protected String weekNumberFormat;

    protected String eventTimeFormat;

    protected boolean displayEventEnd = false;

    public AbstractDayGridViewProperties(String name) {
        super(name);
    }

    @Nullable
    public String getDayHeaderFormat() {
        return dayHeaderFormat;
    }

    public void setDayHeaderFormat(@Nullable String dayHeaderFormat) {
        this.dayHeaderFormat = dayHeaderFormat;

        markAsDirty();
    }

    @Nullable
    public String getWeekNumberFormat() {
        return weekNumberFormat;
    }

    public void setWeekNumberFormat(@Nullable String weekNumberFormat) {
        this.weekNumberFormat = weekNumberFormat;

        markAsDirty();
    }

    @Nullable
    public String getEventTimeFormat() {
        return eventTimeFormat;
    }

    public void setEventTimeFormat(@Nullable String eventTimeFormat) {
        this.eventTimeFormat = eventTimeFormat;

        markAsDirty();
    }

    @Nullable
    public String getDayPopoverFormat() {
        return dayPopoverFormat;
    }

    public void setDayPopoverFormat(@Nullable String dayPopoverFormat) {
        this.dayPopoverFormat = dayPopoverFormat;

        markAsDirty();
    }

    public boolean isDisplayEventEnd() {
        return displayEventEnd;
    }

    public void setDisplayEventEnd(boolean displayEventEnd) {
        this.displayEventEnd = displayEventEnd;

        markAsDirty();
    }
}
