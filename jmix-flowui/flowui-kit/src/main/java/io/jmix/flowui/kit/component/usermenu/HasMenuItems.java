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

import java.util.List;
import java.util.Optional;

/**
 * Represents a contract for managing menu items in user menu components.
 */
public interface HasMenuItems {

    /**
     * Adds a separator to the menu. A separator is used to visually divide groups
     * of menu items, enabling better organization and readability.
     */
    void addSeparator();

    /**
     * Adds a separator to the menu at the specified index. A separator is used to
     * visually divide groups of menu items, enhancing organization and readability.
     * The given index determines the position where the separator is inserted.
     *
     * @param index the position in the menu where the separator will be added.
     *              The index must be within the valid range of current menu items.
     */
    void addSeparatorAtIndex(int index);

    /**
     * Adds a separator to the menu that links to the parent menu item.
     * The separator will change its visibility depending on the state of the parent item.
     * <p>
     * A separator is used to visually divide groups of menu items, enhancing organization
     * and readability.
     *
     * @param parentItem the parent item
     */
    void addSeparator(UserMenuItem parentItem);

    /**
     * Adds a separator to the menu at the specified index that links to the parent menu item.
     * The separator will change its visibility depending on the state of the parent item.
     * <p>
     * A separator is used to visually divide groups of menu items, enhancing organization
     * and readability.
     *
     * @param index      the position in the menu where the separator will be added.
     *                   The index must be within the valid range of current menu items
     * @param parentItem the parent item
     */
    void addSeparatorAtIndex(int index, UserMenuItem parentItem);

    /**
     * Searches for a user menu item with the specified unique identifier.
     *
     * @param itemId the unique identifier of the menu item to find
     * @return an {@link Optional} containing the {@link UserMenuItem} if found,
     * or an empty {@link Optional} if the item does not exist
     */
    Optional<UserMenuItem> findItem(String itemId);

    /**
     * Retrieves a user menu item with the specified unique identifier.
     *
     * @param itemId the unique identifier of the menu item to retrieve
     * @return the {@link UserMenuItem} associated with the given identifier
     * @throws IllegalArgumentException if the item with the given identifier does not exist
     */
    UserMenuItem getItem(String itemId);

    /**
     * Returns a list of all items currently present in the user menu component.
     *
     * @return a list of all items currently present in the user menu component
     */
    List<UserMenuItem> getItems();

    /**
     * Removes a menu item identified by the specified unique identifier from the menu.
     *
     * @param itemId the unique identifier of the menu item to be removed
     */
    void remove(String itemId);

    /**
     * Removes the specified menu item from the user menu.
     *
     * @param menuItem the {@link UserMenuItem} to be removed from the menu
     */
    void remove(UserMenuItem menuItem);

    /**
     * Removes all menu items from the user menu.
     */
    void removeAll();
}
