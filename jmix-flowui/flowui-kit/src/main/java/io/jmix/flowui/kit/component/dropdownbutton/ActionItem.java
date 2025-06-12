/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.kit.component.dropdownbutton;

import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.meta.StudioIgnore;

import java.util.function.Consumer;

/**
 * Represents an item in a dropdown button that is associated with an {@link Action}.
 * This interface provides functionalities for retrieving the related action
 * and delegates click event handling to the encapsulated action logic.
 */
public interface ActionItem extends DropdownButtonItem {

    /**
     * Returns the {@link Action} associated with this item.
     *
     * @return the associated action, or null if no action is set
     */
    Action getAction();

    /**
     * Adds a listener to handle click events for the dropdown button item.
     * This method is not supported for {@code ActionItem} and will throw
     * an {@link UnsupportedOperationException}.
     *
     * @param listener the {@link Consumer} to process {@link ClickEvent} when the item is clicked
     * @return a {@link Registration} object to remove the listener later
     * @throws UnsupportedOperationException if invoked on an {@code ActionItem}
     */
    @StudioIgnore
    @Override
    default Registration addClickListener(Consumer<ClickEvent> listener) {
        throw new UnsupportedOperationException(String.format(
                "Unable to add a ClickListener to actionItem '%s'", getId() != null ? getId() : "null"));
    }
}
