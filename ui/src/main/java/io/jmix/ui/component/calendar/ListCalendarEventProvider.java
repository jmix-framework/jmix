/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.calendar;

import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListCalendarEventProvider implements CalendarEventProvider {

    protected List<CalendarEvent> eventList = new ArrayList<>();

    protected Consumer<CalendarEvent.EventChangeEvent> eventChangeListener = eventChangeEvent ->
            fireEventSetChange();

    protected EventHub events = new EventHub();

    protected void fireEventSetChange() {
        events.publish(EventSetChangeEvent.class, new EventSetChangeEvent(this));
    }

    @Override
    public List<CalendarEvent> getEvents() {
        return eventList;
    }

    @Override
    public void addEvent(CalendarEvent calendarEvent) {
        calendarEvent.addEventChangeListener(eventChangeListener);
        eventList.add(calendarEvent);
        fireEventSetChange();
    }

    @Override
    public void removeEvent(CalendarEvent calendarEvent) {
        calendarEvent.removeEventChangeListener(eventChangeListener);
        eventList.remove(calendarEvent);
        fireEventSetChange();
    }

    @Override
    public void removeAllEvents() {
        for (CalendarEvent calendarEvent : eventList) {
            calendarEvent.removeEventChangeListener(eventChangeListener);
        }

        eventList.clear();
        fireEventSetChange();
    }

    @Override
    public Subscription addEventSetChangeListener(Consumer<EventSetChangeEvent> listener) {
        return events.subscribe(EventSetChangeEvent.class, listener);
    }

    @Override
    public void removeEventSetChangeListener(Consumer<EventSetChangeEvent> listener) {
        events.unsubscribe(EventSetChangeEvent.class, listener);
    }
}
