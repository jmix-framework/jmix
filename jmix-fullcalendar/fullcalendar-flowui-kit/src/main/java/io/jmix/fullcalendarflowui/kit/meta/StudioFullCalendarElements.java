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

@StudioUiKit
public interface StudioFullCalendarElements {

    @StudioElement(
            xmlElement = "containerDataProvider",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.component.data.ContainerCalendarDataProvider",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#container-data-provider",
            isInjectable = false,
            properties = {
                    @StudioProperty(xmlAttribute = "id",
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "dataContainer",
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF,
                            required = true,
                            typeParameter = "E"),
                    @StudioProperty(xmlAttribute = "additionalProperties", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "allDay", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "backgroundColor", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "borderColor", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "constraint", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "display", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "durationEditable", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "endDateTime", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "groupId", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "interactive", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "overlap", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "recurringDaysOfWeek", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "recurringEndDate", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "recurringEndTime", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "recurringStartDate", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "recurringStartTime", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "startDateTime", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "startEditable", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "textColor", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.PROPERTY_REF),
            }
    )
    void containerDataProvider();

    @StudioElement(
            xmlElement = "callbackDataProvider",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.component.data.EntityCalendarDataRetriever",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#callback-calendar-data-provider",
            isInjectable = false,
            properties = {
                    @StudioProperty(xmlAttribute = "id",
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "additionalProperties", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "allDay", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "backgroundColor", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "borderColor", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "constraint", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "display", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "durationEditable", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "endDateTime", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "groupId", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "interactive", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "overlap", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "recurringDaysOfWeek", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "recurringEndDate", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "recurringEndTime", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "recurringStartDate", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "recurringStartTime", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "startDateTime", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "startEditable", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "textColor", type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.PROPERTY_REF),
            }
    )
    void callbackDataProvider();

    @StudioElement(
            xmlElement = "itemsQuery",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            icon = "io/jmix/flowui/kit/meta/icon/element/itemsQuery.svg",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#callback-calendar-data-provider",
            target = "io.jmix.fullcalendarflowui.component.data.EntityCalendarDataRetriever",
            properties = {
                    @StudioProperty(xmlAttribute = "class", type = StudioPropertyType.ENTITY_CLASS, required = true),
                    @StudioProperty(xmlAttribute = "query", type = StudioPropertyType.JPA_QUERY),
                    @StudioProperty(xmlAttribute = "fetchPlan", type = StudioPropertyType.FETCH_PLAN)
            }
    )
    void entityItemsQuery();

    @StudioElement(
            xmlElement = "displayMode",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.CustomCalendarDisplayMode",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#calendar-display-modes",
            isInjectable = false,
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "type", type = StudioPropertyType.STRING,
                            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.GenericCalendarDisplayModes",
                            options = {"DAY_GRID", "TIME_GRID", "LIST", "MULTI_MONTH"},
                            defaultValue = "DAY_GRID"),
                    @StudioProperty(xmlAttribute = "dayCount", type = StudioPropertyType.INTEGER)
            },
            unsupportedTarget = "tag:displayModeProperties"
    )
    void customDisplayMode();

    @StudioElement(
            name = "Display Mode Properties",
            xmlElement = "displayModeProperties",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            target = "io.jmix.fullcalendarflowui.component.FullCalendar",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg"
    )
    void displayModeProperties();

    @StudioElement(
            xmlElement = "dayGridDay",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.DayGridDayProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
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
            xmlElement = "dayGridWeek",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.DayGridWeekProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
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
            xmlElement = "dayGridMonth",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.DayGridMonthProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
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
            xmlElement = "dayGridYear",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.DayGridYearProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
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
            xmlElement = "listDay",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.ListDayProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "listDayFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDaySideFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDaySideVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "listDayVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),

            }
    )
    void listDay();

    @StudioElement(
            xmlElement = "listWeek",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.ListWeekProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "listDayFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDaySideFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDaySideVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "listDayVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void listWeek();

    @StudioElement(
            xmlElement = "listMonth",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.ListMonthProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "listDayFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDaySideFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDaySideVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "listDayVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void listMonth();

    @StudioElement(
            xmlElement = "listYear",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.ListYearProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "listDayFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDaySideFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "listDaySideVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "listDayVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void listYear();

    @StudioElement(
            xmlElement = "timeGridDay",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.TimeGridDayProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "dayPopoverFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "dayHeaderFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "weekNumberFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventTimeFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "slotLabelFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventMinHeight", type = StudioPropertyType.INTEGER, defaultValue = "15"),
                    @StudioProperty(xmlAttribute = "eventShortHeight", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "slotEventOverlap", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "allDaySlotVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "displayEventEnd", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void timeGridDay();

    @StudioElement(
            xmlElement = "timeGridWeek",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.TimeGridWeekProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "dayPopoverFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "dayHeaderFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "weekNumberFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventTimeFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "slotLabelFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventMinHeight", type = StudioPropertyType.INTEGER, defaultValue = "15"),
                    @StudioProperty(xmlAttribute = "eventShortHeight", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "slotEventOverlap", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "allDaySlotVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "displayEventEnd", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void timeGridWeek();

    @StudioElement(
            xmlElement = "multiMonthYear",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.MultiMonthYearProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "multiMonthMaxColumns", type = StudioPropertyType.INTEGER, defaultValue = "3"),
                    @StudioProperty(xmlAttribute = "multiMonthMinWidth", type = StudioPropertyType.INTEGER, defaultValue = "350"),
                    @StudioProperty(xmlAttribute = "multiMonthTitleFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "fixedWeekCount", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "showNonCurrentDates", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
    void multiMonthYear();

    @StudioElement(
            xmlElement = "duration",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#custom-calendar-display-mode",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration",
            target = "io.jmix.fullcalendarflowui.kit.component.model.CustomCalendarDisplayMode",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
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
            xmlElement = "property",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.meta.group.StudioFullCalendarProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING, required = true),
            }
    )
    void property();

    @StudioElement(
            xmlElement = "entry",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#business-hours",
            classFqn = "io.jmix.fullcalendarflowui.component.model.CalendarBusinessHours",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "endTime", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "startTime", type = StudioPropertyType.STRING),
            }
    )
    void businessHoursEntry();

    @StudioElement(
            xmlElement = "day",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#business-hours",
            classFqn = "io.jmix.fullcalendarflowui.kit.meta.group.StudioFullCalendarHiddenDays",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "name", type = StudioPropertyType.ENUMERATION, required = true,
                            classFqn = "io.jmix.fullcalendar.DayOfWeek",
                            options = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"}),
            },
            target = {
                    "io.jmix.fullcalendarflowui.component.model.CalendarBusinessHours",
            }
    )
    void day();

    @StudioElement(
            xmlElement = "calendarParameters",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.facet.urlqueryparameters.FullCalendarUrlQueryParametersBinder",
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            documentationLink = "%VERSION%/calendar/url-query-parameters.html",
            properties = {
                    @StudioProperty(xmlAttribute = "component", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_REF, componentRefTags = "calendar", required = true),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "calendarDisplayMode", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "calendarDate", type = StudioPropertyType.STRING),
            }
    )
    void calendarParameters();
}
