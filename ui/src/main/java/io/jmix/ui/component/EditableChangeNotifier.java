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

package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Component that fires {@link EditableChangeEvent} events.
 */
public interface EditableChangeNotifier {

    /**
     * Adds a listener that is invoked when the {@code editable} property changes.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addEditableChangeListener(Consumer<EditableChangeEvent> listener);

    /**
     * Event sent when the {@code editable} property is changed.
     */
    class EditableChangeEvent extends EventObject {
        public EditableChangeEvent(Component.Editable source) {
            super(source);
        }

        @Override
        public Component.Editable getSource() {
            return (Component.Editable) super.getSource();
        }
    }
}