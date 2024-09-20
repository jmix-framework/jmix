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
import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;

/**
 * INTERNAL.
 */
@DomEvent("jmix-day-link-click")
public class DayNavigationLinkClickDomEvent extends ComponentEvent<JmixFullCalendar> {

    protected final String date;

    public DayNavigationLinkClickDomEvent(JmixFullCalendar source, boolean fromClient,
                                          @EventData("event.detail.context.date") String date) {
        super(source, fromClient);

        this.date = date;
    }

    public String getDate() {
        return date;
    }
}
