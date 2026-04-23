/*
 * Copyright 2026 Haulmont.
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
import io.jmix.flowui.kit.meta.StudioPropertyGroups.BaseActionDefaultProperties;
import io.jmix.flowui.kit.meta.StudioPropertyGroups.IconWithSetParameter;

@StudioAPI
final class StudioFullCalendarPropertyGroups {

    private StudioFullCalendarPropertyGroups() {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ADDITIONAL_PROPERTIES,
            type = StudioPropertyType.VALUES_LIST))
    public interface AdditionalProperties {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALL_DAY,
            type = StudioPropertyType.PROPERTY_REF))
    public interface AllDay {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ALL_DAY_SLOT_VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface AllDaySlotVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BACKGROUND_COLOR,
            type = StudioPropertyType.PROPERTY_REF))
    public interface BackgroundColor {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.BORDER_COLOR,
            type = StudioPropertyType.PROPERTY_REF))
    public interface BorderColor {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CLASS_NAMES,
            type = StudioPropertyType.PROPERTY_REF))
    public interface DataProviderClassNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.CONSTRAINT,
            type = StudioPropertyType.PROPERTY_REF))
    public interface Constraint {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DAY_HEADER_FORMAT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface DayHeaderFormat {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DAY_POPOVER_FORMAT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface DayPopoverFormat {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DESCRIPTION,
            type = StudioPropertyType.PROPERTY_REF))
    public interface Description {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DISPLAY_EVENT_END,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface DisplayEventEnd {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DISPLAY_EVENT_END,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "false"))
    public interface DisplayEventEndWithFalseDefaultValue {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DISPLAY,
            type = StudioPropertyType.PROPERTY_REF))
    public interface Display {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.DURATION_EDITABLE,
            type = StudioPropertyType.PROPERTY_REF))
    public interface DurationEditable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.END_DATE_TIME,
            type = StudioPropertyType.PROPERTY_REF))
    public interface EndDateTime {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.EVENT_MIN_HEIGHT,
            type = StudioPropertyType.INTEGER,
            defaultValue = "15"))
    public interface EventMinHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.EVENT_SHORT_HEIGHT,
            type = StudioPropertyType.INTEGER))
    public interface EventShortHeight {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.EVENT_TIME_FORMAT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface EventTimeFormat {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.FIXED_WEEK_COUNT,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface FixedWeekCount {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.GROUP_ID,
            type = StudioPropertyType.PROPERTY_REF))
    public interface GroupId {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.INTERACTIVE,
            type = StudioPropertyType.PROPERTY_REF))
    public interface Interactive {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LIST_DAY_FORMAT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface ListDayFormat {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LIST_DAY_SIDE_FORMAT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface ListDaySideFormat {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LIST_DAY_SIDE_VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface ListDaySideVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.LIST_DAY_VISIBLE,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface ListDayVisible {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.OVERLAP,
            type = StudioPropertyType.PROPERTY_REF))
    public interface Overlap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RECURRING_DAYS_OF_WEEK,
            type = StudioPropertyType.PROPERTY_REF))
    public interface RecurringDaysOfWeek {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RECURRING_END_DATE,
            type = StudioPropertyType.PROPERTY_REF))
    public interface RecurringEndDate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RECURRING_END_TIME,
            type = StudioPropertyType.PROPERTY_REF))
    public interface RecurringEndTime {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RECURRING_START_DATE,
            type = StudioPropertyType.PROPERTY_REF))
    public interface RecurringStartDate {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.RECURRING_START_TIME,
            type = StudioPropertyType.PROPERTY_REF))
    public interface RecurringStartTime {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SHOW_NON_CURRENT_DATES,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface ShowNonCurrentDates {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SLOT_EVENT_OVERLAP,
            type = StudioPropertyType.BOOLEAN,
            defaultValue = "true"))
    public interface SlotEventOverlap {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SLOT_LABEL_FORMAT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface SlotLabelFormat {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.START_DATE_TIME,
            type = StudioPropertyType.PROPERTY_REF))
    public interface StartDateTime {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.START_EDITABLE,
            type = StudioPropertyType.PROPERTY_REF))
    public interface StartEditable {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TEXT_COLOR,
            type = StudioPropertyType.PROPERTY_REF))
    public interface TextColor {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.TITLE,
            type = StudioPropertyType.PROPERTY_REF))
    public interface Title {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.WEEK_NUMBER_FORMAT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface WeekNumberFormat {
    }

    @StudioPropertyGroup
    public interface DataProviderProperties extends StudioPropertyGroups.Id, AdditionalProperties, AllDay,
            BackgroundColor, BorderColor, DataProviderClassNames, Constraint, Description, Display,
            DurationEditable, EndDateTime, GroupId, Interactive, Overlap, RecurringDaysOfWeek, RecurringEndDate,
            RecurringEndTime, RecurringStartDate, RecurringStartTime, StartDateTime, StartEditable, TextColor,
            Title {
    }

    @StudioPropertyGroup
    public interface ListProperties extends ListDayFormat, ListDaySideFormat, ListDaySideVisible, ListDayVisible {
    }

    @StudioPropertyGroup
    public interface TimeGridProperties extends DayPopoverFormat, DayHeaderFormat, WeekNumberFormat,
            EventTimeFormat, SlotLabelFormat, EventMinHeight, EventShortHeight, SlotEventOverlap,
            AllDaySlotVisible, DisplayEventEnd {
    }

    @StudioPropertyGroup
    public interface DayGridProperties extends DayHeaderFormat, DayPopoverFormat, EventTimeFormat,
            WeekNumberFormat, DisplayEventEndWithFalseDefaultValue {
    }

    @StudioPropertyGroup
    public interface DayGridDayDefaultProperties extends DayHeaderFormat, DayPopoverFormat, EventTimeFormat,
            WeekNumberFormat, DisplayEventEnd {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            initialValue = "daysOfWeekEdit"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "msg://io.jmix.datatoolsflowui.action/showEntityInfoAction.title")
            }
    )
    public interface DaysOfWeekEditActionComponent extends BaseActionDefaultProperties, IconWithSetParameter {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATA_CONTAINER,
                            type = StudioPropertyType.COLLECTION_OR_INSTANCE_DATA_CONTAINER_REF,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            typeParameter = "E")
            }
    )
    public interface ContainerDataProviderComponent extends Title, AllDay, Overlap, GroupId, Display, TextColor,
            Constraint, Interactive, EndDateTime, Description, BorderColor, StartEditable, StartDateTime,
            BackgroundColor, RecurringEndTime, RecurringEndDate, DurationEditable, RecurringStartTime,
            RecurringStartDate, RecurringDaysOfWeek, AdditionalProperties, DataProviderClassNames {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.ID,
            type = StudioPropertyType.STRING,
            category = StudioProperty.Category.GENERAL))
    public interface CallbackDataProviderComponent extends Title, AllDay, Overlap, GroupId, Display, TextColor,
            Constraint, Interactive, EndDateTime, Description, BorderColor, StartEditable, StartDateTime,
            BackgroundColor, RecurringEndTime, RecurringEndDate, DurationEditable, RecurringStartTime,
            RecurringStartDate, RecurringDaysOfWeek, AdditionalProperties, DataProviderClassNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ID,
                            type = StudioPropertyType.STRING,
                            required = true),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TYPE,
                            type = StudioPropertyType.STRING,
                            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.GenericCalendarDisplayModes",
                            defaultValue = "DAY_GRID",
                            options = {"DAY_GRID", "TIME_GRID", "LIST", "MULTI_MONTH"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DAY_COUNT,
                            type = StudioPropertyType.INTEGER)
            }
    )
    public interface CustomDisplayModeComponent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.MONTH_START_FORMAT,
            type = StudioPropertyType.LOCALIZED_STRING))
    public interface DayGridYearComponent extends DayGridProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MULTI_MONTH_MAX_COLUMNS,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "3"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MULTI_MONTH_MIN_WIDTH,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "350"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MULTI_MONTH_TITLE_FORMAT,
                            type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface MultiMonthYearComponent extends FixedWeekCount, ShowNonCurrentDates {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.YEARS,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MONTHS,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.WEEKS,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DAYS,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.HOURS,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MINUTES,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SECONDS,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MILLISECONDS,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "0")
            }
    )
    public interface CalendarDurationComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.END_TIME,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.START_TIME,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface BusinessHoursEntryComponent {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.NAME,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.fullcalendar.DayOfWeek",
            required = true,
            options = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"}))
    public interface DayComponent {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.COMPONENT,
                            type = StudioPropertyType.COMPONENT_REF,
                            category = StudioProperty.Category.GENERAL,
                            required = true,
                            componentRefTags = "calendar"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CALENDAR_DISPLAY_MODE,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CALENDAR_DATE,
                            type = StudioPropertyType.STRING)
            }
    )
    public interface CalendarParametersComponent extends StudioPropertyGroups.Id {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ALL_DAY_MAINTAIN_DURATION_ENABLED,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ALL_DAY_TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.CLOSE_HINT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DAY_HEADERS_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DAY_MAX_EVENTS,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DAY_MAX_EVENT_ROWS,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DAY_OF_YEAR,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DATE_ALIGNMENT,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT_ALL_DAY,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT_ALL_DAY_EVENT_DURATION,
                            type = StudioPropertyType.STRING,
                            defaultValue = "P1d"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT_DAY_HEADER_FORMAT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT_DAY_MAX_EVENT_ROWS_ENABLED,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT_DAY_MAX_EVENTS_ENABLED,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT_DAY_POPOVER_FORMAT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT_BUSINESS_HOURS_ENABLED,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT_EVENT_TIME_FORMAT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT_SLOT_LABEL_FORMAT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT_TIMED_EVENT_DURATION,
                            type = StudioPropertyType.STRING,
                            defaultValue = "PT1h"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DEFAULT_WEEK_NUMBER_FORMAT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DIRECTION,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.fullcalendarflowui.component.FullCalendarI18n$Direction",
                            options = {"LTR", "RTL"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DISPLAY_EVENT_TIME,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DRAG_REVERT_DURATION,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "500"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.DRAG_SCROLL,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_BACKGROUND_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE",
                                    "BLACK", "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE",
                                    "CHARTREUSE", "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN",
                                    "DARKBLUE", "DARKCYAN", "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN",
                                    "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN", "DARKORANGE", "DARKORCHID", "DARKRED",
                                    "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE", "DARKSLATEGRAY", "DARKSLATEGREY",
                                    "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE", "DIMGRAY", "DIMGREY",
                                    "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA", "GAINSBORO",
                                    "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW",
                                    "HONEYDEW", "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER",
                                    "LAVENDERBLUSH", "LAWNGREEN", "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL",
                                    "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY", "LIGHTGREY", "LIGHTGREEN",
                                    "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE", "LIGHTSLATEGRAY",
                                    "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID",
                                    "MEDIUMPURPLE", "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN",
                                    "MEDIUMTURQUOISE", "MEDIUMVIOLETRED", "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE",
                                    "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE", "OLIVEDRAB", "ORANGE",
                                    "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE",
                                    "PURPLE", "RED", "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN",
                                    "SEAGREEN", "SEASHELL", "SIENNA", "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY",
                                    "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE", "TAN", "TEAL", "THISTLE", "TOMATO",
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_BORDER_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE",
                                    "BLACK", "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE",
                                    "CHARTREUSE", "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN",
                                    "DARKBLUE", "DARKCYAN", "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN",
                                    "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN", "DARKORANGE", "DARKORCHID", "DARKRED",
                                    "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE", "DARKSLATEGRAY", "DARKSLATEGREY",
                                    "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE", "DIMGRAY", "DIMGREY",
                                    "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA", "GAINSBORO",
                                    "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW",
                                    "HONEYDEW", "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER",
                                    "LAVENDERBLUSH", "LAWNGREEN", "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL",
                                    "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY", "LIGHTGREY", "LIGHTGREEN",
                                    "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE", "LIGHTSLATEGRAY",
                                    "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID",
                                    "MEDIUMPURPLE", "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN",
                                    "MEDIUMTURQUOISE", "MEDIUMVIOLETRED", "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE",
                                    "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE", "OLIVEDRAB", "ORANGE",
                                    "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE",
                                    "PURPLE", "RED", "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN",
                                    "SEAGREEN", "SEASHELL", "SIENNA", "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY",
                                    "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE", "TAN", "TEAL", "THISTLE", "TOMATO",
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_CONSTRAINT_GROUP_ID,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_CONSTRAINT_BUSINESS_HOURS_ENABLED,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_DISPLAY,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.fullcalendar.Display",
                            defaultValue = "AUTO",
                            options = {"AUTO", "BLOCK", "LIST_ITEM", "BACKGROUND", "INVERSE_BACKGROUND", "NONE"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_DRAG_MIN_DISTANCE,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "5"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_DURATION_EDITABLE,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_HINT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_INTERACTIVE,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_LONG_PRESS_DELAY,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "1000"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_MAX_STACK,
                            type = StudioPropertyType.INTEGER),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_ORDER,
                            type = StudioPropertyType.VALUES_LIST,
                            defaultValue = "start,-duration,allDay,title"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_ORDER_STRICT,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_OVERLAP,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_RESIZABLE_FROM_START,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_SINGLE_CLICK_THRESHOLD,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "250"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_START_EDITABLE,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EVENT_TEXT_COLOR,
                            type = StudioPropertyType.OPTIONS,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"ALICEBLUE", "ANTIQUEWHITE", "AQUA", "AQUAMARINE", "AZURE", "BEIGE", "BISQUE",
                                    "BLACK", "BLANCHEDALMOND", "BLUE", "BLUEVIOLET", "BROWN", "BURLYWOOD", "CADETBLUE",
                                    "CHARTREUSE", "CHOCOLATE", "CORAL", "CORNFLOWERBLUE", "CORNSILK", "CRIMSON", "CYAN",
                                    "DARKBLUE", "DARKCYAN", "DARKGOLDENROD", "DARKGRAY", "DARKGREY", "DARKGREEN",
                                    "DARKKHAKI", "DARKMAGENTA", "DARKOLIVEGREEN", "DARKORANGE", "DARKORCHID", "DARKRED",
                                    "DARKSALMON", "DARKSEAGREEN", "DARKSLATEBLUE", "DARKSLATEGRAY", "DARKSLATEGREY",
                                    "DARKTURQUOISE", "DARKVIOLET", "DEEPPINK", "DEEPSKYBLUE", "DIMGRAY", "DIMGREY",
                                    "DODGERBLUE", "FIREBRICK", "FLORALWHITE", "FORESTGREEN", "FUCHSIA", "GAINSBORO",
                                    "GHOSTWHITE", "GOLD", "GOLDENROD", "GRAY", "GREY", "GREEN", "GREENYELLOW",
                                    "HONEYDEW", "HOTPINK", "INDIANRED", "INDIGO", "IVORY", "KHAKI", "LAVENDER",
                                    "LAVENDERBLUSH", "LAWNGREEN", "LEMONCHIFFON", "LIGHTBLUE", "LIGHTCORAL",
                                    "LIGHTCYAN", "LIGHTGOLDENRODYELLOW", "LIGHTGRAY", "LIGHTGREY", "LIGHTGREEN",
                                    "LIGHTPINK", "LIGHTSALMON", "LIGHTSEAGREEN", "LIGHTSKYBLUE", "LIGHTSLATEGRAY",
                                    "LIGHTSLATEGREY", "LIGHTSTEELBLUE", "LIGHTYELLOW", "LIME", "LIMEGREEN", "LINEN",
                                    "MAGENTA", "MAROON", "MEDIUMAQUAMARINE", "MEDIUMBLUE", "MEDIUMORCHID",
                                    "MEDIUMPURPLE", "MEDIUMSEAGREEN", "MEDIUMSLATEBLUE", "MEDIUMSPRINGGREEN",
                                    "MEDIUMTURQUOISE", "MEDIUMVIOLETRED", "MIDNIGHTBLUE", "MINTCREAM", "MISTYROSE",
                                    "MOCCASIN", "NAVAJOWHITE", "NAVY", "OLDLACE", "OLIVE", "OLIVEDRAB", "ORANGE",
                                    "ORANGERED", "ORCHID", "PALEGOLDENROD", "PALEGREEN", "PALETURQUOISE",
                                    "PALEVIOLETRED", "PAPAYAWHIP", "PEACHPUFF", "PERU", "PINK", "PLUM", "POWDERBLUE",
                                    "PURPLE", "RED", "ROSYBROWN", "ROYALBLUE", "SADDLEBROWN", "SALMON", "SANDYBROWN",
                                    "SEAGREEN", "SEASHELL", "SIENNA", "SILVER", "SKYBLUE", "SLATEBLUE", "SLATEGRAY",
                                    "SLATEGREY", "SNOW", "SPRINGGREEN", "STEELBLUE", "TAN", "TEAL", "THISTLE", "TOMATO",
                                    "TURQUOISE", "VIOLET", "WHEAT", "WHITE", "WHITESMOKE", "YELLOW", "YELLOWGREEN"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.EXPAND_ROWS,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FIRST_DAY_OF_WEEK,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.fullcalendar.DayOfWeek",
                            options = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.FORCE_EVENT_DURATION,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.INITIAL_DATE,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.INITIAL_DISPLAY_MODE,
                            type = StudioPropertyType.STRING,
                            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayModes",
                            defaultValue = "DAY_GRID_MONTH",
                            options = {"DAY_GRID", "DAY_GRID_DAY", "DAY_GRID_WEEK", "DAY_GRID_MONTH", "DAY_GRID_YEAR",
                                    "TIME_GRID", "TIME_GRID_DAY", "TIME_GRID_WEEK", "LIST", "LIST_DAY", "LIST_WEEK",
                                    "LIST_MONTH", "LIST_YEAR", "MULTI_MONTH", "MULTI_MONTH_YEAR"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.LONG_PRESS_DELAY,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "1000"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MORE_LINK_CLASS_NAMES,
                            type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MORE_LINK_DISPLAY_MODE,
                            type = StudioPropertyType.STRING,
                            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayModes",
                            options = {"DAY_GRID_DAY", "DAY_GRID_WEEK", "DAY_GRID_MONTH", "DAY_GRID_YEAR",
                                    "TIME_GRID_DAY", "TIME_GRID_WEEK", "LIST_DAY", "LIST_WEEK", "LIST_MONTH",
                                    "LIST_YEAR", "MULTI_MONTH_YEAR"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MORE_LINK_HINT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.MORE_LINK_TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.NAVIGATION_LINKS_ENABLED,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.NAV_LINK_HINT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.NEXT_DAY_THRESHOLD,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.NO_EVENTS_TEXT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.NOW_INDICATOR_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.PROGRESSIVE_EVENT_RENDERING,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SCROLL_TIME,
                            type = StudioPropertyType.STRING,
                            defaultValue = "PT6h"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SCROLL_TIME_RESET,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SELECT_CONSTRAINT_GROUP_ID,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SELECT_CONSTRAINT_BUSINESS_HOURS_ENABLED,
                            type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SELECTION_ENABLED,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SELECT_LONG_PRESS_DELAY,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "1000"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SELECT_MIN_DISTANCE,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SELECT_MIRROR,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SELECT_OVERLAP,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SLOT_DURATION,
                            type = StudioPropertyType.STRING,
                            defaultValue = "PT30m"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SLOT_LABEL_INTERVAL,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SLOT_MAX_TIME,
                            type = StudioPropertyType.STRING,
                            defaultValue = "PT24h"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SLOT_MIN_TIME,
                            type = StudioPropertyType.STRING,
                            defaultValue = "PT0h"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SNAP_DURATION,
                            type = StudioPropertyType.STRING,
                            defaultValue = "PT30m"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.TIME_HINT,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.UNSELECT_AUTO,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.UNSELECT_CANCEL_SELECTOR,
                            type = StudioPropertyType.STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.WEEKENDS_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.WEEK_TEXT_LONG,
                            type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.WINDOW_RESIZE_DELAY,
                            type = StudioPropertyType.INTEGER,
                            defaultValue = "100")
            }
    )
    public interface FullCalendarComponent extends StudioPropertyGroups.AddonComponentDefaultProperties,
            StudioPropertyGroups.WeekNumbersVisible {
    }

}
