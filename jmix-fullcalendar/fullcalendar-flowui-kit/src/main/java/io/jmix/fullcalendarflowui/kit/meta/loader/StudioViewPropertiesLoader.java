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

package io.jmix.fullcalendarflowui.kit.meta.loader;

import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;
import io.jmix.fullcalendarflowui.kit.component.model.*;
import org.dom4j.Element;

import java.util.List;

public class StudioViewPropertiesLoader implements HasLoaderSupport {

    public void loadCalendarViewProperties(Element element, JmixFullCalendar resultComponent) {
        Element viewsElement = element.element("viewProperties");
        if (viewsElement == null) {
            return;
        }
        viewsElement.elements()
                .forEach(v -> loadViewProperties(v, resultComponent));
    }

    public void loadCustomCalendarViews(Element element, JmixFullCalendar resultComponent) {
        Element customViews = element.element("customViews");
        if (customViews == null) {
            return;
        }
        customViews.elements()
                .forEach(v -> resultComponent.addCustomCalendarView(loadCustomView(v, resultComponent)));
    }

    protected CustomCalendarView loadCustomView(Element viewElement, JmixFullCalendar resultComponent) {
        String id = loadString(viewElement, "id")
                .orElseThrow(() -> new IllegalStateException("Calendar custom view must have an ID"));
        CalendarView type = loadString(viewElement, "type")
                .map(t -> getView(t, resultComponent))
                .orElse(GenericCalendarViewType.DAY_GRID);
        Integer dayCount = loadInteger(viewElement, "dayCount").orElse(null);
        CalendarDuration calendarDuration = null;

        Element durartionElement = viewElement.element("duration");
        if (durartionElement != null) {
            calendarDuration = loadCalendarDuration(durartionElement);
        }

        CustomCalendarView customCalendarView = new CustomCalendarView(id, type)
                .withDayCount(dayCount)
                .withDuration(calendarDuration);

        loadBaseViewProperties(viewElement, customCalendarView);

        return customCalendarView;
    }

    protected void loadViewProperties(Element viewElement, JmixFullCalendar resultComponent) {
        if (viewElement.getName().contains("dayGrid")) {
            loadDayGridView(viewElement, resultComponent);
        } else if (viewElement.getName().contains("timeGrid")) {
            loadTimeGridViewProperties(viewElement, resultComponent);
        } else if (viewElement.getName().contains("list")) {
            loadListViewProperties(viewElement, resultComponent);
        } else if (viewElement.getName().contains("multiMonth")) {
            loadMultiMonthViewProperties(viewElement, resultComponent);
        }
    }

    protected void loadDayGridView(Element dayGridElement, JmixFullCalendar resultComponent) {
        if (CalendarViewType.DAY_GRID_DAY.getId().equals(dayGridElement.getName())) {
            DayGridDayViewProperties dayGridDay
                    = resultComponent.getCalendarViewProperties(CalendarViewType.DAY_GRID_DAY);
            loadDayGridDayViewProperties(dayGridElement, dayGridDay);
        } else if (CalendarViewType.DAY_GRID_WEEK.getId().equals(dayGridElement.getName())) {
            DayGridWeekViewProperties dayGridWeek
                    = resultComponent.getCalendarViewProperties(CalendarViewType.DAY_GRID_WEEK);
            loadDayGridWeekViewProperties(dayGridElement, dayGridWeek);
        } else if (CalendarViewType.DAY_GRID_MONTH.getId().equals(dayGridElement.getName())) {
            DayGridMonthViewProperties dayGridMonth
                    = resultComponent.getCalendarViewProperties(CalendarViewType.DAY_GRID_MONTH);
            loadDayGridMonthViewProperties(dayGridElement, dayGridMonth);
        } else if (CalendarViewType.DAY_GRID_YEAR.getId().equals(dayGridElement.getName())) {
            DayGridYearViewProperties dayGridYear
                    = resultComponent.getCalendarViewProperties(CalendarViewType.DAY_GRID_YEAR);
            loadDayGridYearViewProperties(dayGridElement, dayGridYear);
        }
    }

    protected void loadDayGridDayViewProperties(Element dayGridElement, DayGridDayViewProperties dayGridDay) {
        loadBaseViewProperties(dayGridElement, dayGridDay);
    }

    protected void loadDayGridWeekViewProperties(Element dayGridElement, DayGridWeekViewProperties dayGridWeek) {
        loadBaseViewProperties(dayGridElement, dayGridWeek);
    }

    protected void loadDayGridMonthViewProperties(Element dayGridElement, DayGridMonthViewProperties dayGridMonth) {
        loadBoolean(dayGridElement, "fixedWeekCount", dayGridMonth::setFixedWeekCount);
        loadBoolean(dayGridElement, "showNonCurrentDates", dayGridMonth::setFixedWeekCount);

        loadBaseViewProperties(dayGridElement, dayGridMonth);
    }

    protected void loadDayGridYearViewProperties(Element dayGridElement, DayGridYearViewProperties dayGridYear) {
        loadBaseViewProperties(dayGridElement, dayGridYear);
    }

    protected void loadTimeGridViewProperties(Element timeGridElement, JmixFullCalendar resultComponent) {
        if (CalendarViewType.TIME_GRID_DAY.getId().equals(timeGridElement.getName())) {
            TimeGridDayViewProperties timeGridDay
                    = resultComponent.getCalendarViewProperties(CalendarViewType.TIME_GRID_DAY);

            loadTimeGridDayViewProperties(timeGridElement, timeGridDay);
        } else if (CalendarViewType.TIME_GRID_WEEK.getId().equals(timeGridElement.getName())) {
            TimeGridWeekViewProperties timeGridWeek
                    = resultComponent.getCalendarViewProperties(CalendarViewType.TIME_GRID_WEEK);

            loadTimeGridWeekViewProperties(timeGridElement, timeGridWeek);
        }
    }

    protected void loadTimeGridDayViewProperties(Element timeGridElement, TimeGridDayViewProperties timeGridDay) {
        loadBaseTimeGridViewProperties(timeGridElement, timeGridDay);
        loadBaseViewProperties(timeGridElement, timeGridDay);
    }

    protected void loadTimeGridWeekViewProperties(Element timeGridElement, TimeGridWeekViewProperties timeGridWeek) {
        loadBaseTimeGridViewProperties(timeGridElement, timeGridWeek);
        loadBaseViewProperties(timeGridElement, timeGridWeek);
    }

    protected void loadBaseTimeGridViewProperties(Element timeGridElement,
                                                  AbstractTimeGridViewProperties view) {
        loadInteger(timeGridElement, "eventMinHeight", view::setEventMinHeight);
        loadInteger(timeGridElement, "eventShortHeight", view::setEventShortHeight);
        loadBoolean(timeGridElement, "slotEventOverlap", view::setSlotEventOverlap);
        loadBoolean(timeGridElement, "allDaySlot", view::setAllDaySlot);
    }

    protected void loadListViewProperties(Element listElement, JmixFullCalendar resultComponent) {
        if (CalendarViewType.LIST_DAY.getId().equals(listElement.getName())) {
            ListDayViewProperties listDay
                    = resultComponent.getCalendarViewProperties(CalendarViewType.LIST_DAY);

            loadListDayViewProperties(listElement, listDay);
        } else if (CalendarViewType.LIST_WEEK.getId().equals(listElement.getName())) {
            ListWeekViewProperties listWeek
                    = resultComponent.getCalendarViewProperties(CalendarViewType.LIST_WEEK);

            loadListWeekViewProperties(listElement, listWeek);
        } else if (CalendarViewType.LIST_MONTH.getId().equals(listElement.getName())) {
            ListMonthViewProperties listMonth
                    = resultComponent.getCalendarViewProperties(CalendarViewType.LIST_MONTH);

            loadListMonthViewProperties(listElement, listMonth);
        } else if (CalendarViewType.LIST_YEAR.getId().equals(listElement.getName())) {
            ListYearViewProperties listYear
                    = resultComponent.getCalendarViewProperties(CalendarViewType.LIST_YEAR);

            loadListYearViewProperties(listElement, listYear);
        }
    }

    protected void loadListDayViewProperties(Element listElement, ListDayViewProperties view) {
        loadBaseViewProperties(listElement, view);
        loadBaseListViewProperties(listElement, view);
    }

    protected void loadListWeekViewProperties(Element listElement, ListWeekViewProperties view) {
        loadBaseViewProperties(listElement, view);
        loadBaseListViewProperties(listElement, view);
    }

    protected void loadListMonthViewProperties(Element listElement, ListMonthViewProperties view) {
        loadBaseViewProperties(listElement, view);
        loadBaseListViewProperties(listElement, view);
    }

    protected void loadListYearViewProperties(Element listElement, ListYearViewProperties view) {
        loadBaseViewProperties(listElement, view);
        loadBaseListViewProperties(listElement, view);
    }

    protected void loadBaseListViewProperties(Element listElement, AbstractListViewProperties view) {
        loadBoolean(listElement, "listDaySideVisible", view::setListDaySideVisible);
        loadBoolean(listElement, "listDayVisible", view::setListDayVisible);
    }

    protected void loadMultiMonthViewProperties(Element multiMonthElement, JmixFullCalendar resultComponent) {
        if (CalendarViewType.MULTI_MONTH_YEAR.getId().equals(multiMonthElement.getName())) {
            MultiMonthYearViewProperties view =
                    resultComponent.getCalendarViewProperties(CalendarViewType.MULTI_MONTH_YEAR);

            loadMultiMonthYearViewProperties(multiMonthElement, view);
        }
    }

    protected void loadMultiMonthYearViewProperties(Element multiMonthElement, MultiMonthYearViewProperties view) {
        loadInteger(multiMonthElement, "multiMonthMaxColumns", view::setMultiMonthMaxColumns);
        loadInteger(multiMonthElement, "multiMonthMinWidth", view::setMultiMonthMinWidth);
        loadBoolean(multiMonthElement, "fixedWeekCount", view::setFixedWeekCount);
        loadBoolean(multiMonthElement, "showNonCurrentDates", view::setShowNonCurrentDates);

        loadBaseViewProperties(multiMonthElement, view);
    }

    protected void loadBaseViewProperties(Element viewElement, AbstractCalendarViewProperties view) {
        Element propertiesElement = viewElement.element("properties");
        if (propertiesElement == null) {
            return;
        }

        List<Element> properties = propertiesElement.elements("property");
        properties.forEach(e -> loadBaseViewProperty(e, view));
    }

    protected void loadBaseViewProperty(Element propertyElement, AbstractCalendarViewProperties view) {
        String propertyName = loadString(propertyElement, "name").orElse(null);

        String stringValue = loadString(propertyElement, "value").orElse(null);

        if (propertyName == null || stringValue == null) {
            return;
        }

        view.addProperty(propertyName, parseBaseViewPropertyValue(stringValue));
    }

    protected Object parseBaseViewPropertyValue(String value) {
        if (Boolean.TRUE.toString().equals(value)) {
            return Boolean.TRUE;
        } else if (Boolean.FALSE.toString().equals(value)) {
            return Boolean.FALSE;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // Do nothing
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // Do nothing
        }
        return value;
    }

    protected CalendarDuration loadCalendarDuration(Element element) {
        return CalendarDuration.ofYears(loadInteger(element, "years").orElse(0))
                .plusMonths(loadInteger(element, "months").orElse(0))
                .plusWeeks(loadInteger(element, "weeks").orElse(0))
                .plusDays(loadInteger(element, "days").orElse(0))
                .plusHours(loadInteger(element, "hours").orElse(0))
                .plusMinutes(loadInteger(element, "minutes").orElse(0))
                .plusSeconds(loadInteger(element, "seconds").orElse(0))
                .plusMilliseconds(loadInteger(element, "milliseconds").orElse(0));
    }

    protected CalendarView getView(String view, JmixFullCalendar resultComponent) {
        try {
            return CalendarViewType.valueOf(view);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        try {
            return GenericCalendarViewType.valueOf(view);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        for (CustomCalendarView customView : resultComponent.getCustomCalendarViews()) {
            if (customView.getCalendarView().getId().equals(view)) {
                return customView.getCalendarView();
            }
        }
        return () -> view;
    }
}
