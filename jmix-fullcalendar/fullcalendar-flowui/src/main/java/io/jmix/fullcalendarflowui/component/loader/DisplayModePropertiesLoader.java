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

package io.jmix.fullcalendarflowui.component.loader;

import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.model.*;
import org.dom4j.Element;

import java.util.List;

import static io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayModes.*;

public class DisplayModePropertiesLoader {

    protected ComponentLoader.Context context;
    protected LoaderSupport loaderSupport;

    public DisplayModePropertiesLoader(LoaderSupport loaderSupport, ComponentLoader.Context context) {
        this.loaderSupport = loaderSupport;
        this.context = context;
    }

    public void loadCalendarDisplayModeProperties(Element element, FullCalendar resultComponent) {
        Element displayModeProperties = element.element("displayModeProperties");
        if (displayModeProperties == null) {
            return;
        }
        displayModeProperties.elements()
                .forEach(v -> loadDisplayModeProperties(v, resultComponent));
    }

    public void loadCustomCalendarDisplayModes(Element element, FullCalendar resultComponent) {
        Element customDisplayModes = element.element("customDisplayModes");
        if (customDisplayModes == null) {
            return;
        }
        customDisplayModes.elements()
                .forEach(v -> resultComponent.addCustomCalendarDisplayMode(loadCustomDisplayMode(v, resultComponent)));
    }

    protected CustomCalendarDisplayMode loadCustomDisplayMode(Element displayModeElement,
                                                              FullCalendar resultComponent) {
        String id = loaderSupport.loadString(displayModeElement, "id")
                .orElseThrow(() -> new IllegalStateException("Calendar custom display mode must have an ID"));

        CalendarDisplayMode type = loaderSupport.loadString(displayModeElement, "type")
                .map(t -> getDisplayMode(t, resultComponent))
                .orElse(GenericCalendarDisplayModes.DAY_GRID);

        Integer dayCount = loaderSupport.loadInteger(displayModeElement, "dayCount").orElse(null);
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

    protected void loadDisplayModeProperties(Element displayModeElement, FullCalendar resultComponent) {
        if (displayModeElement.getName().contains("dayGrid")) {
            loadDayGridProperties(displayModeElement, resultComponent);
        } else if (displayModeElement.getName().contains("timeGrid")) {
            loadTimeGridProperties(displayModeElement, resultComponent);
        } else if (displayModeElement.getName().contains("list")) {
            loadListProperties(displayModeElement, resultComponent);
        } else if (displayModeElement.getName().contains("multiMonth")) {
            loadMultiMonthProperties(displayModeElement, resultComponent);
        } else {
            throw new GuiDevelopmentException("Unknown display mode element: " + displayModeElement.getName(), context);
        }
    }

    protected void loadDayGridProperties(Element dayGridElement, FullCalendar resultComponent) {
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
        loadBaseDayGridProperties(dayGridElement, dayGridDay);
        loadBaseProperties(dayGridElement, dayGridDay);
    }

    protected void loadDayGridWeekProperties(Element dayGridElement, DayGridWeekProperties dayGridWeek) {
        loadBaseDayGridProperties(dayGridElement, dayGridWeek);
        loadBaseProperties(dayGridElement, dayGridWeek);
    }

    protected void loadDayGridMonthProperties(Element dayGridElement, DayGridMonthProperties dayGridMonth) {
        loadBaseDayGridProperties(dayGridElement, dayGridMonth);

        loaderSupport.loadBoolean(dayGridElement, "fixedWeekCount",
                dayGridMonth::setFixedWeekCount);
        loaderSupport.loadBoolean(dayGridElement, "showNonCurrentDates",
                dayGridMonth::setShowNonCurrentDates);

        loadBaseProperties(dayGridElement, dayGridMonth);
    }

    protected void loadDayGridYearProperties(Element dayGridElement, DayGridYearProperties dayGridYear) {
        loadBaseDayGridProperties(dayGridElement, dayGridYear);

        loaderSupport.loadResourceString(dayGridElement, "monthStartFormat",
                context.getMessageGroup(), dayGridYear::setMonthStartFormat);

        loadBaseProperties(dayGridElement, dayGridYear);
    }

    protected void loadBaseDayGridProperties(Element dayGridElement, AbstractDayGridProperties dayGrid) {
        loaderSupport.loadResourceString(dayGridElement, "dayPopoverFormat",
                context.getMessageGroup(), dayGrid::setDayPopoverFormat);
        loaderSupport.loadResourceString(dayGridElement, "dayHeaderFormat",
                context.getMessageGroup(), dayGrid::setDayHeaderFormat);
        loaderSupport.loadResourceString(dayGridElement, "weekNumberFormat",
                context.getMessageGroup(), dayGrid::setWeekNumberFormat);
        loaderSupport.loadResourceString(dayGridElement, "eventTimeFormat",
                context.getMessageGroup(), dayGrid::setEventTimeFormat);
        loaderSupport.loadBoolean(dayGridElement, "displayEventEnd", dayGrid::setDisplayEventEnd);
    }

    protected void loadTimeGridProperties(Element timeGridElement, FullCalendar resultComponent) {
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
                                              AbstractTimeGridProperties timeGrid) {
        loaderSupport.loadResourceString(timeGridElement, "dayPopoverFormat",
                context.getMessageGroup(), timeGrid::setDayPopoverFormat);
        loaderSupport.loadResourceString(timeGridElement, "dayHeaderFormat",
                context.getMessageGroup(), timeGrid::setDayHeaderFormat);
        loaderSupport.loadResourceString(timeGridElement, "weekNumberFormat",
                context.getMessageGroup(), timeGrid::setWeekNumberFormat);
        loaderSupport.loadResourceString(timeGridElement, "eventTimeFormat",
                context.getMessageGroup(), timeGrid::setEventTimeFormat);
        loaderSupport.loadResourceString(timeGridElement, "slotLabelFormat",
                context.getMessageGroup(), timeGrid::setSlotLabelFormat);
        loaderSupport.loadInteger(timeGridElement, "eventMinHeight", timeGrid::setEventMinHeight);
        loaderSupport.loadInteger(timeGridElement, "eventShortHeight", timeGrid::setEventShortHeight);
        loaderSupport.loadBoolean(timeGridElement, "slotEventOverlap", timeGrid::setSlotEventOverlap);
        loaderSupport.loadBoolean(timeGridElement, "allDaySlot", timeGrid::setAllDaySlot);
        loaderSupport.loadBoolean(timeGridElement, "displayEventEnd", timeGrid::setDisplayEventEnd);
    }

    protected void loadListProperties(Element listElement, FullCalendar resultComponent) {
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

    protected void loadListDayProperties(Element listElement, ListDayProperties listDay) {
        loadBaseListProperties(listElement, listDay);
        loadBaseProperties(listElement, listDay);
    }

    protected void loadListWeekProperties(Element listElement, ListWeekProperties listWeek) {
        loadBaseListProperties(listElement, listWeek);
        loadBaseProperties(listElement, listWeek);
    }

    protected void loadListMonthProperties(Element listElement, ListMonthProperties listMonth) {
        loadBaseListProperties(listElement, listMonth);
        loadBaseProperties(listElement, listMonth);
    }

    protected void loadListYearProperties(Element listElement, ListYearProperties listYear) {
        loadBaseListProperties(listElement, listYear);
        loadBaseProperties(listElement, listYear);
    }

    protected void loadBaseListProperties(Element listElement, AbstractListProperties baseList) {
        loaderSupport.loadResourceString(listElement, "listDayFormat",
                context.getMessageGroup(), baseList::setListDayFormat);
        loaderSupport.loadResourceString(listElement, "listDaySideFormat",
                context.getMessageGroup(), baseList::setListDaySideFormat);
        loaderSupport.loadBoolean(listElement, "listDaySideVisible", baseList::setListDaySideVisible);
        loaderSupport.loadBoolean(listElement, "listDayVisible", baseList::setListDayVisible);
    }

    protected void loadMultiMonthProperties(Element multiMonthElement, FullCalendar resultComponent) {
        if (CalendarDisplayModes.MULTI_MONTH_YEAR.getId().equals(multiMonthElement.getName())) {
            MultiMonthYearProperties multiMonthYear =
                    resultComponent.getCalendarDisplayModeProperties(CalendarDisplayModes.MULTI_MONTH_YEAR);

            loadMultiMonthYearProperties(multiMonthElement, multiMonthYear);
        }
    }

    protected void loadMultiMonthYearProperties(Element multiMonthElement,
                                                MultiMonthYearProperties multiMonthYear) {
        loaderSupport.loadInteger(multiMonthElement, "multiMonthMaxColumns",
                multiMonthYear::setMultiMonthMaxColumns);
        loaderSupport.loadInteger(multiMonthElement, "multiMonthMinWidth",
                multiMonthYear::setMultiMonthMinWidth);
        loaderSupport.loadBoolean(multiMonthElement, "fixedWeekCount",
                multiMonthYear::setFixedWeekCount);
        loaderSupport.loadBoolean(multiMonthElement, "showNonCurrentDates",
                multiMonthYear::setShowNonCurrentDates);
        loaderSupport.loadResourceString(multiMonthElement, "multiMonthTitleFormat",
                context.getMessageGroup(), multiMonthYear::setMultiMonthTitleFormat);

        loadBaseProperties(multiMonthElement, multiMonthYear);
    }

    protected void loadBaseProperties(Element displayModeElement, AbstractCalendarDisplayModeProperties displayMode) {
        Element propertiesElement = displayModeElement.element("properties");
        if (propertiesElement == null) {
            return;
        }

        List<Element> properties = propertiesElement.elements("property");
        properties.forEach(e -> loadBaseProperty(e, displayMode));
    }

    protected void loadBaseProperty(Element propertyElement, AbstractCalendarDisplayModeProperties displayMode) {
        String propertyName = loaderSupport.loadString(propertyElement, "name")
                .orElseThrow(() -> new GuiDevelopmentException("Missing required 'name' attribute", context));

        String stringValue = loaderSupport.loadString(propertyElement, "value")
                .orElseThrow(() -> new GuiDevelopmentException("Missing required 'value' attribute", context));

        displayMode.addProperty(propertyName, parseBasePropertyValue(stringValue));
    }

    protected Object parseBasePropertyValue(String value) {
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
        return CalendarDuration.ofYears(loaderSupport.loadInteger(element, "years").orElse(0))
                .plusMonths(loaderSupport.loadInteger(element, "months").orElse(0))
                .plusWeeks(loaderSupport.loadInteger(element, "weeks").orElse(0))
                .plusDays(loaderSupport.loadInteger(element, "days").orElse(0))
                .plusHours(loaderSupport.loadInteger(element, "hours").orElse(0))
                .plusMinutes(loaderSupport.loadInteger(element, "minutes").orElse(0))
                .plusSeconds(loaderSupport.loadInteger(element, "seconds").orElse(0))
                .plusMilliseconds(loaderSupport.loadInteger(element, "milliseconds").orElse(0));
    }

    protected CalendarDisplayMode getDisplayMode(String displayModeId, FullCalendar resultComponent) {
        try {
            return CalendarDisplayModes.valueOf(displayModeId);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        try {
            return GenericCalendarDisplayModes.valueOf(displayModeId);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        for (CustomCalendarDisplayMode customDisplayMode : resultComponent.getCustomCalendarDisplayModes()) {
            if (customDisplayMode.getDisplayMode().getId().equals(displayModeId)) {
                return customDisplayMode.getDisplayMode();
            }
        }
        return () -> displayModeId;
    }
}
