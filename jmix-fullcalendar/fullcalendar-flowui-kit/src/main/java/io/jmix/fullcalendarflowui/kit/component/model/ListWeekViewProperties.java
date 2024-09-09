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

package io.jmix.fullcalendarflowui.kit.component.model;

import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;

/**
 * Configuration properties of list week view {@link CalendarViewType#LIST_WEEK}.
 * <p>
 * The view properties can be retrieved from {@link JmixFullCalendar#getCalendarViewProperties(CalendarViewType)}.
 * For instance:
 * <pre>{@code
 * calendar.getCalendarViewProperties(CalendarViewType.LIST_WEEK);
 * }</pre>
 */
public class ListWeekViewProperties extends AbstractListViewProperties {

    public ListWeekViewProperties() {
        super(CalendarViewType.LIST_WEEK.getId());
    }
}
