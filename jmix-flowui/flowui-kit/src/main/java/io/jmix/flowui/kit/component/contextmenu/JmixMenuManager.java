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

package io.jmix.flowui.kit.component.contextmenu;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.contextmenu.MenuItemBase;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenuBase;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableRunnable;

import jakarta.annotation.Nullable;

/**
 * Common management logic for context menus and sub menus. Maintains the list
 * of components to stamp into one overlay.
 *
 * @param <C> the context menu type
 * @param <I> the menu item type
 * @param <S> the sub menu type
 */
public class JmixMenuManager<C extends Component, I extends MenuItemBase<?, I, S>, S extends SubMenuBase<?, I, S>>
        extends MenuManager<C, I, S> {

    private final C menu;
    private final SerializableBiFunction<C, SerializableRunnable, I> itemGenerator;
    private final SerializableRunnable contentReset;

    public JmixMenuManager(C menu, SerializableRunnable contentReset,
                           SerializableBiFunction<C, SerializableRunnable, I> itemGenerator,
                           Class<I> itemType, @Nullable I parentMenuItem) {
        super(menu, contentReset, itemGenerator, itemType, parentMenuItem);

        this.menu = menu;
        this.contentReset = contentReset;
        this.itemGenerator = itemGenerator;
    }

    /**
     * Adds a text as a menu item at the given position.
     *
     * @param index item position
     * @param text  the text for the menu item
     * @return a new menu item
     */
    public I addItemAtIndex(int index, String text) {
        I menuItem = itemGenerator.apply(menu, contentReset);
        menuItem.setText(text);
        addComponentAtIndex(index, menuItem);
        return menuItem;
    }

    /**
     * Adds a component as a menu item at the given position.
     *
     * @param index     item position
     * @param component the component for the menu item
     * @return a new menu item
     */
    public I addItemAtIndex(int index, Component component) {
        I menuItem = itemGenerator.apply(menu, contentReset);
        addComponentAtIndex(index, menuItem);
        menuItem.add(component);
        return menuItem;
    }

    /**
     * Adds a text as a menu item with a click listener at the given position.
     *
     * @param index         item position
     * @param text          the text for the menu item
     * @param clickListener a click listener
     * @return a new menu item
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public I addItemAtIndex(int index, String text,
                            @Nullable ComponentEventListener<ClickEvent<I>> clickListener) {
        I menuItem = addItemAtIndex(index, text);
        if (clickListener != null) {
            ComponentUtil.addListener(menuItem, ClickEvent.class, (ComponentEventListener) clickListener);
        }
        return menuItem;
    }

    /**
     * Adds a component as a menu item with a click listener at the given position.
     *
     * @param index         item position
     * @param component     the component for the menu item
     * @param clickListener a click listener
     * @return a new menu item
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public I addItemAtIndex(int index, Component component,
                            @Nullable ComponentEventListener<ClickEvent<I>> clickListener) {
        I menuItem = addItemAtIndex(index, component);
        if (clickListener != null) {
            ComponentUtil.addListener(menuItem, ClickEvent.class, (ComponentEventListener) clickListener);
        }
        return menuItem;
    }
}
