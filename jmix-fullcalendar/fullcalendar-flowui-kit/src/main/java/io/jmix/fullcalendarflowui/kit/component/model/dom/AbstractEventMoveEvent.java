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

package io.jmix.fullcalendarflowui.kit.component.model.dom;

import java.io.Serializable;
import java.util.List;

/**
 * INTERNAL.
 */
public class AbstractEventMoveEvent implements Serializable {

    protected DomCalendarEvent event;

    protected DomCalendarEvent oldEvent;

    protected DomMouseEventDetails mouseDetails;

    protected DomViewInfo view;

    protected List<DomCalendarEvent> relatedEvents;

    public DomCalendarEvent getEvent() {
        return event;
    }

    public void setEvent(DomCalendarEvent event) {
        this.event = event;
    }

    public DomCalendarEvent getOldEvent() {
        return oldEvent;
    }

    public void setOldEvent(DomCalendarEvent oldEvent) {
        this.oldEvent = oldEvent;
    }

    public DomMouseEventDetails getMouseDetails() {
        return mouseDetails;
    }

    public void setMouseDetails(DomMouseEventDetails mouseDetails) {
        this.mouseDetails = mouseDetails;
    }

    public DomViewInfo getView() {
        return view;
    }

    public void setView(DomViewInfo view) {
        this.view = view;
    }

    public List<DomCalendarEvent> getRelatedEvents() {
        return relatedEvents;
    }

    public void setRelatedEvents(List<DomCalendarEvent> relatedEvents) {
        this.relatedEvents = relatedEvents;
    }
}
