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

public class ViewPropertiesLoader {

    protected ComponentLoader.Context context;
    protected LoaderSupport loaderSupport;

    public ViewPropertiesLoader(LoaderSupport loaderSupport, ComponentLoader.Context context) {
        this.loaderSupport = loaderSupport;
        this.context = context;
    }

    public void loadCalendarViewProperties(Element element, FullCalendar resultComponent) {
        Element viewsElement = element.element("viewProperties");
        if (viewsElement == null) {
            return;
        }
        viewsElement.elements()
                .forEach(v -> loadViewProperties(v, resultComponent));
    }

    public void loadCustomCalendarViews(Element element, FullCalendar resultComponent) {
        Element customViews = element.element("customViews");
        if (customViews == null) {
            return;
        }
        customViews.elements()
                .forEach(v -> resultComponent.addCustomCalendarView(loadCustomView(v, resultComponent)));
    }

    protected CustomCalendarView loadCustomView(Element viewElement, FullCalendar resultComponent) {
        String id = loaderSupport.loadString(viewElement, "id")
                .orElseThrow(() -> new IllegalStateException("Calendar custom view must have an ID"));
        CalendarView type = loaderSupport.loadString(viewElement, "type")
                .map(t -> getView(t, resultComponent))
                .orElse(GenericCalendarViewType.DAY_GRID);
        Integer dayCount = loaderSupport.loadInteger(viewElement, "dayCount").orElse(null);
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

    protected void loadViewProperties(Element viewElement, FullCalendar resultComponent) {
        if (viewElement.getName().contains("dayGrid")) {
            loadDayGridView(viewElement, resultComponent);
        } else if (viewElement.getName().contains("timeGrid")) {
            loadTimeGridViewProperties(viewElement, resultComponent);
        } else if (viewElement.getName().contains("list")) {
            loadListViewProperties(viewElement, resultComponent);
        } else if (viewElement.getName().contains("multiMonth")) {
            loadMultiMonthViewProperties(viewElement, resultComponent);
        } else {
            throw new GuiDevelopmentException("Unknown view element: " + viewElement.getName(), context);
        }
    }

    protected void loadDayGridView(Element dayGridElement, FullCalendar resultComponent) {
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
        loadBaseDayGridViewProperties(dayGridElement, dayGridDay);
        loadBaseViewProperties(dayGridElement, dayGridDay);
    }

    protected void loadDayGridWeekViewProperties(Element dayGridElement, DayGridWeekViewProperties dayGridWeek) {
        loadBaseDayGridViewProperties(dayGridElement, dayGridWeek);
        loadBaseViewProperties(dayGridElement, dayGridWeek);
    }

    protected void loadDayGridMonthViewProperties(Element dayGridElement, DayGridMonthViewProperties dayGridMonth) {
        loadBaseDayGridViewProperties(dayGridElement, dayGridMonth);

        loaderSupport.loadBoolean(dayGridElement, "fixedWeekCount", dayGridMonth::setFixedWeekCount);
        loaderSupport.loadBoolean(dayGridElement, "showNonCurrentDates", dayGridMonth::setShowNonCurrentDates);

        loadBaseViewProperties(dayGridElement, dayGridMonth);
    }

    protected void loadDayGridYearViewProperties(Element dayGridElement, DayGridYearViewProperties dayGridYear) {
        loadBaseDayGridViewProperties(dayGridElement, dayGridYear);

        loaderSupport.loadResourceString(dayGridElement, "monthStartFormat",
                context.getMessageGroup(), dayGridYear::setMonthStartFormat);

        loadBaseViewProperties(dayGridElement, dayGridYear);
    }

    protected void loadBaseDayGridViewProperties(Element dayGridElement,
                                                 AbstractDayGridViewProperties view) {
        loaderSupport.loadResourceString(dayGridElement, "dayPopoverFormat",
                context.getMessageGroup(), view::setDayPopoverFormat);
        loaderSupport.loadResourceString(dayGridElement, "dayHeaderFormat",
                context.getMessageGroup(), view::setDayHeaderFormat);
        loaderSupport.loadResourceString(dayGridElement, "weekNumberFormat",
                context.getMessageGroup(), view::setWeekNumberFormat);
        loaderSupport.loadResourceString(dayGridElement, "eventTimeFormat",
                context.getMessageGroup(), view::setEventTimeFormat);
        loaderSupport.loadBoolean(dayGridElement, "displayEventEnd", view::setDisplayEventEnd);
    }

    protected void loadTimeGridViewProperties(Element timeGridElement, FullCalendar resultComponent) {
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
        loaderSupport.loadResourceString(timeGridElement, "dayPopoverFormat",
                context.getMessageGroup(), view::setDayPopoverFormat);
        loaderSupport.loadResourceString(timeGridElement, "dayHeaderFormat",
                context.getMessageGroup(), view::setDayHeaderFormat);
        loaderSupport.loadResourceString(timeGridElement, "weekNumberFormat",
                context.getMessageGroup(), view::setWeekNumberFormat);
        loaderSupport.loadResourceString(timeGridElement, "eventTimeFormat",
                context.getMessageGroup(), view::setEventTimeFormat);
        loaderSupport.loadResourceString(timeGridElement, "slotLabelFormat",
                context.getMessageGroup(), view::setSlotLabelFormat);
        loaderSupport.loadInteger(timeGridElement, "eventMinHeight", view::setEventMinHeight);
        loaderSupport.loadInteger(timeGridElement, "eventShortHeight", view::setEventShortHeight);
        loaderSupport.loadBoolean(timeGridElement, "slotEventOverlap", view::setSlotEventOverlap);
        loaderSupport.loadBoolean(timeGridElement, "allDaySlot", view::setAllDaySlot);
        loaderSupport.loadBoolean(timeGridElement, "displayEventEnd", view::setDisplayEventEnd);
    }

    protected void loadListViewProperties(Element listElement, FullCalendar resultComponent) {
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
        loadBaseListViewProperties(listElement, view);
        loadBaseViewProperties(listElement, view);
    }

    protected void loadListWeekViewProperties(Element listElement, ListWeekViewProperties view) {
        loadBaseListViewProperties(listElement, view);
        loadBaseViewProperties(listElement, view);
    }

    protected void loadListMonthViewProperties(Element listElement, ListMonthViewProperties view) {
        loadBaseListViewProperties(listElement, view);
        loadBaseViewProperties(listElement, view);
    }

    protected void loadListYearViewProperties(Element listElement, ListYearViewProperties view) {
        loadBaseListViewProperties(listElement, view);
        loadBaseViewProperties(listElement, view);
    }

    protected void loadBaseListViewProperties(Element listElement, AbstractListViewProperties view) {
        loaderSupport.loadResourceString(listElement, "listDayFormat",
                context.getMessageGroup(), view::setListDayFormat);
        loaderSupport.loadResourceString(listElement, "listDaySideFormat",
                context.getMessageGroup(), view::setListDaySideFormat);
    }

    protected void loadMultiMonthViewProperties(Element multiMonthElement, FullCalendar resultComponent) {
        if (CalendarViewType.MULTI_MONTH_YEAR.getId().equals(multiMonthElement.getName())) {
            MultiMonthYearViewProperties view =
                    resultComponent.getCalendarViewProperties(CalendarViewType.MULTI_MONTH_YEAR);

            loadMultiMonthYearViewProperties(multiMonthElement, view);
        }
    }

    protected void loadMultiMonthYearViewProperties(Element multiMonthElement, MultiMonthYearViewProperties view) {
        loaderSupport.loadInteger(multiMonthElement, "multiMonthMaxColumns", view::setMultiMonthMaxColumns);
        loaderSupport.loadInteger(multiMonthElement, "multiMonthMinWidth", view::setMultiMonthMinWidth);
        loaderSupport.loadBoolean(multiMonthElement, "fixedWeekCount", view::setFixedWeekCount);
        loaderSupport.loadBoolean(multiMonthElement, "showNonCurrentDates", view::setShowNonCurrentDates);
        loaderSupport.loadResourceString(multiMonthElement, "multiMonthTitleFormat",
                context.getMessageGroup(), view::setMultiMonthTitleFormat);

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
        String propertyName = loaderSupport.loadString(propertyElement, "name")
                .orElseThrow(() -> new GuiDevelopmentException("Missing required 'name' attribute", context));

        String stringValue = loaderSupport.loadString(propertyElement, "value")
                .orElseThrow(() -> new GuiDevelopmentException("Missing required 'value' attribute", context));

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
        return CalendarDuration.ofYears(loaderSupport.loadInteger(element, "years").orElse(0))
                .plusMonths(loaderSupport.loadInteger(element, "months").orElse(0))
                .plusWeeks(loaderSupport.loadInteger(element, "weeks").orElse(0))
                .plusDays(loaderSupport.loadInteger(element, "days").orElse(0))
                .plusHours(loaderSupport.loadInteger(element, "hours").orElse(0))
                .plusMinutes(loaderSupport.loadInteger(element, "minutes").orElse(0))
                .plusSeconds(loaderSupport.loadInteger(element, "seconds").orElse(0))
                .plusMilliseconds(loaderSupport.loadInteger(element, "milliseconds").orElse(0));
    }

    protected CalendarView getView(String view, FullCalendar resultComponent) {
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
