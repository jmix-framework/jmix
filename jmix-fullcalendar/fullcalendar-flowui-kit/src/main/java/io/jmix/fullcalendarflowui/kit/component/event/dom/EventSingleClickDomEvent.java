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

package io.jmix.fullcalendarflowui.kit.component.event.dom;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;
import tools.jackson.databind.node.ObjectNode;

/**
 * INTERNAL.
 */
@DomEvent("jmix-event-single-click")
public class EventSingleClickDomEvent extends AbstractEventClickDomEvent {

    public EventSingleClickDomEvent(JmixFullCalendar source,
                                    boolean fromClient,
                                    @EventData("event.detail.context") ObjectNode context) {
        super(source, fromClient, context);
    }
}
