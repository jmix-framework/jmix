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

import io.jmix.flowui.kit.meta.StudioElementsGroup;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit(studioClassloaderDependencies = "io.jmix.fullcalendar:jmix-fullcalendar-flowui-kit")
public interface StudioFullCalendarElementsGroups {

    @StudioElementsGroup(
            name = "Event providers",
            xmlElement = "eventProviders",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            elementClassFqn = "io.jmix.fullcalendarflowui.component.data.BaseCalendarEventProvider",
            target = "io.jmix.fullcalendarflowui.component.FullCalendar",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg"
    )
    void eventProviders();

    @StudioElementsGroup(
            name = "Custom views",
            xmlElement = "customViews",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            elementClassFqn = "io.jmix.fullcalendarflowui.kit.component.model.CustomCalendarView",
            target = "io.jmix.fullcalendarflowui.component.FullCalendar",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg"
    )
    void customViews();

    @StudioElementsGroup(
            name = "Properties",
            xmlElement = "properties",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            elementClassFqn = "io.jmix.fullcalendarflowui.kit.meta.group.StudioFullCalendarProperties",
            target = {
                    "io.jmix.fullcalendarflowui.kit.component.model.CustomCalendarView",
                    "io.jmix.fullcalendarflowui.kit.component.model.DayGridDayViewProperties",
                    "io.jmix.fullcalendarflowui.kit.component.model.DayGridWeekViewProperties",
                    "io.jmix.fullcalendarflowui.kit.component.model.DayGridMonthViewProperties",
                    "io.jmix.fullcalendarflowui.kit.component.model.DayGridYearViewProperties",
                    "io.jmix.fullcalendarflowui.kit.component.model.ListDayViewProperties",
                    "io.jmix.fullcalendarflowui.kit.component.model.ListWeekViewProperties",
                    "io.jmix.fullcalendarflowui.kit.component.model.ListMonthViewProperties",
                    "io.jmix.fullcalendarflowui.kit.component.model.ListYearViewProperties",
                    "io.jmix.fullcalendarflowui.kit.component.model.TimeGridDayViewProperties",
                    "io.jmix.fullcalendarflowui.kit.component.model.TimeGridWeekViewProperties",
                    "io.jmix.fullcalendarflowui.kit.component.model.MultiMonthYearViewProperties",
            },
            icon = "io/jmix/mapsflowui/kit/meta/icon/unknownComponent.svg"
    )
    void properties();

    @StudioElementsGroup(
            name = "Business hours",
            xmlElement = "businessHours",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            elementClassFqn = "io.jmix.fullcalendarflowui.component.model.BusinessHours",
            target = "io.jmix.fullcalendarflowui.component.FullCalendar",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg"
    )
    void businessHours();

    @StudioElementsGroup(
            name = "Hidden days",
            xmlElement = "hiddenDays",
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            elementClassFqn = "io.jmix.fullcalendarflowui.kit.meta.group.StudioFullCalendarHiddenDays",
            target = "io.jmix.fullcalendarflowui.component.FullCalendar",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/unknownComponent.svg"
    )
    void hiddenDays();
}
