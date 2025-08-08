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

package io.jmix.flowui.kit.component.usermenu;

import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.HasSubParts;

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Represents an item that can be included in a user menu component.
 * Defines the common behavior for items such as visibility, enabling/disabling,
 * and handling click events.
 */
public interface UserMenuItem extends HasThemeVariant<UserMenuItemVariant>, HasSubParts {

    /**
     * Returns the unique identifier of the user menu item.
     *
     * @return the unique identifier of the menu item
     */
    String getId();

    /**
     * Sets the visibility of the user menu item.
     * A visible item is rendered and can interact with users,
     * while an invisible item is not displayed and cannot be interacted with.
     *
     * @param visible if true, the item will be visible; otherwise, it will be hidden
     */
    void setVisible(boolean visible);

    /**
     * Checks whether the user menu item is currently visible.
     * A visible item is displayed in the dropdown and can interact with users,
     * whereas an invisible item is hidden and cannot be interacted with.
     *
     * @return true if the item is visible, false otherwise
     */
    boolean isVisible();

    /**
     * Enables or disables the user menu item.
     * When the item is disabled, it cannot be interacted with.
     *
     * @param enabled if {@code true}, the item will be enabled; otherwise, it will be disabled
     */
    void setEnabled(boolean enabled);

    /**
     * Checks whether the user menu item is currently enabled.
     * An enabled item can be interacted with, whereas a disabled item cannot.
     *
     * @return {@code true} if the item is enabled, {@code false} otherwise
     */
    boolean isEnabled();

    /**
     * Returns whether this item toggles a checkmark icon when clicked.
     *
     * @return the checkable state of the item
     * @see #setCheckable(boolean)
     */
    boolean isCheckable();

    /**
     * Sets the checkable state of this menu item. A checkable item toggles a
     * checkmark icon when clicked. Changes in the checked state can be handled
     * in the item's click handler with {@link #isChecked()}.
     * <p>
     * Setting a checked item un-checkable also makes it un-checked.
     *
     * @param checkable {@code true} to enable toggling the checked-state of this menu
     *                  item by clicking, {@code false} to disable it.
     * @throws IllegalStateException if setting a parent item checkable
     */
    void setCheckable(boolean checkable);

    /**
     * Returns the checked state of this item. The item can be checked and
     * un-checked with {@link #setChecked(boolean)} or by clicking it when it is
     * checkable. A checked item displays a checkmark icon inside it.
     *
     * @return {@code true} if the item is checked, {@code false} otherwise
     * @see #setCheckable(boolean)
     * @see #setChecked(boolean)
     */
    boolean isChecked();

    /**
     * Sets the checked state of this item. A checked item displays a checkmark
     * icon next to it. The checked state is also toggled by clicking the item.
     * <p>
     * Note that the item needs to be explicitly set as checkable via
     * {@link #setCheckable(boolean)} in order to check it.
     *
     * @param checked {@code true} to check this item, {@code false} to un-check it
     * @throws IllegalStateException if trying to check the item when it's checkable
     */
    void setChecked(boolean checked);

    /**
     * Retrieves the submenu associated with this user menu item.
     * A submenu is a container for additional menu items, providing
     * hierarchical structuring of user menu options.
     *
     * @return the submenu associated with this user menu item
     */
    SubMenu getSubMenu();

    /**
     * Represents a submenu within a user menu.
     * <p>
     * A submenu serves as a container for additional menu items, enabling hierarchical
     * structuring of user menu options.
     */
    interface SubMenu extends HasTextMenuItems, HasActionMenuItems, HasComponentMenuItems {

    }

    /**
     * This interface provides functionality for adding click listeners to user menu items.
     * Implementing classes allow handling of click events using the {@link ClickEvent}.
     *
     * @param <ITEM> the type of {@link UserMenuItem} this interface applies to
     */
    interface HasClickListener<ITEM extends UserMenuItem> {

        /**
         * Adds a listener to handle click events for the user menu item.
         * When the item is clicked, the provided {@link Consumer} processes the associated {@link ClickEvent}.
         *
         * @param listener the {@link Consumer} to handle the {@link ClickEvent} triggered by a click on the item
         * @return a {@link Registration} object that can be used to remove the added listener
         */
        Registration addClickListener(Consumer<ClickEvent<ITEM>> listener);

        /**
         * Represents a click event triggered by a {@link UserMenuItem}.
         * This event encapsulates the source item, providing access to
         * the user menu item associated with the click event.
         *
         * @see UserMenuItem
         */
        class ClickEvent<ITEM extends UserMenuItem> extends EventObject {

            public ClickEvent(ITEM item) {
                super(item);
            }

            @SuppressWarnings("unchecked")
            @Override
            public ITEM getSource() {
                return (ITEM) super.getSource();
            }
        }
    }
}
