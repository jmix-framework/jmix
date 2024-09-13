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

package io.jmix.fullcalendarflowui.component.data;

import com.vaadin.flow.shared.Registration;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interface to be implemented by data providers that should load a list of events.
 */
public interface ItemsCalendarDataProvider extends CalendarDataProvider {

    /**
     * @return loaded calendar events
     */
    List<CalendarEvent> getItems();

    /**
     * @param itemId ID of calendar event
     * @return calendar event or {@code null} if no event with provided ID
     */
    @Nullable
    CalendarEvent getItem(Object itemId);

    /**
     * Adds an item set change listener.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener added to a data provider
     */
    Registration addItemSetChangeListener(Consumer<ItemSetChangeEvent> listener);

    /**
     * An event that is fired when item set is changed.
     */
    class ItemSetChangeEvent extends EventObject {

        protected final DataChangeOperation operation;
        protected final Collection<CalendarEvent> items;

        public ItemSetChangeEvent(CalendarDataProvider source,
                                  DataChangeOperation operation,
                                  Collection<CalendarEvent> items) {
            super(source);

            this.operation = operation;
            this.items = items;
        }

        @Override
        public CalendarDataProvider getSource() {
            return (CalendarDataProvider) super.getSource();
        }

        /**
         * @return operation which caused the data provider change
         */
        public DataChangeOperation getOperation() {
            return operation;
        }

        /**
         * @return items which used in operation
         */
        public Collection<CalendarEvent> getItems() {
            return items;
        }
    }
}
