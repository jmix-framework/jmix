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

import static io.jmix.flowui.kit.meta.StudioMetaConstants.TAG_PREFIX;

@StudioUiKit
public interface StudioFullCalendarElements {

    @StudioElement(
            xmlElement = StudioXmlElements.CONTAINER_DATA_PROVIDER,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.component.data.ContainerCalendarDataProvider",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#container-data-provider",
            isInjectable = false,
            propertyGroups = StudioFullCalendarPropertyGroups.ContainerDataProviderComponent.class)
    void containerDataProvider();

    @StudioElement(
            xmlElement = StudioXmlElements.CALLBACK_DATA_PROVIDER,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.component.data.EntityCalendarDataRetriever",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#callback-calendar-data-provider",
            isInjectable = false,
            propertyGroups = StudioFullCalendarPropertyGroups.CallbackDataProviderComponent.class)
    void callbackDataProvider();

    @StudioElement(
            xmlElement = StudioXmlElements.ITEMS_QUERY,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            icon = "io/jmix/flowui/kit/meta/icon/element/itemsQuery.svg",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#callback-calendar-data-provider",
            target = "io.jmix.fullcalendarflowui.component.data.EntityCalendarDataRetriever",
            propertyGroups = {
                    StudioPropertyGroups.RequiredEntityClass.class,
                    StudioPropertyGroups.Query.class,
                    StudioPropertyGroups.FetchPlan.class
            })
    void entityItemsQuery();

    @StudioElement(
            xmlElement = StudioXmlElements.DISPLAY_MODE,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.CustomCalendarDisplayMode",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#calendar-display-modes",
            isInjectable = false,
            propertyGroups = StudioFullCalendarPropertyGroups.CustomDisplayModeComponent.class,
            unsupportedTarget = TAG_PREFIX + "displayModeProperties"
    )
    void customDisplayMode();

    @StudioElement(
            name = "Display Mode Properties",
            xmlElement = StudioXmlElements.DISPLAY_MODE_PROPERTIES,
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
            xmlElement = StudioXmlElements.DAY_GRID_DAY,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.DayGridDayProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.DayGridDayDefaultProperties.class)
    void dayGridDay();

    @StudioElement(
            xmlElement = StudioXmlElements.DAY_GRID_WEEK,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.DayGridWeekProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.DayGridProperties.class)
    void dayGridWeek();

    @StudioElement(
            xmlElement = StudioXmlElements.DAY_GRID_MONTH,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.DayGridMonthProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = {
                    StudioFullCalendarPropertyGroups.DayGridProperties.class,
                    StudioFullCalendarPropertyGroups.FixedWeekCount.class,
                    StudioFullCalendarPropertyGroups.ShowNonCurrentDates.class
            })
    void dayGridMonth();

    @StudioElement(
            xmlElement = StudioXmlElements.DAY_GRID_YEAR,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.DayGridYearProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.DayGridYearComponent.class)
    void dayGridYear();

    @StudioElement(
            xmlElement = StudioXmlElements.LIST_DAY,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.ListDayProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.ListProperties.class)
    void listDay();

    @StudioElement(
            xmlElement = StudioXmlElements.LIST_WEEK,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.ListWeekProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.ListProperties.class)
    void listWeek();

    @StudioElement(
            xmlElement = StudioXmlElements.LIST_MONTH,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.ListMonthProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.ListProperties.class)
    void listMonth();

    @StudioElement(
            xmlElement = StudioXmlElements.LIST_YEAR,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.ListYearProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.ListProperties.class)
    void listYear();

    @StudioElement(
            xmlElement = StudioXmlElements.TIME_GRID_DAY,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.TimeGridDayProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.TimeGridProperties.class)
    void timeGridDay();

    @StudioElement(
            xmlElement = StudioXmlElements.TIME_GRID_WEEK,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.TimeGridWeekProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.TimeGridProperties.class)
    void timeGridWeek();

    @StudioElement(
            xmlElement = StudioXmlElements.MULTI_MONTH_YEAR,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#_display_mode_properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.MultiMonthYearProperties",
            target = "io.jmix.fullcalendarflowui.kit.meta.element.StudioFullCalendarDisplayModeProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.MultiMonthYearComponent.class)
    void multiMonthYear();

    @StudioElement(
            xmlElement = StudioXmlElements.DURATION,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            unlimitedCount = false,
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#custom-calendar-display-mode",
            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration",
            target = "io.jmix.fullcalendarflowui.kit.component.model.CustomCalendarDisplayMode",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.CalendarDurationComponent.class)
    void calendarDuration();

    @StudioElement(
            xmlElement = StudioXmlElements.PROPERTY,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.meta.group.StudioFullCalendarProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioPropertyGroups.RequiredStringNameAndValue.class)
    void property();

    @StudioElement(
            xmlElement = StudioXmlElements.ENTRY,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#business-hours",
            classFqn = "io.jmix.fullcalendarflowui.component.model.CalendarBusinessHours",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.BusinessHoursEntryComponent.class)
    void businessHoursEntry();

    @StudioElement(
            xmlElement = StudioXmlElements.DAY,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#business-hours",
            classFqn = "io.jmix.fullcalendarflowui.kit.meta.group.StudioFullCalendarHiddenDays",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.DayComponent.class,
            target = {
                    "io.jmix.fullcalendarflowui.component.model.CalendarBusinessHours",
            }
    )
    void day();

    @StudioElement(
            xmlElement = StudioXmlElements.CALENDAR_PARAMETERS,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.facet.urlqueryparameters.FullCalendarUrlQueryParametersBinder",
            target = {"io.jmix.flowui.facet.UrlQueryParametersFacet"},
            documentationLink = "%VERSION%/calendar/url-query-parameters.html",
            propertyGroups = StudioFullCalendarPropertyGroups.CalendarParametersComponent.class)
    void calendarParameters();
}
