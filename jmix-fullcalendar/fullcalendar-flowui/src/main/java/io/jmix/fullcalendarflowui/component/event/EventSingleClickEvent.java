/*
 * Copyright 2025 Haulmont.
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

package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;

/**
 * The event is fired after some delay when a calendar event is clicked. The delay helps to separate
 * single-click from double-click.
 *
 * @see FullCalendar#setEventSingleClickThreshold(int)
 */
public class EventSingleClickEvent extends AbstractClickEvent {

    protected final CalendarEvent calendarEvent;

    protected final CalendarDataProvider dataProvider;

    protected final DisplayModeInfo displayModeInfo;

    public EventSingleClickEvent(FullCalendar fullCalendar,
                                 boolean fromClient,
                                 MouseEventDetails mouseEventDetails,
                                 CalendarEvent calendarEvent,
                                 CalendarDataProvider dataProvider,
                                 DisplayModeInfo displayModeInfo) {
        super(fullCalendar, fromClient, mouseEventDetails);

        this.calendarEvent = calendarEvent;
        this.dataProvider = dataProvider;
        this.displayModeInfo = displayModeInfo;
    }

    /**
     * @return clicked calendar event
     */
    @SuppressWarnings("unchecked")
    public <T extends CalendarEvent> T getCalendarEvent() {
        return (T) calendarEvent;
    }

    /**
     * @return data provider that contains clicked calendar event
     */
    public CalendarDataProvider getDataProvider() {
        return dataProvider;
    }

    /**
     * @return information about the current calendar's display mode
     */
    public DisplayModeInfo getDisplayModeInfo() {
        return displayModeInfo;
    }
}
