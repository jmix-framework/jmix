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

package io.jmix.flowui.kit.component.menubar;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.MenuItem;

import java.io.Serializable;

/**
 * Interface defining methods for components that can have {@link JmixMenuItem JmixMenuItems} inside them.
 */
public interface HasMenuItemsEnhanced extends Serializable {

    /**
     * Adds a new item component with the given text content to the context menu overlay.
     *
     * @param text the text content for the new item
     * @return the added {@link JmixMenuItem} component
     */
    JmixMenuItem addItem(String text);

    /**
     * Adds a new item component with the given component to the context menu overlay.
     *
     * @param component the component inside the new item
     * @return the added {@link JmixMenuItem} component
     */
    JmixMenuItem addItem(Component component);

    /**
     * Adds a new item component with the given text content and click listener
     * to the context menu overlay.
     *
     * @param text          the text content for the new item
     * @param clickListener the handler for clicking the new item, can be {@code null} to not add listener
     * @return the added {@link JmixMenuItem} component
     */
    JmixMenuItem addItem(String text,
                         ComponentEventListener<ClickEvent<MenuItem>> clickListener);

    /**
     * Adds a new item component with the given component and click listener to
     * the context menu overlay.
     *
     * @param component     the component inside the new item
     * @param clickListener the handler for clicking the new item, can be {@code null} to not add listener
     * @return the added {@link JmixMenuItem} component
     */
    JmixMenuItem addItem(Component component,
                         ComponentEventListener<ClickEvent<MenuItem>> clickListener);

    /**
     * Adds a new item component with the given text content at the given position
     * to the context menu overlay.
     *
     * @param index item position
     * @param text  the text content for the new item
     * @return the added {@link JmixMenuItem} component
     */
    JmixMenuItem addItemAtIndex(int index, String text);

    /**
     * Adds a new item component with the given component at the given position
     * to the context menu overlay.
     *
     * @param index     item position
     * @param component the component inside the new item
     * @return the added {@link JmixMenuItem} component
     */
    JmixMenuItem addItemAtIndex(int index, Component component);

    /**
     * Adds a new item component with the given text content and click listener
     * at the given position to the context menu overlay.
     *
     * @param index         item position
     * @param text          the text content for the new item
     * @param clickListener the handler for clicking the new item, can be {@code null} to not add listener
     * @return the added {@link JmixMenuItem} component
     */
    JmixMenuItem addItemAtIndex(int index, String text,
                                ComponentEventListener<ClickEvent<MenuItem>> clickListener);

    /**
     * Adds a new item component with the given component and click listener
     * at the given position to the context menu overlay.
     *
     * @param index         item position
     * @param component     the component inside the new item
     * @param clickListener the handler for clicking the new item, can be {@code null} to not add listener
     * @return the added {@link JmixMenuItem} component
     */
    JmixMenuItem addItemAtIndex(int index, Component component,
                                ComponentEventListener<ClickEvent<MenuItem>> clickListener);
}
