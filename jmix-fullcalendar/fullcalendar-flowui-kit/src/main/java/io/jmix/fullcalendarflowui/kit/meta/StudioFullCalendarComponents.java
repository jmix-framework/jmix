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
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;

import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlElements;
@StudioUiKit(studioClassloaderDependencies = "io.jmix.fullcalendar:jmix-fullcalendar-flowui-kit")
public interface StudioFullCalendarComponents {

    @StudioComponent(
            name = "FullCalendar",
            xmlElement = StudioXmlElements.CALENDAR,
            xmlnsAlias = "calendar",
            xmlns = "http://jmix.io/schema/fullcalendar/ui",
            category = "Components",
            classFqn = "io.jmix.fullcalendarflowui.component.FullCalendar",
            documentationLink = "%VERSION%/calendar/full-calendar-component.html",
            icon = "io/jmix/fullcalendarflowui/kit/meta/icon/component/calendar.svg",
            propertyGroups = StudioFullCalendarPropertyGroups.FullCalendarComponent.class)
    JmixFullCalendar fullCalendar();
}
