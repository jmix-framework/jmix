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
            propertyGroups = StudioFullCalendarPropertyGroups.ContainerDataProviderComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_CONTAINER,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF,
                            required = true,
                            typeParameter = "E"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ADDITIONAL_PROPERTIES, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALL_DAY, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.BACKGROUND_COLOR, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.BORDER_COLOR, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CONSTRAINT, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DISPLAY, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DURATION_EDITABLE, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.END_DATE_TIME, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.GROUP_ID, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.INTERACTIVE, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OVERLAP, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RECURRING_DAYS_OF_WEEK, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RECURRING_END_DATE, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RECURRING_END_TIME, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RECURRING_START_DATE, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RECURRING_START_TIME, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.START_DATE_TIME, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.START_EDITABLE, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT_COLOR, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, type = StudioPropertyType.PROPERTY_REF),
            }
    )
    void containerDataProvider();

    @StudioElement(
            xmlElement = StudioXmlElements.CALLBACK_DATA_PROVIDER,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            classFqn = "io.jmix.fullcalendarflowui.component.data.EntityCalendarDataRetriever",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#callback-calendar-data-provider",
            isInjectable = false,
            propertyGroups = StudioFullCalendarPropertyGroups.CallbackDataProviderComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID,
                            category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ADDITIONAL_PROPERTIES, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALL_DAY, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.BACKGROUND_COLOR, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.BORDER_COLOR, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CONSTRAINT, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DESCRIPTION, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DISPLAY, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DURATION_EDITABLE, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.END_DATE_TIME, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.GROUP_ID, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.INTERACTIVE, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OVERLAP, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RECURRING_DAYS_OF_WEEK, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RECURRING_END_DATE, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RECURRING_END_TIME, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RECURRING_START_DATE, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.RECURRING_START_TIME, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.START_DATE_TIME, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.START_EDITABLE, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TEXT_COLOR, type = StudioPropertyType.PROPERTY_REF),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, type = StudioPropertyType.PROPERTY_REF),
            }
    )
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
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENTITY_CLASS, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.QUERY, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.JPA_QUERY),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FETCH_PLAN, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.FETCH_PLAN)
            }
    )
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
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TYPE, type = StudioPropertyType.STRING,
                            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.GenericCalendarDisplayModes",
                            options = {"DAY_GRID", "TIME_GRID", "LIST", "MULTI_MONTH"},
                            defaultValue = "DAY_GRID"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_COUNT, type = StudioPropertyType.INTEGER)
            },
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
            propertyGroups = StudioFullCalendarPropertyGroups.DayGridDayDefaultProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_HEADER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_POPOVER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EVENT_TIME_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WEEK_NUMBER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DISPLAY_EVENT_END, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
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
            propertyGroups = StudioFullCalendarPropertyGroups.DayGridProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_HEADER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_POPOVER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EVENT_TIME_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WEEK_NUMBER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DISPLAY_EVENT_END, type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
            }
    )
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
            },
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_HEADER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_POPOVER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EVENT_TIME_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WEEK_NUMBER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FIXED_WEEK_COUNT, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHOW_NON_CURRENT_DATES, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DISPLAY_EVENT_END, type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
            }
    )
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
            propertyGroups = StudioFullCalendarPropertyGroups.DayGridYearComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_HEADER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_POPOVER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EVENT_TIME_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WEEK_NUMBER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MONTH_START_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DISPLAY_EVENT_END, type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
            }
    )
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
            propertyGroups = StudioFullCalendarPropertyGroups.ListProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_SIDE_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_SIDE_VISIBLE, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_VISIBLE, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),

            }
    )
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
            propertyGroups = StudioFullCalendarPropertyGroups.ListProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_SIDE_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_SIDE_VISIBLE, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_VISIBLE, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
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
            propertyGroups = StudioFullCalendarPropertyGroups.ListProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_SIDE_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_SIDE_VISIBLE, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_VISIBLE, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
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
            propertyGroups = StudioFullCalendarPropertyGroups.ListProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_SIDE_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_SIDE_VISIBLE, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LIST_DAY_VISIBLE, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
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
            propertyGroups = StudioFullCalendarPropertyGroups.TimeGridProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_POPOVER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_HEADER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WEEK_NUMBER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EVENT_TIME_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SLOT_LABEL_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EVENT_MIN_HEIGHT, type = StudioPropertyType.INTEGER, defaultValue = "15"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EVENT_SHORT_HEIGHT, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SLOT_EVENT_OVERLAP, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALL_DAY_SLOT_VISIBLE, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DISPLAY_EVENT_END, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
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
            propertyGroups = StudioFullCalendarPropertyGroups.TimeGridProperties.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_POPOVER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAY_HEADER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WEEK_NUMBER_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EVENT_TIME_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SLOT_LABEL_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EVENT_MIN_HEIGHT, type = StudioPropertyType.INTEGER, defaultValue = "15"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.EVENT_SHORT_HEIGHT, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SLOT_EVENT_OVERLAP, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALL_DAY_SLOT_VISIBLE, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DISPLAY_EVENT_END, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
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
            propertyGroups = StudioFullCalendarPropertyGroups.MultiMonthYearComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MULTI_MONTH_MAX_COLUMNS, type = StudioPropertyType.INTEGER, defaultValue = "3"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MULTI_MONTH_MIN_WIDTH, type = StudioPropertyType.INTEGER, defaultValue = "350"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MULTI_MONTH_TITLE_FORMAT, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FIXED_WEEK_COUNT, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SHOW_NON_CURRENT_DATES, type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
            }
    )
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
            propertyGroups = StudioFullCalendarPropertyGroups.CalendarDurationComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.YEARS, type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MONTHS, type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WEEKS, type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DAYS, type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HOURS, type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MINUTES, type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SECONDS, type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MILLISECONDS, type = StudioPropertyType.INTEGER, defaultValue = "0"),
            }
    )
    void calendarDuration();

    @StudioElement(
            xmlElement = StudioXmlElements.PROPERTY,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#properties",
            classFqn = "io.jmix.fullcalendarflowui.kit.meta.group.StudioFullCalendarProperties",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioPropertyGroups.RequiredStringNameAndValue.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.NAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING, required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING, required = true),
            }
    )
    void property();

    @StudioElement(
            xmlElement = StudioXmlElements.ENTRY,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#business-hours",
            classFqn = "io.jmix.fullcalendarflowui.component.model.CalendarBusinessHours",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.BusinessHoursEntryComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.END_TIME, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.START_TIME, type = StudioPropertyType.STRING),
            }
    )
    void businessHoursEntry();

    @StudioElement(
            xmlElement = StudioXmlElements.DAY,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html#business-hours",
            classFqn = "io.jmix.fullcalendarflowui.kit.meta.group.StudioFullCalendarHiddenDays",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.DayComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.NAME, type = StudioPropertyType.ENUMERATION, required = true,
                            classFqn = "io.jmix.fullcalendar.DayOfWeek",
                            options = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"}),
            },
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
            propertyGroups = StudioFullCalendarPropertyGroups.CalendarParametersComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COMPONENT, category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.COMPONENT_REF, componentRefTags = "calendar", required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CALENDAR_DISPLAY_MODE, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CALENDAR_DATE, type = StudioPropertyType.STRING),
            }
    )
    void calendarParameters();
}
