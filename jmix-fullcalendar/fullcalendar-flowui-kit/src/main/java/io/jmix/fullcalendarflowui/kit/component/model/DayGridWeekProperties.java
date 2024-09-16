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
 * Configuration properties of day-grid week display mode {@link CalendarDisplayModes#DAY_GRID_WEEK}.
 * <p>
 * The properties can be retrieved from {@link JmixFullCalendar#getCalendarDisplayModeProperties(CalendarDisplayModes)}.
 * For instance:
 * <pre>{@code
 * calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.DAY_GRID_WEEK);
 * }</pre>
 */
public class DayGridWeekProperties extends AbstractDayGridProperties {

    public DayGridWeekProperties() {
        super(CalendarDisplayModes.DAY_GRID_WEEK.getId());
    }
}
