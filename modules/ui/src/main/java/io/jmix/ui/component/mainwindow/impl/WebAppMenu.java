/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.mainwindow.impl;

import com.vaadin.server.ClientConnector;
import com.vaadin.server.Resource;
import com.vaadin.ui.MenuBar;
import io.jmix.ui.AppUI;
import io.jmix.ui.component.impl.WebAbstractComponent;
import io.jmix.ui.component.mainwindow.AppMenu;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.menu.MenuBuilder;
import io.jmix.ui.widget.JmixMenuBar;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class WebAppMenu extends WebAbstractComponent<JmixMenuBar> implements AppMenu {

    @Autowired
    protected MenuBuilder menuBuilder;

    @Autowired
    protected IconResolver iconResolver;

    protected Map<String, MenuItem> allItemsIds = new HashMap<>();
    protected Map<MenuBar.MenuItem, MenuItem> viewModelMap = new HashMap<>();

    public static final String MENU_STYLENAME = "c-main-menu";

    public WebAppMenu() {
        component = new JmixMenuBar();
        component.addStyleName(MENU_STYLENAME);

        component.addAttachListener(this::handleAttach);
    }

    protected void handleAttach(@SuppressWarnings("unused") ClientConnector.AttachEvent event) {
        AppUI appUi = (AppUI) component.getUI();
        if (appUi == null || !appUi.isTestMode()) {
            return;
        }

        for (Map.Entry<String, MenuItem> entry : allItemsIds.entrySet()) {
            assignCubaIds(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public void setStyleName(@Nullable String name) {
        super.setStyleName(name);

        component.addStyleName(MENU_STYLENAME);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(MENU_STYLENAME, ""));
    }

    @Override
    public void loadMenu() {
        menuBuilder.build(this);
    }

    @Override
    public MenuItem createMenuItem(String id) {
        return createMenuItem(id, id, null, null);
    }

    @Override
    public MenuItem createMenuItem(String id, String caption) {
        return createMenuItem(id, caption, null, null);
    }

    @Override
    public MenuItem createMenuItem(String id, String caption,
                                   @Nullable String icon, @Nullable Consumer<MenuItem> command) {
        checkNotNullArgument(id);
        checkItemIdDuplicate(id);

        MenuItemImpl menuItem = new MenuItemImpl(this, id);

        Resource iconResource = null;
        if (icon != null) {
            iconResource = iconResolver.getIconResource(icon);
        }

        MenuBar.MenuItem delegateItem = component.createMenuItem(caption, iconResource, null);
        if (command != null) {
            @SuppressWarnings("UnnecessaryLocalVariable")
            Consumer<MenuItem> nonnullCommand = command;

            delegateItem.setCommand(selectedItem ->
                    nonnullCommand.accept(menuItem));
        }
        menuItem.setDelegateItem(delegateItem);

        menuItem.setCaption(caption);
        menuItem.setIcon(icon);
        menuItem.setCommand(command);

        return menuItem;
    }

    protected void assignCubaIds(MenuItem menuItem, String id) {
        AppUI ui = (AppUI) component.getUI();
        if (ui == null || !ui.isTestMode()) {
            return;
        }

        MenuBar.MenuItem delegateItem = ((MenuItemImpl) menuItem).getDelegateItem();
        component.setJTestId(delegateItem, id);
    }

    @Override
    public void addMenuItem(MenuItem menuItem) {
        checkNotNullArgument(menuItem);
        checkItemIdDuplicate(menuItem.getId());
        checkItemOwner(menuItem);

        component.addMenuItem(((MenuItemImpl) menuItem).getDelegateItem());
        registerMenuItem(menuItem);

        assignCubaIds(menuItem, menuItem.getId());
    }

    @Override
    public void addMenuItem(MenuItem menuItem, int index) {
        checkNotNullArgument(menuItem);
        checkItemIdDuplicate(menuItem.getId());
        checkItemOwner(menuItem);

        component.addMenuItem(((MenuItemImpl) menuItem).getDelegateItem(), index);
        registerMenuItem(menuItem);

        assignCubaIds(menuItem, menuItem.getId());
    }

    protected void registerMenuItem(MenuItem menuItem) {
        allItemsIds.put(menuItem.getId(), menuItem);
        viewModelMap.put(((MenuItemImpl) menuItem).getDelegateItem(), menuItem);
    }

    @Override
    public void removeMenuItem(MenuItem menuItem) {
        checkNotNullArgument(menuItem);
        checkItemOwner(menuItem);

        component.removeMenuItem(((MenuItemImpl) menuItem).getDelegateItem());
        unregisterItem(menuItem);
    }

    @Override
    public void removeMenuItem(int index) {
        MenuBar.MenuItem delegateItem = component.getMenuItems().get(index);
        component.removeMenuItem(delegateItem);
        unregisterItem(viewModelMap.get(delegateItem));
    }

    protected void unregisterItem(MenuItem menuItem) {
        allItemsIds.remove(menuItem.getId());
        viewModelMap.remove(((MenuItemImpl) menuItem).getDelegateItem());
    }

    @Nullable
    @Override
    public MenuItem getMenuItem(String id) {
        return allItemsIds.get(id);
    }

    @Override
    public MenuItem getMenuItemNN(String id) {
        MenuItem menuItem = allItemsIds.get(id);
        if (menuItem == null) {
            throw new IllegalArgumentException("Unable to find menu item with id: " + id);
        }
        return menuItem;
    }

    @Override
    public List<MenuItem> getMenuItems() {
        return component.getMenuItems().stream()
                .map(viewItem -> viewModelMap.get(viewItem))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasMenuItems() {
        return component.hasMenuItems();
    }

    protected void checkItemIdDuplicate(String id) {
        if (allItemsIds.containsKey(id)) {
            throw new IllegalArgumentException(String.format("MenuItem with id \"%s\" already exists", id));
        }
    }

    protected void checkItemOwner(MenuItem item) {
        if (item.getMenu() != this) {
            throw new IllegalArgumentException("MenuItem is not created by this menu");
        }
    }

    @Override
    public MenuItem createSeparator() {
        MenuItemImpl menuItem = new MenuItemImpl(this, null);
        menuItem.setSeparator(true);

        MenuBar.MenuItem separator = component.createSeparator();
        menuItem.setDelegateItem(separator);

        return menuItem;
    }

    @Override
    public void setMenuItemShortcutCaption(MenuItem menuItem, String shortcut) {
        component.setShortcut(((MenuItemImpl) menuItem).getDelegateItem(), shortcut);
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    protected class MenuItemImpl implements MenuItem {
        protected WebAppMenu menu;
        protected String id;
        protected MenuBar.MenuItem delegateItem;
        protected Consumer<MenuItem> command;

        protected String icon;
        protected boolean separator;

        public MenuItemImpl(WebAppMenu menu, @Nullable String id) {
            this.menu = menu;
            this.id = id;
        }

        @Nullable
        @Override
        public String getId() {
            return id;
        }

        @Override
        public AppMenu getMenu() {
            return menu;
        }

        public MenuBar.MenuItem getDelegateItem() {
            return delegateItem;
        }

        public void setDelegateItem(MenuBar.MenuItem delegateItem) {
            this.delegateItem = delegateItem;
        }

        @Override
        public String getCaption() {
            return delegateItem.getText();
        }

        @Override
        public void setCaption(String caption) {
            delegateItem.setText(caption);
        }

        @Override
        public String getDescription() {
            return delegateItem.getDescription();
        }

        @Override
        public void setDescription(String description) {
            delegateItem.setDescription(description);
        }

        @Nullable
        @Override
        public String getIcon() {
            return icon;
        }

        @Override
        public void setIcon(@Nullable String icon) {
            this.icon = icon;

            if (icon != null) {
                Resource iconResource = iconResolver
                        .getIconResource(this.icon);
                delegateItem.setIcon(iconResource);
            } else {
                delegateItem.setIcon(null);
            }
        }

        @Override
        public boolean isVisible() {
            return delegateItem.isVisible();
        }

        @Override
        public void setVisible(boolean visible) {
            delegateItem.setVisible(visible);
        }

        @Override
        public String getStyleName() {
            return delegateItem.getStyleName();
        }

        @Override
        public void setStyleName(String styleName) {
            delegateItem.setStyleName(styleName);
        }

        @Nullable
        @Override
        public Consumer<MenuItem> getCommand() {
            return command;
        }

        @Override
        public void setCommand(@Nullable Consumer<MenuItem> command) {
            this.command = command;

            if (command != null) {
                delegateItem.setCommand(this::menuSelected);
            } else {
                delegateItem.setCommand(null);
            }
        }

        @Override
        public void addChildItem(MenuItem menuItem) {
            MenuBar.MenuItem childItem = ((MenuItemImpl) menuItem).getDelegateItem();
            if (childItem.getText() == null) {
                throw new IllegalArgumentException("Caption cannot be null");
            }

            MenuBar.MenuItem delegateItem = this.getDelegateItem();

            childItem.setParent(delegateItem);

            delegateItem.getChildren().add(childItem);
            menu.registerMenuItem(menuItem);

            menu.getComponent().markAsDirty();
        }

        @Override
        public void addChildItem(MenuItem menuItem, int index) {
            MenuBar.MenuItem childItem = ((MenuItemImpl) menuItem).getDelegateItem();
            if (childItem.getText() == null) {
                throw new IllegalArgumentException("Caption cannot be null");
            }

            MenuBar.MenuItem delegateItem = this.getDelegateItem();

            childItem.setParent(delegateItem);

            delegateItem.getChildren().add(index, childItem);
            menu.registerMenuItem(menuItem);

            menu.getComponent().markAsDirty();
        }

        @Override
        public void removeChildItem(MenuItem menuItem) {
            MenuBar.MenuItem childItem = ((MenuItemImpl) menuItem).getDelegateItem();

            getDelegateItem().getChildren().remove(childItem);
            menu.unregisterItem(menuItem);
        }

        @Override
        public void removeChildItem(int index) {
            MenuItem menuItem = getChildren().get(index);
            removeChildItem(menuItem);
            menu.unregisterItem(menuItem);
        }

        @Override
        public List<MenuItem> getChildren() {
            return delegateItem.getChildren().stream()
                    .map(menuItem -> menu.viewModelMap.get(menuItem))
                    .collect(Collectors.toList());
        }

        @Override
        public boolean hasChildren() {
            return delegateItem.hasChildren();
        }

        @Override
        public boolean isSeparator() {
            return separator;
        }

        protected void setSeparator(boolean separator) {
            this.separator = separator;
        }

        protected void menuSelected(@SuppressWarnings("unused") MenuBar.MenuItem event) {
            AppUI ui = (AppUI) menu.getComponent().getUI();
            if (ui.isAccessibleForUser(menu.getComponent())) {
                this.command.accept(this);
            } else {
                LoggerFactory.getLogger(WebAppMenu.class)
                        .debug("Ignore click because menu is inaccessible for user");
            }
        }
    }
}
