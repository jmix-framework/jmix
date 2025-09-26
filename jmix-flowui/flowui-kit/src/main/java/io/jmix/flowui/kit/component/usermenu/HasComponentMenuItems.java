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
import io.jmix.flowui.kit.component.usermenu.UserMenuItem.HasClickListener;

import java.util.function.Consumer;

/**
 * Defines a set of methods for {@link JmixUserMenu} components where menu items
 * can contain UI {@link Component} elements.
 */
public interface HasComponentMenuItems extends HasMenuItems {

    /**
     * Adds a new menu item with a unique identifier and a custom UI {@link Component} as its content.
     *
     * @param id      the unique identifier of the menu item
     * @param content the {@link Component} to be set as the content of the menu item
     * @return the created {@link ComponentUserMenuItem} instance
     */
    ComponentUserMenuItem addComponentItem(String id, Component content);

    /**
     * Adds a new menu item with a unique identifier, a custom UI {@link Component} as its content,
     * and inserts it at a specific position in the menu.
     *
     * @param id      the unique identifier of the menu item
     * @param content the {@link Component} to be set as the content of the menu item
     * @param index   the position at which the menu item will be inserted
     * @return the created {@link ComponentUserMenuItem} instance
     */
    ComponentUserMenuItem addComponentItem(String id, Component content, int index);

    /**
     * Adds a new menu item with a unique identifier, a custom UI {@link Component} as its content,
     * and a click listener to handle click events for the menu item.
     *
     * @param id       the unique identifier of the menu item
     * @param content  the {@link Component} to be set as the content of the menu item
     * @param listener the {@link Consumer} that handles the click events triggered by the menu item
     * @return the created {@link ComponentUserMenuItem} instance
     */
    ComponentUserMenuItem addComponentItem(String id, Component content,
                                           Consumer<HasClickListener.ClickEvent<ComponentUserMenuItem>> listener);

    /**
     * Adds a new menu item with a unique identifier, a custom UI {@link Component} as its content,
     * a click listener to handle click events for the menu item, and inserts it at a specific position
     * in the menu.
     *
     * @param id       the unique identifier of the menu item
     * @param content  the {@link Component} to be set as the content of the menu item
     * @param listener the {@link Consumer} that handles the click events triggered by the menu item
     * @param index    the position at which the menu item will be inserted
     * @return the created {@link ComponentUserMenuItem} instance
     */
    ComponentUserMenuItem addComponentItem(String id, Component content,
                                           Consumer<HasClickListener.ClickEvent<ComponentUserMenuItem>> listener,
                                           int index);
}
