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

package io.jmix.fullcalendarflowui.kit.meta;

import io.jmix.flowui.kit.meta.*;

@StudioUiKit(studioClassloaderDependencies = "io.jmix.fullcalendar:jmix-fullcalendar-flowui-kit")
public interface StudioFullCalendarElements {

    @StudioElement(
            name = "ContainerEventProvider",
            xmlElement = "containerEventProvider",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.component.data.ContainerCalendarEventProvider",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF, required = true),
                    @StudioProperty(xmlAttribute = "groupId", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "allDay", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "startDateTime", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "endDateTime", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "interactive", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "startEditable", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "durationEditable", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "display", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlap", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "constraint", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "backgroundColor", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "borderColor", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "textColor", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "additionalProperties", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "recurringDaysOfWeek", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "recurringStartTime", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "recurringEndTime", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "recurringStartDate", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "recurringEndDate", type = StudioPropertyType.STRING),
            }
    )
    void containerEventProvider();

    @StudioElement(
            name = "LazyEventProvider",
            xmlElement = "lazyEventProvider",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.component.data.LazyEntityCalendarEventRetriever",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "groupId", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "allDay", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "startDateTime", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "endDateTime", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "interactive", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "startEditable", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "durationEditable", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "display", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "overlap", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "constraint", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "backgroundColor", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "borderColor", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "textColor", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "additionalProperties", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "recurringDaysOfWeek", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "recurringStartTime", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "recurringEndTime", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "recurringStartDate", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "recurringEndDate", type = StudioPropertyType.STRING),
            }
    )
    void lazyEventProvider();

    @StudioElement(
            name = "ItemsQuery",
            xmlElement = "itemsQuery",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            icon = "io/jmix/flowui/kit/meta/icon/element/itemsQuery.svg",
            target = "io.jmix.fullcalendarflowui.component.data.LazyEntityCalendarEventRetriever",
            properties = {
                    @StudioProperty(xmlAttribute = "class", type = StudioPropertyType.ENTITY_CLASS, required = true),
                    @StudioProperty(xmlAttribute = "query", type = StudioPropertyType.JPA_QUERY),
                    @StudioProperty(xmlAttribute = "fetchPlan", type = StudioPropertyType.FETCH_PLAN)
            }
    )
    void entityItemsQuery();

    @StudioElement(
            name = "View",
            xmlElement = "view",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.CustomCalendarView",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.STRING,
                            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.GenericCalendarViewType",
                            options = {"DAY_GRID", "TIME_GRID", "LIST", "MULTI_MONTH"},
                            defaultValue = "DAY_GRID"),
                    @StudioProperty(xmlAttribute = "dayCount", type = StudioPropertyType.INTEGER)
            },
            unsupportedTarget = "tag:viewProperties"
    )
    void customView();

    @StudioElement(
            name = "View properties",
            xmlElement = "viewProperties",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarViewProperties",
            target = "io.jmix.fullcalendarflowui.component.FullCalendar",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg"
    )
    void viewProperties();

    @StudioElement(
            name = "DayGridDay",
            xmlElement = "dayGridDay",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.DayGridDayViewProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarViewProperties",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "dayHeaderFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "dayPopoverFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventTimeFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "weekNumberFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "displayEventEnd", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void dayGridDay();

    @StudioElement(
            name = "DayGridWeek",
            xmlElement = "dayGridWeek",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.DayGridWeekViewProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarViewProperties",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "dayHeaderFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "dayPopoverFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventTimeFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "weekNumberFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "displayEventEnd", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
            }
    )
    void dayGridWeek();

    @StudioElement(
            name = "DayGridMonth",
            xmlElement = "dayGridMonth",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.DayGridMonthViewProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarViewProperties",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "dayHeaderFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "dayPopoverFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventTimeFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "weekNumberFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "fixedWeekCount", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "showNonCurrentDates", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "displayEventEnd", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
            }
    )
    void dayGridMonth();

    @StudioElement(
            name = "DayGridYear",
            xmlElement = "dayGridYear",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.DayGridYearViewProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarViewProperties",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "dayHeaderFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "dayPopoverFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventTimeFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "weekNumberFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "monthStartFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "displayEventEnd", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
            }
    )
    void dayGridYear();

    @StudioElement(
            name = "ListDay",
            xmlElement = "listDay",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.ListDayViewProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarViewProperties",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "listDayFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDaySideFormat", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "listDaySideVisible", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDayVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),

            }
    )
    void listDay();

    @StudioElement(
            name = "ListWeek",
            xmlElement = "listWeek",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.ListWeekViewProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarViewProperties",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "listDayFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDaySideFormat", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "listDaySideVisible", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDayVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void listWeek();

    @StudioElement(
            name = "ListMonth",
            xmlElement = "listMonth",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.ListMonthViewProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarViewProperties",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "listDayFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDaySideFormat", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "listDaySideVisible", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDayVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void listMonth();

    @StudioElement(
            name = "ListYear",
            xmlElement = "listYear",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.ListYearViewProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarViewProperties",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "listDayFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDaySideFormat", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "listDaySideVisible", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDayVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void listYear();

    @StudioElement(
            name = "TimeGridDay",
            xmlElement = "timeGridDay",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.TimeGridDayViewProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarViewProperties",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "dayPopoverFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "dayHeaderFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "weekNumberFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventTimeFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "slotLabelFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventMinHeight", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "eventShortHeight", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "slotEventOverlap", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "allDaySlot", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "displayEventEnd", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void timeGridDay();

    @StudioElement(
            name = "TimeGridWeek",
            xmlElement = "timeGridWeek",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.TimeGridWeekViewProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarViewProperties",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "dayPopoverFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "dayHeaderFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "weekNumberFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventTimeFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "slotLabelFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventMinHeight", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "eventShortHeight", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "slotEventOverlap", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "allDaySlot", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "displayEventEnd", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void timeGridWeek();

    @StudioElement(
            name = "MultiMonthYear",
            xmlElement = "multiMonthYear",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.MultiMonthYearViewProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarViewProperties",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "multiMonthMaxColumns", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "multiMonthMinWidth", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "multiMonthTitleFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "fixedWeekCount", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "showNonCurrentDates", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void multiMonthYear();

    @StudioElement(
            name = "Duration",
            xmlElement = "duration",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration",
            target = "io.jmix.fullcalendarflowui.kit.component.model.CustomCalendarView",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "years", type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "months", type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "weeks", type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "days", type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "hours", type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "minutes", type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "seconds", type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "milliseconds", type = StudioPropertyType.INTEGER, defaultValue = "0"),
            }
    )
    void calendarDuration();

    @StudioElement(
            name = "Property",
            xmlElement = "property",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.kit.meta.group.StudioFullCalendarProperties",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING, required = true),
            }
    )
    void property();

    @StudioElement(
            name = "Entry",
            xmlElement = "entry",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.component.model.BusinessHours",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "endTime", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "startTime", type = StudioPropertyType.STRING),
            }
    )
    void businessHoursEntry();

    @StudioElement(
            name = "Day",
            xmlElement = "day",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.kit.meta.group.StudioFullCalendarHiddenDays",
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.ENUMERATION, required = true,
                            classFqn = "io.jmix.fullcalendar.DayOfWeek",
                            options = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"}),
            },
            target = {
                    "io.jmix.fullcalendarflowui.component.model.BusinessHours",
            }
    )
    void day();
}
