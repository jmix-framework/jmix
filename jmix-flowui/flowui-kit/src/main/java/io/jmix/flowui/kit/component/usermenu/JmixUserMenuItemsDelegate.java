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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Hr;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;
import io.jmix.flowui.kit.component.menubar.JmixSubMenu;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Delegate class for managing {@link UserMenuItem} collection.
 */
public class JmixUserMenuItemsDelegate implements HasTextMenuItems, HasActionMenuItems, HasComponentMenuItems {

    protected final JmixUserMenu<?> userMenu;
    protected final JmixSubMenu subMenu;

    protected List<UserMenuItem> items = new ArrayList<>();

    public JmixUserMenuItemsDelegate(JmixUserMenu<?> userMenu, JmixSubMenu subMenu) {
        this.userMenu = userMenu;
        this.subMenu = subMenu;
    }

    @Override
    public TextUserMenuItem addItem(String id, String text) {
        return addItem(id, text, -1);
    }

    @Override
    public TextUserMenuItem addItem(String id, String text, int index) {
        return addItemInternal(id, text, null, null, index);
    }

    @Override
    public TextUserMenuItem addItem(String id, String text,
                                    Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener) {
        return addItem(id, text, listener, -1);
    }

    @Override
    public TextUserMenuItem addItem(String id, String text,
                                    Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener,
                                    int index) {
        return addItemInternal(id, text, null, listener, index);
    }

    @Override
    public TextUserMenuItem addItem(String id, String text, Component icon) {
        return addItem(id, text, icon, -1);
    }

    @Override
    public TextUserMenuItem addItem(String id, String text, Component icon, int index) {
        return addItemInternal(id, text, icon, null, index);
    }

    @Override
    public TextUserMenuItem addItem(String id,
                                    String text, Component icon,
                                    Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener) {
        return addItem(id, text, icon, listener, -1);
    }

    @Override
    public TextUserMenuItem addItem(String id,
                                    String text, Component icon,
                                    Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener,
                                    int index) {
        return addItemInternal(id, text, icon, listener, index);
    }

    protected TextUserMenuItem addItemInternal(String id,
                                               String text, @Nullable Component icon,
                                               @Nullable Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener,
                                               int index) {
        TextUserMenuItem menuItem = new JmixUserMenu.TextUserMenuItemImpl(
                id,
                userMenu,
                createMenuItem(id, new Text(text), index),
                text
        );

        if (icon != null) {
            menuItem.setIcon(icon);
        }

        if (listener != null) {
            menuItem.addClickListener(listener);
        }

        addItemInternal(menuItem, index);

        return menuItem;
    }

    @Override
    public ActionUserMenuItem addItem(String id, Action action) {
        return addItem(id, action, -1);
    }

    @Override
    public ActionUserMenuItem addItem(String id, Action action, int index) {
        ActionUserMenuItem menuItem = new JmixUserMenu.ActionUserMenuItemImpl(
                id,
                userMenu,
                createMenuItem(id, new Text(Strings.nullToEmpty(action.getText())), index),
                action
        );

        addItemInternal(menuItem, index);

        return menuItem;
    }

    @Override
    public ComponentUserMenuItem addItem(String id, Component content) {
        return addItem(id, content, -1);
    }

    @Override
    public ComponentUserMenuItem addItem(String id, Component content, int index) {
        return addItemInternal(id, content, null, index);
    }

    @Override
    public ComponentUserMenuItem addItem(String id, Component content,
                                         Consumer<UserMenuItem.HasClickListener.ClickEvent<ComponentUserMenuItem>> listener) {
        return addItem(id, content, listener, -1);
    }

    @Override
    public ComponentUserMenuItem addItem(String id, Component content,
                                         Consumer<UserMenuItem.HasClickListener.ClickEvent<ComponentUserMenuItem>> listener,
                                         int index) {
        return addItemInternal(id, content, listener, index);
    }

    protected ComponentUserMenuItem addItemInternal(String id, Component content,
                                                    @Nullable Consumer<UserMenuItem.HasClickListener.ClickEvent<ComponentUserMenuItem>> listener,
                                                    int index) {
        ComponentUserMenuItem menuItem = new JmixUserMenu.ComponentUserMenuItemImpl(
                id,
                userMenu,
                createMenuItem(id, content, index),
                content
        );

        if (listener != null) {
            menuItem.addClickListener(listener);
        }

        addItemInternal(menuItem, index);

        return menuItem;
    }

    @Override
    public void addSeparator() {
        subMenu.addComponent(new Hr());
    }

    @Override
    public void addSeparatorAtIndex(int index) {
        subMenu.addComponentAtIndex(index, new Hr());
    }

    protected void addItemInternal(UserMenuItem item, int index) {
        if (index < 0) {
            items.add(item);
        } else {
            items.add(index, item);
        }

        attachItem(item);
    }

    protected void attachItem(UserMenuItem item) {
        // Hook to be implemented...
    }

    protected JmixMenuItem createMenuItem(String id, Component content, int index) {
        JmixMenuItem menuItem = index < 0
                ? subMenu.addItem(content)
                : subMenu.addItemAtIndex(index, content);

        menuItem.setId(id);

        return menuItem;
    }

    @Override
    public Optional<UserMenuItem> findItem(String itemId) {
        return items.stream()
                .filter(item -> itemId.equals(item.getId()))
                .findAny();
    }

    @Override
    public UserMenuItem getItem(String itemId) {
        return findItem(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Item with id '%s' not found".formatted(itemId)));
    }

    @Override
    public List<UserMenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public void remove(String itemId) {
        findItem(itemId)
                .ifPresent(this::remove);
    }

    @Override
    public void remove(UserMenuItem menuItem) {
        Preconditions.checkArgument(menuItem != null,
                "%s cannot be null".formatted(UserMenuItem.class.getSimpleName()));

        if (menuItem instanceof JmixUserMenu.HasMenuItem hasMenuItem) {
            if (items.remove(menuItem)) {
                subMenu.remove(hasMenuItem.getItem());

                detachItem(menuItem);
            }
        } else {
            throw new IllegalStateException("%s doesn't contain item"
                    .formatted(menuItem.getClass().getSimpleName()));
        }
    }

    protected void detachItem(UserMenuItem item) {
        // Hook to be implemented...
    }

    @Override
    public void removeAll() {
        // Remove each item individually to handle detachment
        new ArrayList<>(items).forEach(this::remove);
    }
}
