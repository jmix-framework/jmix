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

import io.jmix.flowui.kit.action.Action;

/**
 * Defines a set of methods for {@link JmixUserMenu} components where menu items
 * associated with an {@link Action}.
 */
public interface HasActionMenuItems extends HasMenuItems {

    /**
     * Adds a new menu item with the specified identifier and associated action.
     *
     * @param id     the unique identifier of the menu item
     * @param action the {@link Action} to associate with the menu item
     * @return the created {@link ActionUserMenuItem} instance
     */
    ActionUserMenuItem addActionItem(String id, Action action);

    /**
     * Adds a new menu item with the specified identifier, associated action,
     * and specified index where the item should be inserted.
     *
     * @param id     the unique identifier of the menu item
     * @param action the {@link Action} to associate with the menu item
     * @param index  the position at which the menu item should be inserted
     * @return the created {@link ActionUserMenuItem} instance
     */
    ActionUserMenuItem addActionItem(String id, Action action, int index);
}
