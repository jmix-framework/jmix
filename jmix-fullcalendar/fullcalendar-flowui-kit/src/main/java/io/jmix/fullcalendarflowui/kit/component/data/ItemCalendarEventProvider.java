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

package io.jmix.fullcalendarflowui.kit.component.data;

import com.vaadin.flow.shared.Registration;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.function.Consumer;

public interface ItemCalendarEventProvider extends CalendarEventProvider {

    List<CalendarEvent> getItems();

    @Nullable
    CalendarEvent getItem(Object itemId);

    Registration addItemSetChangeListener(Consumer<ItemSetChangeEvent> listener);

    /**
     * An event that is fired when item set is changed.
     */
    class ItemSetChangeEvent extends EventObject {

        protected final ItemChangeOperation operation;
        protected final Collection<CalendarEvent> items;

        public ItemSetChangeEvent(CalendarEventProvider source, ItemChangeOperation operation,
                                  Collection<CalendarEvent> items) {
            super(source);

            this.operation = operation;
            this.items = items;
        }

        @Override
        public CalendarEventProvider getSource() {
            return (CalendarEventProvider) super.getSource();
        }

        /**
         * @return operation which caused the data provider change
         */
        public ItemChangeOperation getOperation() {
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
