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

package io.jmix.flowui.kit.component.grid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.grid.contextmenu.GridSubMenu;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableRunnable;
import io.jmix.flowui.kit.component.contextmenu.JmixMenuManager;

import javax.annotation.Nullable;

public class JmixGridContextMenu<E> extends GridContextMenu<E> {

    public GridMenuItem<E> addItemAtIndex(int index, String text) {
        return getItems().size() == index
                ? getMenuManager().addItem(text)
                : getMenuManager().addItemAtIndex(index, text);
    }

    public GridMenuItem<E> addItemAtIndex(int index, String text,
                                          @Nullable ComponentEventListener<GridContextMenuItemClickEvent<E>> clickListener) {
        GridMenuItem<E> menuItem = addItemAtIndex(index, text);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }
        return menuItem;
    }

    public GridMenuItem<E> addItemAtIndex(int index, Component component) {
        return getItems().size() == index
                ? getMenuManager().addItem(component)
                : getMenuManager().addItemAtIndex(index, component);
    }

    public GridMenuItem<E> addItemAtIndex(int index, Component component,
                                          @Nullable ComponentEventListener<GridContextMenuItemClickEvent<E>> clickListener) {
        GridMenuItem<E> menuItem = addItemAtIndex(index, component);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }
        return menuItem;
    }

    @Override
    protected MenuManager<GridContextMenu<E>, GridMenuItem<E>, GridSubMenu<E>> createMenuManager(SerializableRunnable contentReset) {
        SerializableBiFunction itemFactory = (menu, reset) -> new GridMenuItem<>((GridContextMenu<?>) menu,
                (SerializableRunnable) reset);
        return new JmixMenuManager(this, contentReset, itemFactory, GridMenuItem.class, null);
    }

    @Override
    protected JmixMenuManager<GridContextMenu<E>, GridMenuItem<E>, GridSubMenu<E>> getMenuManager() {
        return (JmixMenuManager<GridContextMenu<E>, GridMenuItem<E>, GridSubMenu<E>>) super.getMenuManager();
    }
}
