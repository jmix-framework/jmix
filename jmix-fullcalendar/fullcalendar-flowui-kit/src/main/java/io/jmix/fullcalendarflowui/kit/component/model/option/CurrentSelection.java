/*
 * Copyright 2025 Haulmont.
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

package io.jmix.fullcalendarflowui.kit.component.model.option;

import org.jspecify.annotations.Nullable;

import static io.jmix.fullcalendarflowui.kit.component.model.option.OptionUtils.CURRENT_SELECTION;

/**
 * INTERNAL.
 */
public class CurrentSelection extends CalendarOption {

    protected Boolean allDay;
    protected String startDateTime;
    protected String endDateTime;

    public CurrentSelection() {
        super(CURRENT_SELECTION);
    }

    @Nullable
    public Boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;

        markAsDirty();
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;

        markAsDirty();
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;

        markAsDirty();
    }

    public void clear() {
        allDay = null;
        startDateTime = null;
        endDateTime = null;
    }
}
