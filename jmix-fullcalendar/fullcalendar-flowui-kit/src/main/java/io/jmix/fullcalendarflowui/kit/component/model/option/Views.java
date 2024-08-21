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

public class Views extends CalendarOption {
    public static final String NAME = "views";

    protected DayGridDayViewProperties dayGridDay;
    protected DayGridWeekViewProperties dayGridWeek;
    protected DayGridMonthViewProperties dayGridMonth;
    protected DayGridYearViewProperties dayGridYear;

    protected TimeGridDayViewProperties timeGridDay;
    protected TimeGridWeekViewProperties timeGridWeek;

    protected ListDayViewProperties listDay;
    protected ListWeekViewProperties listWeek;
    protected ListMonthViewProperties listMonth;
    protected ListYearViewProperties listYear;

    protected List<CalendarCustomView> customViews;

    public Views() {
        super(NAME);

        initDayGridViews();
        initTimeGridViews();
        initListViews();
    }

    public DayGridWeekViewProperties getDayGridWeek() {
        return dayGridWeek;
    }

    public DayGridDayViewProperties getDayGridDay() {
        return dayGridDay;
    }

    public DayGridMonthViewProperties getDayGridMonth() {
        return dayGridMonth;
    }

    public DayGridYearViewProperties getDayGridYear() {
        return dayGridYear;
    }

    public TimeGridDayViewProperties getTimeGridDay() {
        return timeGridDay;
    }

    public TimeGridWeekViewProperties getTimeGridWeek() {
        return timeGridWeek;
    }

    public ListDayViewProperties getListDay() {
        return listDay;
    }

    public ListWeekViewProperties getListWeek() {
        return listWeek;
    }

    public ListMonthViewProperties getListMonth() {
        return listMonth;
    }

    public ListYearViewProperties getListYear() {
        return listYear;
    }

    public List<CalendarCustomView> getCustomViews() {
        return customViews == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(customViews);
    }

    public void addCustomView(CalendarCustomView customView) {
        if (customViews == null) {
            customViews = new ArrayList<>();
        }

        if (!customViews.contains(customView)) {
            customViews.add(customView);
            markAsDirty();
        }
    }

    public void removeCustomView(CalendarCustomView customView) {
        if (customViews == null) {
            return;
        }

        if (customViews.remove(customView)) {
            markAsDirty();
        }
    }

    @Nullable
    public CalendarCustomView getCustomView(String id) {
        if (customViews == null) {
            return null;
        }
        return customViews.stream()
                .filter(c -> c.getCalendarView().getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public <T extends AbstractCalendarViewProperties> T getCalendarViewProperties(CalendarView calendarView) {
        Objects.requireNonNull(calendarView);
        return getCalendarViewProperties(calendarView.getId());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends AbstractCalendarViewProperties> T getCalendarViewProperties(String viewId) {
        if (CalendarViewType.DAY_GRID_DAY.getId().equals(viewId)) {
            return (T) dayGridDay;
        } else if (CalendarViewType.DAY_GRID_WEEK.getId().equals(viewId)) {
            return (T) dayGridWeek;
        } else if (CalendarViewType.DAY_GRID_MONTH.getId().equals(viewId)) {
            return (T) dayGridMonth;
        } else if (CalendarViewType.DAY_GRID_YEAR.getId().equals(viewId)) {
            return (T) dayGridYear;
        } else if (CalendarViewType.TIME_GRID_DAY.getId().equals(viewId)) {
            return (T) timeGridDay;
        } else if (CalendarViewType.TIME_GRID_WEEK.getId().equals(viewId)) {
            return (T) timeGridWeek;
        } else if (CalendarViewType.LIST_DAY.getId().equals(viewId)) {
            return (T) listDay;
        } else if (CalendarViewType.LIST_WEEK.getId().equals(viewId)) {
            return (T) listWeek;
        } else if (CalendarViewType.LIST_MONTH.getId().equals(viewId)) {
            return (T) listMonth;
        } else if (CalendarViewType.LIST_YEAR.getId().equals(viewId)) {
            return (T) listYear;
        } else {
            return null;
        }
    }

    protected void onOptionChange(OptionChangeEvent event) {
        markAsDirty();
    }

    protected void initDayGridViews() {
        dayGridDay = new DayGridDayViewProperties();
        dayGridDay.addChangeListener(this::onOptionChange);

        dayGridWeek = new DayGridWeekViewProperties();
        dayGridWeek.addChangeListener(this::onOptionChange);

        dayGridMonth = new DayGridMonthViewProperties();
        dayGridMonth.addChangeListener(this::onOptionChange);

        dayGridYear = new DayGridYearViewProperties();
        dayGridYear.addChangeListener(this::onOptionChange);
    }

    protected void initTimeGridViews() {
        timeGridDay = new TimeGridDayViewProperties();
        timeGridDay.addChangeListener(this::onOptionChange);

        timeGridWeek = new TimeGridWeekViewProperties();
        timeGridWeek.addChangeListener(this::onOptionChange);
    }

    protected void initListViews() {
        listDay = new ListDayViewProperties();
        listDay.addChangeListener(this::onOptionChange);

        listWeek = new ListWeekViewProperties();
        listWeek.addChangeListener(this::onOptionChange);

        listMonth = new ListMonthViewProperties();
        listMonth.addChangeListener(this::onOptionChange);

        listYear = new ListYearViewProperties();
        listYear.addChangeListener(this::onOptionChange);
    }
}
