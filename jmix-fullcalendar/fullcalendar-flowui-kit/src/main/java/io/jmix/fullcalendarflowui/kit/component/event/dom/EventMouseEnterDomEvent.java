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

package io.jmix.fullcalendarflowui.kit.component.event.dom;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;
import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;

/**
 * INTERNAL.
 */
@DomEvent("jmix-event-mouse-enter")
public class EventMouseEnterDomEvent extends ComponentEvent<JmixFullCalendar> {

    protected final JsonObject context;

    public EventMouseEnterDomEvent(JmixFullCalendar source,
                                   boolean fromClient,
                                   @EventData("event.detail.context") JsonObject context) {
        super(source, fromClient);

        this.context = context;
    }

    public JsonObject getContext() {
        return context;
    }
}
