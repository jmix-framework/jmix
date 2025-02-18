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

import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;

@StudioUiKit(studioClassloaderDependencies = "io.jmix.fullcalendar:jmix-fullcalendar-flowui-kit")
public interface StudioFullCalendarComponents {

    @StudioComponent(
            name = "FullCalendar",
            xmlElement = "calendar",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            category = "Components",
            classFqn = "io.jmix.fullcalendarflowui.component.FullCalendar",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/component/calendar.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "css", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),

                    @StudioProperty(xmlAttribute = "allDayMaintainDurationEnabled", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "allDayText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "closeHint", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "dayHeadersVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "dayMaxEvents", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dayMaxEventRows", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dayOfYear", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dateAlignment", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "defaultAllDay", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "defaultAllDayEventDuration", type = StudioPropertyType.STRING, defaultValue = "P1d"),
                    @StudioProperty(xmlAttribute = "defaultDayHeaderFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "defaultDayMaxEventRowsEnabled", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "defaultDayMaxEventsEnabled", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "defaultDayPopoverFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "defaultBusinessHoursEnabled", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "defaultEventTimeFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "defaultSlotLabelFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "defaultTimedEventDuration", type = StudioPropertyType.STRING, defaultValue = "PT1h"),
                    @StudioProperty(xmlAttribute = "defaultWeekNumberFormat", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "direction",
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.fullcalendarflowui.component.FullCalendarI18n$Direction",
                            options = {"LTR", "RTL"}),
                    @StudioProperty(xmlAttribute = "displayEventTime", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "dragRevertDuration", type = StudioPropertyType.INTEGER, defaultValue = "500"),
                    @StudioProperty(xmlAttribute = "dragScroll", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "eventBackgroundColor",
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            type = StudioPropertyType.OPTIONS,
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
                    @StudioProperty(xmlAttribute = "eventBorderColor",
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            type = StudioPropertyType.OPTIONS,
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
                    @StudioProperty(xmlAttribute = "eventConstraintGroupId", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "eventConstraintBusinessHoursEnabled", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "eventDisplay", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.fullcalendar.Display",
                            options = {"AUTO", "BLOCK", "LIST_ITEM", "BACKGROUND", "INVERSE_BACKGROUND", "NONE"},
                            defaultValue = "AUTO"),
                    @StudioProperty(xmlAttribute = "eventDragMinDistance", type = StudioPropertyType.INTEGER, defaultValue = "5"),
                    @StudioProperty(xmlAttribute = "eventDurationEditable", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "eventHint", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "eventInteractive", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "eventLongPressDelay", type = StudioPropertyType.INTEGER, defaultValue = "1000"),
                    @StudioProperty(xmlAttribute = "eventMaxStack", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "eventOrder", type = StudioPropertyType.VALUES_LIST, defaultValue = "start,-duration,allDay,title"),
                    @StudioProperty(xmlAttribute = "eventOrderStrict", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "eventOverlap", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "eventResizableFromStart", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "eventStartEditable", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "eventTextColor",
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            type = StudioPropertyType.OPTIONS,
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
                    @StudioProperty(xmlAttribute = "expandRows", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "firstDayOfWeek", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.fullcalendar.DayOfWeek",
                            options = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"}),
                    @StudioProperty(xmlAttribute = "forceEventDuration", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "initialDate", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "initialDisplayMode", type = StudioPropertyType.STRING,
                            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayModes",
                            options = {"DAY_GRID", "DAY_GRID_DAY", "DAY_GRID_WEEK", "DAY_GRID_MONTH", "DAY_GRID_YEAR",
                                    "TIME_GRID", "TIME_GRID_DAY", "TIME_GRID_WEEK",
                                    "LIST", "LIST_DAY", "LIST_WEEK", "LIST_MONTH", "LIST_YEAR",
                                    "MULTI_MONTH", "MULTI_MONTH_YEAR"},
                            defaultValue = "DAY_GRID_MONTH"),
                    @StudioProperty(xmlAttribute = "longPressDelay", type = StudioPropertyType.INTEGER, defaultValue = "1000"),
                    @StudioProperty(xmlAttribute = "moreLinkClassNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "moreLinkDisplayMode", type = StudioPropertyType.STRING,
                            classFqn = "io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayModes",
                            options = {"DAY_GRID_DAY", "DAY_GRID_WEEK", "DAY_GRID_MONTH", "DAY_GRID_YEAR",
                                    "TIME_GRID_DAY", "TIME_GRID_WEEK",
                                    "LIST_DAY", "LIST_WEEK", "LIST_MONTH", "LIST_YEAR", "MULTI_MONTH_YEAR"}),
                    @StudioProperty(xmlAttribute = "moreLinkHint", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "moreLinkText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "navigationLinksEnabled", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "navLinkHint", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "nextDayThreshold", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "noEventsText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "nowIndicatorVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "progressiveEventRendering", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "scrollTime", type = StudioPropertyType.STRING, defaultValue = "PT6h"),
                    @StudioProperty(xmlAttribute = "scrollTimeReset", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "selectConstraintGroupId", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "selectConstraintBusinessHoursEnabled", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "selectionEnabled", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "selectLongPressDelay", type = StudioPropertyType.INTEGER, defaultValue = "1000"),
                    @StudioProperty(xmlAttribute = "selectMinDistance", type = StudioPropertyType.INTEGER, defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "selectMirror", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "selectOverlap", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "slotDuration", type = StudioPropertyType.STRING, defaultValue = "PT30m"),
                    @StudioProperty(xmlAttribute = "slotLabelInterval", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "slotMaxTime", type = StudioPropertyType.STRING, defaultValue = "PT24h"),
                    @StudioProperty(xmlAttribute = "slotMinTime", type = StudioPropertyType.STRING, defaultValue = "PT0h"),
                    @StudioProperty(xmlAttribute = "snapDuration", type = StudioPropertyType.STRING, defaultValue = "PT30m"),
                    @StudioProperty(xmlAttribute = "timeHint", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "unselectAuto", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "unselectCancelSelector", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "weekendsVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "weekNumbersVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "weekTextLong", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "windowResizeDelay", type = StudioPropertyType.INTEGER, defaultValue = "100"),
            }
    )
    JmixFullCalendar fullCalendar();
}
