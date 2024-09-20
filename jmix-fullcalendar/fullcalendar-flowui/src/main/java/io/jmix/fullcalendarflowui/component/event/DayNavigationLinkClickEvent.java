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

package io.jmix.fullcalendarflowui.component.event;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.fullcalendarflowui.component.FullCalendar;

import java.time.LocalDate;

/**
 * The event is fired when the user clicks on day heading navigation link. The navigation link can be activated
 * by the {@link FullCalendar#setNavigationLinksEnabled(boolean)} property.
 */
public class DayNavigationLinkClickEvent extends ComponentEvent<FullCalendar> {

    protected final LocalDate date;

    public DayNavigationLinkClickEvent(FullCalendar source, boolean fromClient, LocalDate date) {
        super(source, fromClient);

        this.date = date;
    }

    /**
     * @return the date of the clicked day
     */
    public LocalDate getDate() {
        return date;
    }
}
