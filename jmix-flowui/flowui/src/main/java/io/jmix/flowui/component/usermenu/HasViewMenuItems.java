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

package io.jmix.flowui.component.usermenu;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.component.usermenu.HasMenuItems;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenu;
import io.jmix.flowui.view.View;

/**
 * Defines a set of methods for {@link JmixUserMenu} components where menu items
 * associated with a {@link View}.
 */
public interface HasViewMenuItems extends HasMenuItems {

    /**
     * Adds a menu item associated with a specified view class and text.
     *
     * @param id        the unique identifier of the menu item
     * @param viewClass the class of the view to associate with the menu item
     * @param text      the text to be set for the menu item
     * @return the created {@link ViewUserMenuItem} instance
     */
    ViewUserMenuItem addViewItem(String id, Class<? extends View<?>> viewClass, String text);

    /**
     * Adds a menu item associated with a specified view class and text at a specific position.
     *
     * @param id        the unique identifier of the menu item
     * @param viewClass the class of the view to associate with the menu item
     * @param text      the text to be set for the menu item
     * @param index     the position at which the menu item will be added
     * @return the created {@link ViewUserMenuItem} instance
     */
    ViewUserMenuItem addViewItem(String id, Class<? extends View<?>> viewClass, String text, int index);

    /**
     * Adds a menu item associated with a specified view class, text and icon.
     *
     * @param id        the unique identifier of the menu item
     * @param viewClass the class of the view to associate with the menu item
     * @param text      the text to be set for the menu item
     * @param icon      the icon to be set for the menu item
     * @return the created {@link ViewUserMenuItem} instance
     */
    ViewUserMenuItem addViewItem(String id, Class<? extends View<?>> viewClass, String text, Component icon);

    /**
     * Adds a menu item associated with a specified view class, text and icon at a specific position.
     *
     * @param id        the unique identifier of the menu item
     * @param viewClass the class of the view to associate with the menu item
     * @param text      the text to be set for the menu item
     * @param icon      the icon to be set for the menu item
     * @param index     the position at which the menu item will be added
     * @return the created {@link ViewUserMenuItem} instance
     */
    ViewUserMenuItem addViewItem(String id, Class<? extends View<?>> viewClass, String text, Component icon, int index);

    /**
     * Adds a menu item associated with a specified view id and text.
     *
     * @param id     the unique identifier of the menu item
     * @param viewId the id of the view to associate with the menu item
     * @param text   the text to be set for the menu item
     * @return the created {@link ViewUserMenuItem} instance
     */
    ViewUserMenuItem addViewItem(String id, String viewId, String text);

    /**
     * Adds a menu item associated with a specified view id and text at a specific position.
     *
     * @param id     the unique identifier of the menu item
     * @param viewId the id of the view to associate with the menu item
     * @param text   the text to be set for the menu item
     * @param index  the position at which the menu item will be added
     * @return the created {@link ViewUserMenuItem} instance
     */
    ViewUserMenuItem addViewItem(String id, String viewId, String text, int index);

    /**
     * Adds a menu item associated with a specified view id, text and icon.
     *
     * @param id     the unique identifier of the menu item
     * @param viewId the id of the view to associate with the menu item
     * @param text   the text to be set for the menu item
     * @param icon   the icon to be set for the menu item
     * @return the created {@link ViewUserMenuItem} instance
     */
    ViewUserMenuItem addViewItem(String id, String viewId, String text, Component icon);

    /**
     * Adds a menu item associated with a specified view id, text and icon at a specific position.
     *
     * @param id     the unique identifier of the menu item
     * @param viewId the id of the view to associate with the menu item
     * @param text   the text to be set for the menu item
     * @param icon   the icon to be set for the menu item
     * @param index  the position at which the menu item will be added
     * @return the created {@link ViewUserMenuItem} instance
     */
    ViewUserMenuItem addViewItem(String id, String viewId, String text, Component icon, int index);
}
