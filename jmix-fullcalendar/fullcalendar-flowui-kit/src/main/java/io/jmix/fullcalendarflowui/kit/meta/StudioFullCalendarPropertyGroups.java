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

@StudioAPI
public final class StudioFullCalendarPropertyGroups {

    private StudioFullCalendarPropertyGroups() {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "additionalProperties", type = StudioPropertyType.VALUES_LIST)
            }
    )
    public interface AdditionalProperties {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "allDay", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface AllDay {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "allDaySlotVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    public interface AllDaySlotVisible {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "backgroundColor", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface BackgroundColor {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "borderColor", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface BorderColor {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface DataProviderClassNames {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "constraint", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface Constraint {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "dayHeaderFormat", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface DayHeaderFormat {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "dayPopoverFormat", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface DayPopoverFormat {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface Description {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "displayEventEnd", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    public interface DisplayEventEnd {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "displayEventEnd", type = StudioPropertyType.BOOLEAN, defaultValue = "false")
            }
    )
    public interface DisplayEventEndWithFalseDefaultValue {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "display", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface Display {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "durationEditable", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface DurationEditable {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "endDateTime", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface EndDateTime {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "eventMinHeight", type = StudioPropertyType.INTEGER, defaultValue = "15")
            }
    )
    public interface EventMinHeight {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "eventShortHeight", type = StudioPropertyType.INTEGER)
            }
    )
    public interface EventShortHeight {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "eventTimeFormat", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface EventTimeFormat {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "fixedWeekCount", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    public interface FixedWeekCount {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "groupId", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface GroupId {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "interactive", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface Interactive {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "listDayFormat", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface ListDayFormat {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "listDaySideFormat", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface ListDaySideFormat {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "listDaySideVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    public interface ListDaySideVisible {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "listDayVisible", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    public interface ListDayVisible {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "overlap", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface Overlap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "recurringDaysOfWeek", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface RecurringDaysOfWeek {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "recurringEndDate", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface RecurringEndDate {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "recurringEndTime", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface RecurringEndTime {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "recurringStartDate", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface RecurringStartDate {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "recurringStartTime", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface RecurringStartTime {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "showNonCurrentDates", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    public interface ShowNonCurrentDates {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "slotEventOverlap", type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    public interface SlotEventOverlap {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "slotLabelFormat", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
    public interface SlotLabelFormat {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "startDateTime", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface StartDateTime {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "startEditable", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface StartEditable {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "textColor", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface TextColor {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.PROPERTY_REF)
            }
    )
    public interface Title {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(xmlAttribute = "weekNumberFormat", type = StudioPropertyType.LOCALIZED_STRING)
            }
    )
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
}
