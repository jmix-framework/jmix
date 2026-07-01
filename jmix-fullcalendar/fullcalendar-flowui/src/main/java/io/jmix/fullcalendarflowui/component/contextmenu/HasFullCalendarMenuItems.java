/*
 * Copyright 2026 Haulmont.
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

package io.jmix.fullcalendarflowui.component.contextmenu;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import org.jspecify.annotations.NullMarked;

import java.io.Serializable;

/**
 * Interface to be implemented by context menu that works with {@link FullCalendar}.
 */
@NullMarked
public interface HasFullCalendarMenuItems extends Serializable {

    /**
     * Adds new menu item.
     *
     * @param text          item's text
     * @param clickListener click listener
     * @return added menu item
     */
    FullCalendarMenuItem addItem(String text,
                                 ComponentEventListener<FullCalendarClickContextMenuItemEvent> clickListener);

    /**
     * Adds new menu item.
     *
     * @param component     component that should be used as a content of menu item
     * @param clickListener click listener
     * @return added menu item
     */
    FullCalendarMenuItem addItem(Component component,
                                 ComponentEventListener<FullCalendarClickContextMenuItemEvent> clickListener);

    /**
     * Event is fired when menu item in context menu is clicked.
     */
    class FullCalendarClickContextMenuItemEvent extends ComponentEvent<FullCalendarMenuItem> {

        public FullCalendarClickContextMenuItemEvent(FullCalendarMenuItem source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
