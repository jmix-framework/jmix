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
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;
import io.jmix.flowui.kit.component.menubar.JmixSubMenu;
import org.jspecify.annotations.Nullable;

import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.function.Consumer;

/**
 * Delegate class for managing {@link UserMenuItem} collection.
 */
public class JmixUserMenuItemsDelegate implements HasTextMenuItems, HasActionMenuItems, HasComponentMenuItems {

    protected final JmixUserMenu<?> userMenu;
    protected final JmixSubMenu subMenu;

    protected List<UserMenuItem> items = new ArrayList<>();
    protected Map<UserMenuItem, Registration> propertyChangeRegistrations = new HashMap<>();

    public JmixUserMenuItemsDelegate(JmixUserMenu<?> userMenu, JmixSubMenu subMenu) {
        this.userMenu = userMenu;
        this.subMenu = subMenu;
    }

    @Override
    public TextUserMenuItem addTextItem(String id, String text) {
        return addTextItem(id, text, -1);
    }

    @Override
    public TextUserMenuItem addTextItem(String id, String text, int index) {
        return addItemInternal(id, text, null, null, index);
    }

    @Override
    public TextUserMenuItem addTextItem(String id, String text,
                                        Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener) {
        return addTextItem(id, text, listener, -1);
    }

    @Override
    public TextUserMenuItem addTextItem(String id, String text,
                                        Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener,
                                        int index) {
        return addItemInternal(id, text, null, listener, index);
    }

    @Override
    public TextUserMenuItem addTextItem(String id, String text, Component icon) {
        return addTextItem(id, text, icon, -1);
    }

    @Override
    public TextUserMenuItem addTextItem(String id, String text, Component icon, int index) {
        return addItemInternal(id, text, icon, null, index);
    }

    @Override
    public TextUserMenuItem addTextItem(String id,
                                        String text, Component icon,
                                        Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener) {
        return addTextItem(id, text, icon, listener, -1);
    }

    @Override
    public TextUserMenuItem addTextItem(String id,
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
    public ActionUserMenuItem addActionItem(String id, Action action) {
        return addActionItem(id, action, -1);
    }

    @Override
    public ActionUserMenuItem addActionItem(String id, Action action, int index) {
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
    public ComponentUserMenuItem addComponentItem(String id, Component content) {
        return addComponentItem(id, content, -1);
    }

    @Override
    public ComponentUserMenuItem addComponentItem(String id, Component content, int index) {
        return addItemInternal(id, content, null, index);
    }

    @Override
    public ComponentUserMenuItem addComponentItem(String id, Component content,
                                                  Consumer<UserMenuItem.HasClickListener.ClickEvent<ComponentUserMenuItem>> listener) {
        return addComponentItem(id, content, listener, -1);
    }

    @Override
    public ComponentUserMenuItem addComponentItem(String id, Component content,
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
        Hr separator = new Hr();
        subMenu.addComponent(separator);
        addItemInternal(new SeparatorUserMenuItem(separator), -1);
    }

    @Override
    public void addSeparatorAtIndex(int index) {
        Hr separator = new Hr();
        subMenu.addComponentAtIndex(adjustPhysicalIndex(index), separator);
        addItemInternal(new SeparatorUserMenuItem(separator), index);
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
        Registration registration = item.addPropertyChangeListener(this::onItemPropertyChange);
        if (registration != null) {
            propertyChangeRegistrations.put(item, registration);
        }

        updateItemsVisibility();
    }

    protected void onItemPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        if (UserMenuItem.PROP_VISIBLE.equals(propertyChangeEvent.getPropertyName())) {
            updateItemsVisibility();
        }
    }

    /**
     * Updates the visibility of menu items based on their properties and position.
     */
    public void updateItemsVisibility() {
        List<UserMenuItem> visibleItems = items.stream()
                .filter(item ->
                        item instanceof SeparatorUserMenuItem
                                || item.isVisible()
                )
                .toList();

        if (visibleItems.isEmpty()) {
            return;
        }

        int i = 0;
        // When the physical children's count equals the number of menu
        // items, it means that the header content is not set and the
        // separator component does not separate it from the menu items.
        // This means we need to hide all beginning separators.
        if (subMenu.getChildren().count() == items.size()) {
            for (; i < visibleItems.size(); i++) {
                UserMenuItem item = visibleItems.get(i);
                if (item instanceof SeparatorUserMenuItem separator) {
                    separator.setVisible(false);
                } else {
                    break;
                }
            }
        }

        for (; i < visibleItems.size(); i++) {
            UserMenuItem item = visibleItems.get(i);
            if (item instanceof SeparatorUserMenuItem separator) {
                separator.setVisible(true);

                if ((i + 1) < visibleItems.size()
                        // Several separators are located one by one
                        && visibleItems.get(i + 1) instanceof SeparatorUserMenuItem) {
                    separator.setVisible(false);
                } else if (i == visibleItems.size() - 1) {
                    separator.setVisible(false);
                }
            }
        }
    }

    protected JmixMenuItem createMenuItem(String id, Component content, int index) {
        JmixMenuItem menuItem = index < 0
                ? subMenu.addItem(content)
                : subMenu.addItemAtIndex(adjustPhysicalIndex(index), content);

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
        return items.stream()
                .filter(userMenuItem -> !(userMenuItem instanceof SeparatorUserMenuItem))
                .toList();
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
        Registration registration = propertyChangeRegistrations.remove(item);
        // SeparatorUserMenuItem returns null
        if (registration != null) {
            registration.remove();
        }

        updateItemsVisibility();
    }

    @Override
    public void removeAll() {
        // Remove each item individually to handle detachment
        new ArrayList<>(items).forEach(this::remove);
    }

    protected int adjustPhysicalIndex(int index) {
        // If the physical children's count is greater than the number of
        // menu items, it means that the header content has been set and
        // we need to increase the component index by 1.
        if (subMenu.getChildren().count() > items.size()) {
            index++;
        }

        return index;
    }

    /**
     * Blank item needed for correct insertion by index.
     */
    protected static class SeparatorUserMenuItem implements UserMenuItem {

        protected final Component separator;

        protected SeparatorUserMenuItem(Component separator) {
            this.separator = separator;
        }

        @Override
        public String getId() {
            return "";
        }

        @Override
        public void setVisible(boolean visible) {
            separator.setVisible(visible);
        }

        @Override
        public boolean isVisible() {
            return separator.isVisible();
        }

        @Override
        public void setEnabled(boolean enabled) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public boolean isCheckable() {
            return false;
        }

        @Override
        public void setCheckable(boolean checkable) {
        }

        @Override
        public boolean isChecked() {
            return false;
        }

        @Override
        public void setChecked(boolean checked) {
        }

        @Override
        public Registration addPropertyChangeListener(Consumer<PropertyChangeEvent> listener) {
            return null;
        }

        @Override
        public Element getElement() {
            return null;
        }

        @Nullable
        @Override
        public Object getSubPart(String name) {
            return null;
        }
    }
}
