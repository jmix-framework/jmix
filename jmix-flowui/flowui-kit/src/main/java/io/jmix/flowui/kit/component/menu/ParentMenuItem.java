/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.kit.component.menu;

import java.util.List;

/**
 * Represents an item of a menu (for example, {@link io.jmix.flowui.kit.component.main.ListMenu})
 * which can contain nested child items
 * @param <T> child menu item type
 */
public interface ParentMenuItem<T extends MenuItem> extends MenuItem {

    /**
     * @return child items of this parent item
     */
    List<T> getChildren();

    /**
     * Removes all child items of this parent item
     */
    void removeAllChildItems();

    /**
     * Adds child menu item.
     *
     * @param item menu item to add
     */
    void addChildItem(T item);

    /**
     * Adds child menu item at index.
     *
     * @param item menu item to add
     * @param index index in children collection to add at
     */
    void addChildItem(T item, int index);

    /**
     * Removes child menu item.
     *
     * @param item menu item to remove
     */
    void removeChildItem(T item);

    /**
     * Sets the opened status of the item.
     *
     * @param opened true/false to open/close the item
     */
    void setOpened(boolean opened);

    /**
     * @return true/false if the item is opened/closed
     */
    boolean isOpened();
}
