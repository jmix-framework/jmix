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
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayModes;

import java.time.LocalTime;

/**
 * The context for generating class names for labels in time slots in timed display modes:
 * {@link CalendarDisplayModes#TIME_GRID_DAY} and {@link CalendarDisplayModes#TIME_GRID_WEEK}.
 */
public class SlotLabelClassNamesContext extends AbstractFullCalendarContext {

    protected final LocalTime time;

    protected final DisplayModeInfo displayModeInfo;

    public SlotLabelClassNamesContext(FullCalendar fullCalendar,
                                      LocalTime time,
                                      DisplayModeInfo displayModeInfo) {
        super(fullCalendar);
        this.time = time;
        this.displayModeInfo = displayModeInfo;
    }

    /**
     * Returns time as is from component without transformation. It means that value corresponds component's
     * TimeZone.
     *
     * @return slot time
     */
    public LocalTime getTime() {
        return time;
    }

    /**
     * @return information about current calendar's display mode
     */
    public DisplayModeInfo getDisplayModeInfo() {
        return displayModeInfo;
    }
}
