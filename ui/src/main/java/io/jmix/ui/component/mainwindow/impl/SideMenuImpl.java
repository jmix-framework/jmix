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
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.AppUI;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.impl.AbstractComponent;
import io.jmix.ui.component.mainwindow.SideMenu;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.menu.SideMenuBuilder;
import io.jmix.ui.theme.ThemeClassNames;
import io.jmix.ui.widget.JmixSideMenu;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class SideMenuImpl extends AbstractComponent<JmixSideMenu> implements SideMenu {

    protected Map<String, MenuItem> allItemsIds = new HashMap<>();

    protected Button toggleButton;
    protected Component sidePanel;

    public SideMenuImpl() {
        component = new JmixSideMenu();
        component.setBeforeMenuItemTriggeredHandler(menuItem -> {
            if (sidePanel != null) {
                sidePanel.removeStyleName(ThemeClassNames.SIDEMENU_PANEL_OPEN);
            }
        });

        component.addAttachListener(this::handleAttach);
    }

    protected void handleAttach(@SuppressWarnings("unused") ClientConnector.AttachEvent attachEvent) {
        AppUI appUi = (AppUI) component.getUI();
        if (appUi == null || !appUi.isTestMode()) {
            return;
        }

        for (JmixSideMenu.MenuItem vMenuItem : component.getMenuItems()) {
            assignJTestId(((MenuItemWrapper) vMenuItem).getMenuItem());
        }
    }

    @Override
    public void loadMenuConfig() {
        SideMenuBuilder menuBuilder = applicationContext.getBean(SideMenuBuilder.class);
        menuBuilder.build(this);
    }

    @Override
    public void setSidePanelToggleButton(@Nullable Button toggleButton) {
        if (this.toggleButton != null) {
            this.toggleButton.setAction(null);
        }

        if (toggleButton != null) {
            AbstractAction toggleAction = new AbstractAction("toggleSideMenu") {
                @Override
                public void actionPerform(Component component) {
                    toggleSidePanel();
                }
            };

            toggleAction.setCaption(toggleButton.getCaption());
            toggleAction.setIcon(toggleButton.getIcon());
            toggleAction.setDescription(toggleButton.getDescription());
            toggleAction.setEnabled(toggleButton.isEnabled());
            toggleAction.setVisible(toggleButton.isVisible());

            toggleButton.setAction(toggleAction);
        }

        this.toggleButton = toggleButton;
    }

    protected void toggleSidePanel() {
        if (sidePanel != null) {
            if (sidePanel.getStyleName().contains(ThemeClassNames.SIDEMENU_PANEL_OPEN)) {
                sidePanel.removeStyleName(ThemeClassNames.SIDEMENU_PANEL_OPEN);
            } else {
                sidePanel.addStyleName(ThemeClassNames.SIDEMENU_PANEL_OPEN);
            }
        }
    }

    @Nullable
    @Override
    public Button getSidePanelToggleButton() {
        return toggleButton;
    }

    @Override
    public void setSidePanel(@Nullable Component sidePanel) {
        this.sidePanel = sidePanel;
    }

    @Nullable
    @Override
    public Component getSidePanel() {
        return sidePanel;
    }

    @Override
    public boolean isSelectOnClick() {
        return component.isSelectOnClick();
    }

    @Override
    public void setSelectOnClick(boolean selectOnClick) {
        component.setSelectOnClick(selectOnClick);
    }

    @Nullable
    @Override
    public MenuItem getSelectedItem() {
        JmixSideMenu.MenuItem selectedItem = component.getSelectedItem();
        return selectedItem != null
                ? ((MenuItemWrapper) selectedItem).getMenuItem()
                : null;
    }

    @Override
    public void setSelectedItem(MenuItem selectedItem) {
        component.setSelectedItem(((MenuItemImpl) selectedItem).getDelegateItem());
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

        MenuItemWrapper delegateItem = new MenuItemWrapper();

        MenuItem menuItem = new MenuItemImpl(this, id, delegateItem);
        menuItem.setCaption(caption);
        menuItem.setIcon(icon);
        menuItem.setCommand(command);

        delegateItem.setMenuItem(menuItem);

        return menuItem;
    }

    protected void assignJTestId(MenuItem menuItem) {
        AppUI ui = AppUI.getCurrent();
        if (ui == null || !ui.isTestMode())
            return;

        assignJTestIdInternal(menuItem);
    }

    protected void assignJTestIdInternal(MenuItem menuItem) {
        ((MenuItemImpl) menuItem).setJTestId(menuItem.getId());

        if (menuItem.hasChildren()) {
            for (MenuItem item : menuItem.getChildren()) {
                assignJTestIdInternal(item);
            }
        }
    }

    @Override
    public void addMenuItem(MenuItem menuItem) {
        checkNotNullArgument(menuItem);
        checkItemIdDuplicate(menuItem.getId());
        checkItemOwner(menuItem);

        component.addMenuItem(((MenuItemImpl) menuItem).getDelegateItem());
        registerMenuItem(menuItem);

        assignJTestId(menuItem);
    }

    protected void registerMenuItem(MenuItem menuItem) {
        allItemsIds.put(menuItem.getId(), menuItem);
        if (menuItem.hasChildren()) {
            for (MenuItem item : menuItem.getChildren()) {
                registerMenuItem(item);
            }
        }
    }

    protected void unregisterItem(MenuItem menuItem) {
        allItemsIds.remove(menuItem.getId());
        if (menuItem.hasChildren()) {
            for (MenuItem item : menuItem.getChildren()) {
                unregisterItem(item);
            }
        }
    }

    @Override
    public void addMenuItem(MenuItem menuItem, int index) {
        checkNotNullArgument(menuItem);
        checkItemIdDuplicate(menuItem.getId());
        checkItemOwner(menuItem);

        component.addMenuItem(((MenuItemImpl) menuItem).getDelegateItem(), index);
        registerMenuItem(menuItem);

        assignJTestId(menuItem);
    }

    @Override
    public void removeMenuItem(MenuItem menuItem) {
        checkNotNullArgument(menuItem);
        checkItemOwner(menuItem);

        if (getMenuItems().contains(menuItem)) {
            unregisterItem(menuItem);
        }

        component.removeMenuItem(((MenuItemImpl) menuItem).getDelegateItem());
    }

    @Override
    public void removeAllMenuItems() {
        for (JmixSideMenu.MenuItem menuItem : new ArrayList<>(component.getMenuItems())) {
            component.removeMenuItem(menuItem);
            unregisterItem(((MenuItemWrapper) menuItem).getMenuItem());
        }
    }

    @Override
    public void removeMenuItem(int index) {
        JmixSideMenu.MenuItem delegateItem = component.getMenuItems().get(index);
        component.removeMenuItem(index);
        unregisterItem(((MenuItemWrapper) delegateItem).getMenuItem());
    }

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
                .map(delegateItem -> ((MenuItemWrapper) delegateItem).getMenuItem())
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasMenuItems() {
        return component.hasMenuItems();
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

    protected static class MenuItemWrapper extends JmixSideMenu.MenuItem {
        protected MenuItem menuItem;

        public MenuItemWrapper() {
        }

        public MenuItem getMenuItem() {
            return menuItem;
        }

        public void setMenuItem(MenuItem menuItem) {
            this.menuItem = menuItem;
        }
    }

    @Override
    public void setShowSingleExpandedMenu(boolean singleExpandedMenu) {
        component.setShowSingleExpandedMenu(singleExpandedMenu);
    }

    @Override
    public boolean isShowSingleExpandedMenu() {
        return component.isShowSingleExpandedMenu();
    }

    @Override
    public Subscription addItemSelectListener(Consumer<ItemSelectEvent> listener) {
        return getEventHub().subscribe(ItemSelectEvent.class, listener);
    }

    protected static class MenuItemImpl implements MenuItem {
        protected SideMenuImpl menu;
        protected String id;
        protected JmixSideMenu.MenuItem delegateItem;
        protected Consumer<MenuItem> command;

        protected String icon;

        public MenuItemImpl(SideMenuImpl menu, String id, JmixSideMenu.MenuItem delegateItem) {
            this.menu = menu;
            this.id = id;
            this.delegateItem = delegateItem;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public SideMenu getMenu() {
            return menu;
        }

        public JmixSideMenu.MenuItem getDelegateItem() {
            return delegateItem;
        }

        @Nullable
        @Override
        public String getCaption() {
            return delegateItem.getCaption();
        }

        @Override
        public void setCaption(@Nullable String caption) {
            delegateItem.setCaption(caption);
        }

        @Nullable
        @Override
        public String getDescription() {
            return delegateItem.getDescription();
        }

        @Override
        public void setDescription(@Nullable String description) {
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
                Resource iconResource = menu.applicationContext.getBean(IconResolver.class)
                        .getIconResource(this.icon);
                delegateItem.setIcon(iconResource);
            } else {
                delegateItem.setIcon(null);
            }
        }

        @Override
        public boolean isCaptionAsHtml() {
            return delegateItem.isCaptionAsHtml();
        }

        @Override
        public void setCaptionAsHtml(boolean captionAsHtml) {
            delegateItem.setCaptionAsHtml(captionAsHtml);
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
        public boolean isExpanded() {
            return delegateItem.isExpanded();
        }

        @Override
        public void setExpanded(boolean expanded) {
            delegateItem.setExpanded(expanded);
        }

        @Override
        public String getStyleName() {
            return delegateItem.getStyleName();
        }

        @Override
        public void setStyleName(@Nullable String styleName) {
            delegateItem.setStyleName(styleName);
        }

        @Override
        public void addStyleName(String styleName) {
            delegateItem.addStyleName(styleName);
        }

        @Override
        public void removeStyleName(String styleName) {
            delegateItem.removeStyleName(styleName);
        }

        @Nullable
        @Override
        public String getBadgeText() {
            return delegateItem.getBadgeText();
        }

        @Override
        public void setBadgeText(@Nullable String badgeText) {
            delegateItem.setBadgeText(badgeText);
        }

        protected String getJTestId() {
            return delegateItem.getJTestId();
        }

        protected void setJTestId(String jTestId) {
            delegateItem.setJTestId(jTestId);
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
            checkNotNullArgument(menuItem);
            menu.checkItemOwner(menuItem);

            delegateItem.addChildItem(((MenuItemImpl) menuItem).getDelegateItem());

            menu.registerMenuItem(menuItem);
        }

        @Override
        public void addChildItem(MenuItem menuItem, int index) {
            checkNotNullArgument(menuItem);
            menu.checkItemOwner(menuItem);

            delegateItem.addChildItem(((MenuItemImpl) menuItem).getDelegateItem(), index);

            menu.registerMenuItem(menuItem);
        }

        @Override
        public void removeChildItem(MenuItem menuItem) {
            checkNotNullArgument(menuItem);
            menu.checkItemOwner(menuItem);

            if (getChildren().contains(menuItem)) {
                menu.unregisterItem(menuItem);
            }

            delegateItem.removeChildItem(((MenuItemImpl) menuItem).getDelegateItem());
        }

        @Override
        public void removeAllChildItems() {
            for (JmixSideMenu.MenuItem menuItem : new ArrayList<>(delegateItem.getChildren())) {
                delegateItem.removeChildItem(menuItem);

                menu.unregisterItem(((MenuItemWrapper) menuItem).getMenuItem());
            }
        }

        @Override
        public void removeChildItem(int index) {
            JmixSideMenu.MenuItem menuItem = delegateItem.getChildren().get(index);
            delegateItem.removeChildItem(index);

            menu.unregisterItem(((MenuItemWrapper) menuItem).getMenuItem());
        }

        @Override
        public List<MenuItem> getChildren() {
            return delegateItem.getChildren().stream()
                    .map(delegateItem -> ((MenuItemWrapper) delegateItem).getMenuItem())
                    .collect(Collectors.toList());
        }

        @Override
        public boolean hasChildren() {
            return delegateItem.hasChildren();
        }

        @Nullable
        @Override
        public MenuItem getParent() {
            JmixSideMenu.MenuItem parent = delegateItem.getParent();
            return parent != null ? ((MenuItemWrapper) parent).getMenuItem() : null;
        }

        @Override
        public MenuItem getParentNN() {
            if (delegateItem.getParent() == null) {
                throw new IllegalArgumentException("Unable to find parent for menu item with id: " + id);
            }
            return ((MenuItemWrapper) delegateItem.getParent()).getMenuItem();
        }

        @SuppressWarnings("unused")
        protected void menuSelected(JmixSideMenu.MenuItemTriggeredEvent event) {
            this.menu.getEventHub().publish(ItemSelectEvent.class, new ItemSelectEvent(this.menu, this));
            this.command.accept(this);
        }
    }
}
