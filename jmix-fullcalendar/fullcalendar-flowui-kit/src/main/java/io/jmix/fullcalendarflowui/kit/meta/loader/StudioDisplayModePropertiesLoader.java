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

import static io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayModes.*;

public class StudioDisplayModePropertiesLoader implements HasLoaderSupport {

    public void loadCalendarDisplayModeProperties(Element element, JmixFullCalendar resultComponent) {
        Element propertiesElement = element.element("displayModeProperties");
        if (propertiesElement == null) {
            return;
        }
        propertiesElement.elements()
                .forEach(v -> loadDisplayModeProperties(v, resultComponent));
    }

    public void loadCustomCalendarDisplayModes(Element element, JmixFullCalendar resultComponent) {
        Element customModesElement = element.element("customDisplayModes");
        if (customModesElement == null) {
            return;
        }
        customModesElement.elements()
                .forEach(v -> resultComponent.addCustomCalendarDisplayMode(loadCustomDisplayMode(v, resultComponent)));
    }

    protected CustomCalendarDisplayMode loadCustomDisplayMode(Element displayModeElement, JmixFullCalendar resultComponent) {
        String id = loadString(displayModeElement, "id")
                .orElseThrow(() -> new IllegalStateException("Custom calendar display mode must have an ID"));
        CalendarDisplayMode type = loadString(displayModeElement, "type")
                .map(t -> getDisplayMode(t, resultComponent))
                .orElse(GenericCalendarDisplayModes.DAY_GRID);
        Integer dayCount = loadInteger(displayModeElement, "dayCount").orElse(null);
        CalendarDuration calendarDuration = null;

        Element durartionElement = displayModeElement.element("duration");
        if (durartionElement != null) {
            calendarDuration = loadCalendarDuration(durartionElement);
        }

        CustomCalendarDisplayMode customCalendarDisplayMode = new CustomCalendarDisplayMode(id, type)
                .withDayCount(dayCount)
                .withDuration(calendarDuration);

        loadBaseProperties(displayModeElement, customCalendarDisplayMode);

        return customCalendarDisplayMode;
    }

    protected void loadDisplayModeProperties(Element displayModeElement, JmixFullCalendar resultComponent) {
        if (displayModeElement.getName().contains("dayGrid")) {
            loadDayGridProperties(displayModeElement, resultComponent);
        } else if (displayModeElement.getName().contains("timeGrid")) {
            loadTimeGridProperties(displayModeElement, resultComponent);
        } else if (displayModeElement.getName().contains("list")) {
            loadListProperties(displayModeElement, resultComponent);
        } else if (displayModeElement.getName().contains("multiMonth")) {
            loadMultiMonthProperties(displayModeElement, resultComponent);
        }
    }

    protected void loadDayGridProperties(Element dayGridElement, JmixFullCalendar resultComponent) {
        if (DAY_GRID_DAY.getId().equals(dayGridElement.getName())) {
            loadDayGridDayProperties(dayGridElement,
                    resultComponent.getCalendarDisplayModeProperties(DAY_GRID_DAY));
        } else if (DAY_GRID_WEEK.getId().equals(dayGridElement.getName())) {
            loadDayGridWeekProperties(dayGridElement,
                    resultComponent.getCalendarDisplayModeProperties(DAY_GRID_WEEK));
        } else if (DAY_GRID_MONTH.getId().equals(dayGridElement.getName())) {
            loadDayGridMonthProperties(dayGridElement,
                    resultComponent.getCalendarDisplayModeProperties(DAY_GRID_MONTH));
        } else if (DAY_GRID_YEAR.getId().equals(dayGridElement.getName())) {
            loadDayGridYearProperties(dayGridElement,
                    resultComponent.getCalendarDisplayModeProperties(DAY_GRID_YEAR));
        }
    }

    protected void loadDayGridDayProperties(Element dayGridElement, DayGridDayProperties dayGridDay) {
        loadBaseProperties(dayGridElement, dayGridDay);
    }

    protected void loadDayGridWeekProperties(Element dayGridElement, DayGridWeekProperties dayGridWeek) {
        loadBaseProperties(dayGridElement, dayGridWeek);
    }

    protected void loadDayGridMonthProperties(Element dayGridElement, DayGridMonthProperties dayGridMonth) {
        loadBoolean(dayGridElement, "fixedWeekCount", dayGridMonth::setFixedWeekCount);
        loadBoolean(dayGridElement, "showNonCurrentDates", dayGridMonth::setFixedWeekCount);

        loadBaseProperties(dayGridElement, dayGridMonth);
    }

    protected void loadDayGridYearProperties(Element dayGridElement, DayGridYearProperties dayGridYear) {
        loadBaseProperties(dayGridElement, dayGridYear);
    }

    protected void loadTimeGridProperties(Element timeGridElement, JmixFullCalendar resultComponent) {
        if (TIME_GRID_DAY.getId().equals(timeGridElement.getName())) {
            loadTimeGridDayProperties(timeGridElement,
                    resultComponent.getCalendarDisplayModeProperties(TIME_GRID_DAY));
        } else if (TIME_GRID_WEEK.getId().equals(timeGridElement.getName())) {
            loadTimeGridWeekProperties(timeGridElement,
                    resultComponent.getCalendarDisplayModeProperties(TIME_GRID_WEEK));
        }
    }

    protected void loadTimeGridDayProperties(Element timeGridElement, TimeGridDayProperties timeGridDay) {
        loadBaseTimeGridProperties(timeGridElement, timeGridDay);
        loadBaseProperties(timeGridElement, timeGridDay);
    }

    protected void loadTimeGridWeekProperties(Element timeGridElement, TimeGridWeekProperties timeGridWeek) {
        loadBaseTimeGridProperties(timeGridElement, timeGridWeek);
        loadBaseProperties(timeGridElement, timeGridWeek);
    }

    protected void loadBaseTimeGridProperties(Element timeGridElement,
                                              AbstractTimeGridProperties timeGridProperties) {
        loadInteger(timeGridElement, "eventMinHeight", timeGridProperties::setEventMinHeight);
        loadInteger(timeGridElement, "eventShortHeight", timeGridProperties::setEventShortHeight);
        loadBoolean(timeGridElement, "slotEventOverlap", timeGridProperties::setSlotEventOverlap);
        loadBoolean(timeGridElement, "allDaySlot", timeGridProperties::setAllDaySlot);
    }

    protected void loadListProperties(Element listElement, JmixFullCalendar resultComponent) {
        if (LIST_DAY.getId().equals(listElement.getName())) {
            loadListDayProperties(listElement, resultComponent.getCalendarDisplayModeProperties(LIST_DAY));
        } else if (LIST_WEEK.getId().equals(listElement.getName())) {
            loadListWeekProperties(listElement, resultComponent.getCalendarDisplayModeProperties(LIST_WEEK));
        } else if (LIST_MONTH.getId().equals(listElement.getName())) {
            loadListMonthProperties(listElement, resultComponent.getCalendarDisplayModeProperties(LIST_MONTH));
        } else if (LIST_YEAR.getId().equals(listElement.getName())) {
            loadListYearProperties(listElement, resultComponent.getCalendarDisplayModeProperties(LIST_YEAR));
        }
    }

    protected void loadListDayProperties(Element listElement, ListDayProperties properties) {
        loadBaseProperties(listElement, properties);
        loadBaseListProperties(listElement, properties);
    }

    protected void loadListWeekProperties(Element listElement, ListWeekProperties properties) {
        loadBaseProperties(listElement, properties);
        loadBaseListProperties(listElement, properties);
    }

    protected void loadListMonthProperties(Element listElement, ListMonthProperties properties) {
        loadBaseProperties(listElement, properties);
        loadBaseListProperties(listElement, properties);
    }

    protected void loadListYearProperties(Element listElement, ListYearProperties properties) {
        loadBaseProperties(listElement, properties);
        loadBaseListProperties(listElement, properties);
    }

    protected void loadBaseListProperties(Element listElement, AbstractListProperties properties) {
        loadBoolean(listElement, "listDaySideVisible", properties::setListDaySideVisible);
        loadBoolean(listElement, "listDayVisible", properties::setListDayVisible);
    }

    protected void loadMultiMonthProperties(Element multiMonthElement, JmixFullCalendar resultComponent) {
        if (MULTI_MONTH_YEAR.getId().equals(multiMonthElement.getName())) {
            loadMultiMonthYearProperties(multiMonthElement,
                    resultComponent.getCalendarDisplayModeProperties(MULTI_MONTH_YEAR));
        }
    }

    protected void loadMultiMonthYearProperties(Element multiMonthElement, MultiMonthYearProperties properties) {
        loadInteger(multiMonthElement, "multiMonthMaxColumns", properties::setMultiMonthMaxColumns);
        loadInteger(multiMonthElement, "multiMonthMinWidth", properties::setMultiMonthMinWidth);
        loadBoolean(multiMonthElement, "fixedWeekCount", properties::setFixedWeekCount);
        loadBoolean(multiMonthElement, "showNonCurrentDates", properties::setShowNonCurrentDates);

        loadBaseProperties(multiMonthElement, properties);
    }

    protected void loadBaseProperties(Element displayModeElement,
                                      AbstractCalendarDisplayModeProperties displayModeProperties) {
        Element propertiesElement = displayModeElement.element("properties");
        if (propertiesElement == null) {
            return;
        }

        List<Element> properties = propertiesElement.elements("property");
        properties.forEach(e -> loadProperty(e, displayModeProperties));
    }

    protected void loadProperty(Element propertyElement, AbstractCalendarDisplayModeProperties properties) {
        String propertyName = loadString(propertyElement, "name").orElse(null);

        String stringValue = loadString(propertyElement, "value").orElse(null);

        if (propertyName == null || stringValue == null) {
            return;
        }

        properties.addProperty(propertyName, parsePropertyValue(stringValue));
    }

    protected Object parsePropertyValue(String value) {
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

    protected CalendarDisplayMode getDisplayMode(String id, JmixFullCalendar resultComponent) {
        try {
            return CalendarDisplayModes.valueOf(id);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        try {
            return GenericCalendarDisplayModes.valueOf(id);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        for (CustomCalendarDisplayMode customMode : resultComponent.getCustomCalendarDisplayModes()) {
            if (customMode.getDisplayMode().getId().equals(id)) {
                return customMode.getDisplayMode();
            }
        }
        return () -> id;
    }
}
