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

import com.vaadin.flow.component.Component;

import java.util.function.Consumer;

/**
 * Defines a set of methods for {@link JmixUserMenu} components where menu items
 * can contain text and icon.
 */
public interface HasTextMenuItems extends HasMenuItems {

    /**
     * Adds a new menu item with the specified ID and text.
     *
     * @param id   unique identifier for the menu item
     * @param text text to be displayed in the menu item
     * @return newly created menu item
     */
    TextUserMenuItem addItem(String id, String text);

    /**
     * Adds a new menu item with the specified ID and text at the given index.
     *
     * @param id    unique identifier for the menu item
     * @param text  text to be displayed in the menu item
     * @param index position at which to insert the menu item
     * @return newly created menu item
     */
    TextUserMenuItem addItem(String id, String text, int index);

    /**
     * Adds a new menu item with the specified ID, text and click listener.
     *
     * @param id       unique identifier for the menu item
     * @param text     text to be displayed in the menu item
     * @param listener callback to be invoked when the menu item is clicked
     * @return newly created menu item
     */
    TextUserMenuItem addItem(String id, String text,
                             Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener);

    /**
     * Adds a new menu item with the specified ID, text and click listener at the given index.
     *
     * @param id       unique identifier for the menu item
     * @param text     text to be displayed in the menu item
     * @param listener callback to be invoked when the menu item is clicked
     * @param index    position at which to insert the menu item
     * @return newly created menu item
     */
    TextUserMenuItem addItem(String id, String text,
                             Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener,
                             int index);

    /**
     * Adds a new menu item with the specified ID, text and icon.
     *
     * @param id   unique identifier for the menu item
     * @param text text to be displayed in the menu item
     * @param icon icon component to be displayed in the menu item
     * @return newly created menu item
     */
    TextUserMenuItem addItem(String id, String text, Component icon);

    /**
     * Adds a new menu item with the specified ID, text and icon at the given index.
     *
     * @param id    unique identifier for the menu item
     * @param text  text to be displayed in the menu item
     * @param icon  icon component to be displayed in the menu item
     * @param index position at which to insert the menu item
     * @return newly created menu item
     */
    TextUserMenuItem addItem(String id, String text, Component icon, int index);

    /**
     * Adds a new menu item with the specified ID, text, icon and click listener.
     *
     * @param id       unique identifier for the menu item
     * @param text     text to be displayed in the menu item
     * @param icon     icon component to be displayed in the menu item
     * @param listener callback to be invoked when the menu item is clicked
     * @return newly created menu item
     */
    TextUserMenuItem addItem(String id,
                             String text, Component icon,
                             Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener);

    /**
     * Adds a new menu item with the specified ID, text, icon and click listener at the given index.
     *
     * @param id       unique identifier for the menu item
     * @param text     text to be displayed in the menu item
     * @param icon     icon component to be displayed in the menu item
     * @param listener callback to be invoked when the menu item is clicked
     * @param index    position at which to insert the menu item
     * @return newly created menu item
     */
    TextUserMenuItem addItem(String id,
                             String text, Component icon,
                             Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener,
                             int index);
}
