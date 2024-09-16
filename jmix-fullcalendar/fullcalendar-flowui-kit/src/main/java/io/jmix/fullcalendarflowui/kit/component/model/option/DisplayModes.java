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

package io.jmix.fullcalendarflowui.kit.component.model.option;

import io.jmix.fullcalendarflowui.kit.component.model.*;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static io.jmix.fullcalendarflowui.kit.component.model.option.OptionConstants.VIEWS;

/**
 * INTERNAL.
 */
public class DisplayModes extends CalendarOption {

    protected DayGridDayProperties dayGridDay;
    protected DayGridWeekProperties dayGridWeek;
    protected DayGridMonthProperties dayGridMonth;
    protected DayGridYearProperties dayGridYear;

    protected TimeGridDayProperties timeGridDay;
    protected TimeGridWeekProperties timeGridWeek;

    protected ListDayProperties listDay;
    protected ListWeekProperties listWeek;
    protected ListMonthProperties listMonth;
    protected ListYearProperties listYear;

    protected MultiMonthYearProperties multiMonthYear;

    protected List<CustomCalendarDisplayMode> customDisplayModes;

    public DisplayModes() {
        super(VIEWS);

        initDayGrids();
        initTimeGrids();
        initLists();
        initMultiMonths();
    }

    public DayGridWeekProperties getDayGridWeek() {
        return dayGridWeek;
    }

    public DayGridDayProperties getDayGridDay() {
        return dayGridDay;
    }

    public DayGridMonthProperties getDayGridMonth() {
        return dayGridMonth;
    }

    public DayGridYearProperties getDayGridYear() {
        return dayGridYear;
    }

    public TimeGridDayProperties getTimeGridDay() {
        return timeGridDay;
    }

    public TimeGridWeekProperties getTimeGridWeek() {
        return timeGridWeek;
    }

    public ListDayProperties getListDay() {
        return listDay;
    }

    public ListWeekProperties getListWeek() {
        return listWeek;
    }

    public ListMonthProperties getListMonth() {
        return listMonth;
    }

    public ListYearProperties getListYear() {
        return listYear;
    }

    public MultiMonthYearProperties getMultiMonthYear() {
        return multiMonthYear;
    }

    public List<CustomCalendarDisplayMode> getCustomCalendarDisplayModes() {
        return customDisplayModes == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(customDisplayModes);
    }

    public void addCustomCalendarDisplayMode(CustomCalendarDisplayMode displayMode) {
        if (customDisplayModes == null) {
            customDisplayModes = new ArrayList<>();
        }

        if (!customDisplayModes.contains(displayMode)) {
            customDisplayModes.add(displayMode);
            markAsDirty();
        }
    }

    public void removeCustomCalendarDisplayMode(CustomCalendarDisplayMode displayMode) {
        if (customDisplayModes == null) {
            return;
        }

        if (customDisplayModes.remove(displayMode)) {
            markAsDirty();
        }
    }

    @Nullable
    public CustomCalendarDisplayMode getCustomCalendarDisplayMode(String id) {
        if (customDisplayModes == null) {
            return null;
        }
        return customDisplayModes.stream()
                .filter(c -> c.getDisplayMode().getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public <T extends AbstractCalendarDisplayModeProperties> T getCalendarDisplayModeProperties(
            CalendarDisplayMode displayMode) {
        Objects.requireNonNull(displayMode);
        return getCalendarDisplayModeProperties(displayMode.getId());
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractCalendarDisplayModeProperties> T getCalendarDisplayModeProperties(String id) {
        if (CalendarDisplayModes.DAY_GRID_DAY.getId().equals(id)) {
            return (T) dayGridDay;
        } else if (CalendarDisplayModes.DAY_GRID_WEEK.getId().equals(id)) {
            return (T) dayGridWeek;
        } else if (CalendarDisplayModes.DAY_GRID_MONTH.getId().equals(id)) {
            return (T) dayGridMonth;
        } else if (CalendarDisplayModes.DAY_GRID_YEAR.getId().equals(id)) {
            return (T) dayGridYear;
        } else if (CalendarDisplayModes.TIME_GRID_DAY.getId().equals(id)) {
            return (T) timeGridDay;
        } else if (CalendarDisplayModes.TIME_GRID_WEEK.getId().equals(id)) {
            return (T) timeGridWeek;
        } else if (CalendarDisplayModes.LIST_DAY.getId().equals(id)) {
            return (T) listDay;
        } else if (CalendarDisplayModes.LIST_WEEK.getId().equals(id)) {
            return (T) listWeek;
        } else if (CalendarDisplayModes.LIST_MONTH.getId().equals(id)) {
            return (T) listMonth;
        } else if (CalendarDisplayModes.LIST_YEAR.getId().equals(id)) {
            return (T) listYear;
        } else if (CalendarDisplayModes.MULTI_MONTH_YEAR.getId().equals(id)) {
            return (T) multiMonthYear;
        } else {
            throw new IllegalStateException("There is no predefined display mode properties with id: " + id);
        }
    }

    protected void onOptionChange(OptionChangeEvent event) {
        markAsDirty();
    }

    protected void initDayGrids() {
        dayGridDay = new DayGridDayProperties();
        dayGridDay.addChangeListener(this::onOptionChange);

        dayGridWeek = new DayGridWeekProperties();
        dayGridWeek.addChangeListener(this::onOptionChange);

        dayGridMonth = new DayGridMonthProperties();
        dayGridMonth.addChangeListener(this::onOptionChange);

        dayGridYear = new DayGridYearProperties();
        dayGridYear.addChangeListener(this::onOptionChange);
    }

    protected void initTimeGrids() {
        timeGridDay = new TimeGridDayProperties();
        timeGridDay.addChangeListener(this::onOptionChange);

        timeGridWeek = new TimeGridWeekProperties();
        timeGridWeek.addChangeListener(this::onOptionChange);
    }

    protected void initLists() {
        listDay = new ListDayProperties();
        listDay.addChangeListener(this::onOptionChange);

        listWeek = new ListWeekProperties();
        listWeek.addChangeListener(this::onOptionChange);

        listMonth = new ListMonthProperties();
        listMonth.addChangeListener(this::onOptionChange);

        listYear = new ListYearProperties();
        listYear.addChangeListener(this::onOptionChange);
    }

    protected void initMultiMonths() {
        multiMonthYear = new MultiMonthYearProperties();
        multiMonthYear.addChangeListener(this::onOptionChange);
    }
}
