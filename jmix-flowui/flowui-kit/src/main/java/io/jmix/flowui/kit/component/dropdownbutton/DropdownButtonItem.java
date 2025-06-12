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

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Represents an item that can be included in a dropdown button component.
 * Defines the common behavior for items such as visibility, enabling/disabling,
 * and handling click events.
 */
public interface DropdownButtonItem {

    /**
     * Returns the parent dropdown button component to which this item belongs.
     *
     * @return the parent {@code DropdownButtonComponent} of this item
     */
    DropdownButtonComponent getParent();

    /**
     * Returns the unique identifier of the dropdown button item.
     *
     * @return the identifier of the item as a string, or null if not set
     */
    String getId();

    /**
     * Sets the visibility of the dropdown button item.
     * A visible item is rendered and can interact with users,
     * while an invisible item is not displayed and cannot be interacted with.
     *
     * @param visible if true, the item will be visible; otherwise, it will be hidden
     */
    void setVisible(boolean visible);

    /**
     * Checks whether the dropdown button item is currently visible.
     * A visible item is displayed in the dropdown and can interact with users,
     * whereas an invisible item is hidden and cannot be interacted with.
     *
     * @return true if the item is visible, false otherwise
     */
    boolean isVisible();

    /**
     * Enables or disables the dropdown button item.
     * When the item is disabled, it cannot be interacted with.
     *
     * @param enabled if {@code true}, the item will be enabled; otherwise, it will be disabled
     */
    void setEnabled(boolean enabled);

    /**
     * Checks whether the dropdown button item is currently enabled.
     * An enabled item can be interacted with, whereas a disabled item cannot.
     *
     * @return {@code true} if the item is enabled, {@code false} otherwise
     */
    boolean isEnabled();

    /**
     * Adds a listener to handle click events for the dropdown button item.
     * When the item is clicked, the provided {@link Consumer} processes the associated {@link ClickEvent}.
     *
     * @param listener the {@link Consumer} to handle the {@link ClickEvent} triggered by a click on the item
     * @return a {@link Registration} object that can be used to remove the added listener
     */
    Registration addClickListener(Consumer<ClickEvent> listener);

    /**
     * Represents a click event triggered by a {@link DropdownButtonItem}.
     * This event encapsulates the source item, providing access to
     * the dropdown button item associated with the click event.
     *
     * @see DropdownButtonItem
     */
    class ClickEvent extends EventObject {

        public ClickEvent(DropdownButtonItem item) {
            super(item);
        }

        @Override
        public DropdownButtonItem getSource() {
            return (DropdownButtonItem) super.getSource();
        }
    }
}
