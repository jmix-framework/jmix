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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasSubParts;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a UI component providing functionality for managing dropdown items.
 */
public interface DropdownButtonComponent extends HasSubParts {

    /**
     * Adds a new dropdown item with the specified identifier and action.
     *
     * @param id     the identifier for the dropdown item
     * @param action the action to associate with the dropdown item
     * @return the created dropdown item
     */
    DropdownButtonItem addItem(String id, Action action);

    /**
     * Adds a new dropdown item with the specified identifier, action, and index.
     *
     * @param id     the identifier for the dropdown item
     * @param action the action to associate with the dropdown item
     * @param index  the position at which the dropdown item will be added
     * @return the created dropdown item
     */
    DropdownButtonItem addItem(String id, Action action, int index);

    /**
     * Adds a new dropdown item with the specified identifier and text.
     *
     * @param id   the identifier for the dropdown item
     * @param text the text to display for the dropdown item
     * @return the created dropdown item
     */
    DropdownButtonItem addItem(String id, String text);

    /**
     * Adds a new dropdown item with the specified identifier, text, and index.
     *
     * @param id    the identifier for the dropdown item
     * @param text  the text to display for the dropdown item
     * @param index the position at which the dropdown item will be added
     * @return the created dropdown item
     */
    DropdownButtonItem addItem(String id, String text, int index);

    /**
     * Adds a new dropdown item with the specified identifier, text, and a click event listener.
     *
     * @param id                     the unique identifier for the dropdown item
     * @param text                   the text displayed for the dropdown item
     * @param componentEventListener the listener to handle click events for the dropdown item
     * @return the created dropdown item instance
     */
    DropdownButtonItem addItem(String id,
                               String text,
                               Consumer<DropdownButtonItem.ClickEvent> componentEventListener);

    /**
     * Adds a new dropdown item with the specified identifier, text, click event listener, and position index.
     *
     * @param id                     the unique identifier for the dropdown item
     * @param text                   the text to display for the dropdown item
     * @param componentEventListener the listener to handle click events for the dropdown item
     * @param index                  the position at which the dropdown item will be added
     * @return the created dropdown item instance
     */
    DropdownButtonItem addItem(String id,
                               String text,
                               Consumer<DropdownButtonItem.ClickEvent> componentEventListener,
                               int index);

    /**
     * Adds a new dropdown item with the specified identifier and component content.
     *
     * @param id        the unique identifier for the dropdown item
     * @param component the component to be used as content for the dropdown item
     * @return the created dropdown item instance
     */
    DropdownButtonItem addItem(String id, Component component);

    /**
     * Adds a new dropdown item with the specified identifier, component content, and position index.
     *
     * @param id        the unique identifier for the dropdown item
     * @param component the component to be used as content for the dropdown item
     * @param index     the position at which the dropdown item will be added
     * @return the created dropdown item instance
     */
    DropdownButtonItem addItem(String id, Component component, int index);

    /**
     * Adds a new dropdown item with the specified identifier, content component, and a click event listener.
     *
     * @param id                     the unique identifier for the dropdown item
     * @param component              the component to be used as content for the dropdown item
     * @param componentEventListener the listener to handle click events for the dropdown item
     * @return the created dropdown item instance
     */
    DropdownButtonItem addItem(String id,
                               Component component,
                               Consumer<DropdownButtonItem.ClickEvent> componentEventListener);

    /**
     * Adds a new dropdown item with the specified identifier, component content,
     * click event listener, and position index.
     *
     * @param id                     the unique identifier for the dropdown item
     * @param component              the component to be used as content for the dropdown item
     * @param componentEventListener the listener to handle click events for the dropdown item
     * @param index                  the position at which the dropdown item will be added
     * @return the created dropdown item instance
     */
    DropdownButtonItem addItem(String id,
                               Component component,
                               Consumer<DropdownButtonItem.ClickEvent> componentEventListener,
                               int index);

    /**
     * Returns the dropdown item with the specified identifier.
     *
     * @param itemId the unique identifier of the dropdown item to retrieve
     * @return the item with the given identifier, or {@code null} if no item
     * with the specified ID is found
     */
    @Nullable
    DropdownButtonItem getItem(String itemId);

    /**
     * Returns a list of all items currently present in the dropdown button component.
     *
     * @return a list of {@code DropdownButtonItem} instances representing all items in the dropdown.
     */
    List<DropdownButtonItem> getItems();

    /**
     * Removes the specified item identified by the given item ID.
     *
     * @param itemId the unique identifier of the item to be removed
     */
    void remove(String itemId);

    /**
     * Removes the specified item from the collection or list.
     *
     * @param item the item to be removed
     */
    void remove(DropdownButtonItem item);

    /**
     * Removes the specified items from the dropdown button's list of items.
     *
     * @param items the items to be removed from the dropdown button. Each item represents
     *              a DropdownButtonItem object to be excluded from the list.
     */
    void remove(DropdownButtonItem... items);

    /**
     * Removes all items from the dropdown button component.
     */
    void removeAll();

    /**
     * Adds a visual separator to the dropdown menu.
     * Separators are used to group related items visually.
     */
    void addSeparator();

    /**
     * Adds a visual separator at the specified index in the dropdown menu.
     * Separators are used to visually group related items within the menu.
     *
     * @param index the position at which the separator will be added.
     *              The index must be a non-negative integer less than or
     *              equal to the current number of items.
     */
    void addSeparatorAtIndex(int index);

    /**
     * Configures whether the dropdown menu opens when the user hovers over the button.
     *
     * @param openOnHover if {@code true}, the dropdown menu will open on hover;
     *                    if {@code false}, it will only open on click
     */
    void setOpenOnHover(boolean openOnHover);

    /**
     * Determines whether the dropdown menu is configured to open when the user hovers over the component.
     *
     * @return {@code true} if the dropdown opens on hover, {@code false} otherwise
     */
    boolean isOpenOnHover();


    /**
     * Sets the icon for the component.
     *
     * @param icon the icon to set. Can be {@code null} to clear the current icon.
     */
    void setIcon(@Nullable Icon icon);

    /**
     * Returns the icon associated with the dropdown button item, if present.
     *
     * @return the icon of the dropdown button item, or {@code null} if no icon is set
     */
    @Nullable
    Icon getIcon();

    @Nullable
    @Override
    default Object getSubPart(String name) {
        return getItem(name);
    }
}
