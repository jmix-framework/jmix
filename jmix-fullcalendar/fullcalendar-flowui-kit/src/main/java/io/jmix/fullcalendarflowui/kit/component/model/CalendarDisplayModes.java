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

/**
 * Supported component's display modes.
 */
public enum CalendarDisplayModes implements CalendarDisplayMode {

    DAY_GRID_DAY("dayGridDay"),
    DAY_GRID_WEEK("dayGridWeek"),
    DAY_GRID_MONTH("dayGridMonth"),
    DAY_GRID_YEAR("dayGridYear"),

    TIME_GRID_DAY("timeGridDay"),
    TIME_GRID_WEEK("timeGridWeek"),

    LIST_DAY("listDay"),
    LIST_WEEK("listWeek"),
    LIST_MONTH("listMonth"),
    LIST_YEAR("listYear"),

    MULTI_MONTH_YEAR("multiMonthYear");

    private final String id;

    CalendarDisplayModes(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public static CalendarDisplayModes fromId(String id) {
        for (CalendarDisplayModes mode : CalendarDisplayModes.values()) {
            if (mode.getId().equals(id)) {
                return mode;
            }
        }
        return null;
    }
}
