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

package io.jmix.ui.menu;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.AbstractComponent;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.AppUI;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.KeyCombination;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.mainwindow.SideMenu;
import io.jmix.ui.accesscontext.UiMenuContext;
import io.jmix.ui.screen.FrameOwner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static io.jmix.ui.component.KeyCombination.getShortcutModifiers;

/**
 * Side menu builder.
 */
@Internal
@Component("ui_SideMenuBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SideMenuBuilder {

    @Autowired
    protected MenuConfig menuConfig;
    @Autowired
    protected MenuItemCommands menuItemCommands;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected MessageTools messageTools;

    public SideMenuBuilder() {
    }

    public void build(SideMenu menu) {
        build(menu, menuConfig.getRootItems());
    }

    protected void build(SideMenu menu, List<MenuItem> rootItems) {
        Window window = ComponentsHelper.getWindowImplementation(menu);

        if (window == null) {
            throw new IllegalStateException("SideMenu is not belong to Window");
        }

        for (MenuItem menuItem : rootItems) {
            // AppMenu does not support separators
            UiMenuContext menuItemContext = new UiMenuContext(menuItem);
            accessManager.applyRegisteredConstraints(menuItemContext);
            if (menuItemContext.isPermitted() && !menuItem.isSeparator()) {
                createMenuBarItem(window, menu, menuItem);
            }
        }
        removeExtraSeparators(menu);
    }

    protected void removeExtraSeparators(SideMenu menuBar) {
        List<SideMenu.MenuItem> menuItems = menuBar.getMenuItems();
        for (SideMenu.MenuItem item : menuItems.toArray(new SideMenu.MenuItem[0])) {
            removeExtraSeparators(item);
            if (isMenuItemEmpty(item)) {
                menuBar.removeMenuItem(item);
            }
        }
    }

    protected void removeExtraSeparators(SideMenu.MenuItem item) {
        if (!item.hasChildren())
            return;

        // SideMenu does not support separator elements
        if (item.hasChildren()) {
            SideMenu.MenuItem[] menuItems =
                    item.getChildren().toArray(new SideMenu.MenuItem[0]);

            for (SideMenu.MenuItem child : menuItems) {
                removeExtraSeparators(child);
                if (isMenuItemEmpty(child)) {
                    item.removeChildItem(child);
                }
            }
        }
    }

    protected void createMenuBarItem(Window webWindow, SideMenu menu, MenuItem item) {
        if (isPermitted(item)) {
            SideMenu.MenuItem menuItem = menu.createMenuItem(item.getId(),
                    menuConfig.getItemCaption(item), null, createMenuBarCommand(item));

            createSubMenu(webWindow, menu, menuItem, item);
            assignStyleName(menuItem, item);
            assignIcon(menuItem, item);
            assignDescription(menuItem, item);
            assignExpanded(menuItem, item);
            assignShortcut(webWindow, menuItem, item);

            if (!isMenuItemEmpty(menuItem)) {
                menu.addMenuItem(menuItem);
            }
        }
    }

    protected void createSubMenu(Window webWindow, SideMenu menu, SideMenu.MenuItem vItem,
                                 MenuItem parentItem) {
        if (isPermitted(parentItem)) {
            for (MenuItem child : parentItem.getChildren()) {
                if (child.isSeparator()) {
                    continue;
                }

                if (isPermitted(child)) {
                    SideMenu.MenuItem menuItem = menu.createMenuItem(child.getId(),
                            menuConfig.getItemCaption(child));

                    assignDescription(menuItem, child);
                    assignIcon(menuItem, child);
                    assignStyleName(menuItem, child);

                    if (child.getChildren().isEmpty()) {
                        menuItem.setCommand(createMenuBarCommand(child));

                        assignShortcut(webWindow, menuItem, child);

                        vItem.addChildItem(menuItem);
                    } else {
                        createSubMenu(webWindow, menu, menuItem, child);

                        assignExpanded(menuItem, child);

                        if (!isMenuItemEmpty(menuItem)) {
                            vItem.addChildItem(menuItem);
                        }
                    }
                }
            }
        }
    }

    protected void assignExpanded(SideMenu.MenuItem menuItem, MenuItem item) {
        menuItem.setExpanded(item.isExpanded());
    }

    @Nullable
    protected Consumer<SideMenu.MenuItem> createMenuBarCommand(final MenuItem item) {
        if (!item.getChildren().isEmpty() || item.isMenu())     //check item is menu
            return null;

        return createMenuCommandExecutor(item);
    }

    protected Consumer<SideMenu.MenuItem> createMenuCommandExecutor(MenuItem item) {
        return new MenuCommandExecutor(menuItemCommands, item);
    }

    protected boolean isMenuItemEmpty(SideMenu.MenuItem menuItem) {
        return !menuItem.hasChildren() && menuItem.getCommand() == null;
    }

    protected void assignStyleName(SideMenu.MenuItem menuItem, MenuItem conf) {
        if (StringUtils.isNotEmpty(conf.getStylename())) {
            menuItem.setStyleName(conf.getStylename());
        }
    }

    protected void assignDescription(SideMenu.MenuItem menuItem, MenuItem conf) {
        String description = conf.getDescription();
        if (StringUtils.isNotEmpty(description)) {
            menuItem.setDescription(messageTools.loadString(description));
        }
    }

    protected void assignIcon(SideMenu.MenuItem menuItem, MenuItem conf) {
        if (conf.getIcon() != null) {
            menuItem.setIcon(conf.getIcon());
        }
    }

    protected void assignShortcut(Window webWindow, SideMenu.MenuItem menuItem, MenuItem item) {
        KeyCombination itemShortcut = item.getShortcut();
        if (itemShortcut != null) {
            ShortcutListener shortcut = new SideMenuShortcutListener(menuItem, item);

            AbstractComponent windowImpl = webWindow.unwrap(AbstractComponent.class);
            windowImpl.addShortcutListener(shortcut);

            if (Strings.isNullOrEmpty(menuItem.getBadgeText())) {
                menuItem.setDescription(itemShortcut.format());
            }
        }
    }

    protected boolean isPermitted(MenuItem item) {
        if (Strings.isNullOrEmpty(item.getId()) || item.isSeparator()) {
            return true;
        }
        UiMenuContext menuItemContext = new UiMenuContext(item);
        accessManager.applyRegisteredConstraints(menuItemContext);
        return menuItemContext.isPermitted();
    }

    protected static class SideMenuShortcutListener extends ShortcutListener {
        protected SideMenu.MenuItem menuItem;

        public SideMenuShortcutListener(SideMenu.MenuItem menuItem, MenuItem item) {
            super("shortcut_" + item.getId(),
                    item.getShortcut().getKey().getCode(),
                    getShortcutModifiers(item.getShortcut().getModifiers()));
            this.menuItem = menuItem;
        }

        @Override
        public void handleAction(Object sender, Object target) {
            com.vaadin.ui.Component menuImpl = menuItem.getMenu().unwrap(com.vaadin.ui.Component.class);
            AppUI ui = (AppUI) menuImpl.getUI();
            if (ui.isAccessibleForUser(menuImpl)) {
                Consumer<SideMenu.MenuItem> command = menuItem.getCommand();
                if (command != null) {
                    command.accept(menuItem);
                }
            } else {
                LoggerFactory.getLogger(SideMenuShortcutListener.class)
                        .debug("Ignoring shortcut action because menu is inaccessible for user");
            }
        }
    }

    public static class MenuCommandExecutor implements Consumer<SideMenu.MenuItem> {
        private final MenuItem item;
        private final MenuItemCommands menuItemCommands;

        public MenuCommandExecutor(MenuItemCommands menuItemCommands, MenuItem item) {
            this.item = item;
            this.menuItemCommands = menuItemCommands;
        }

        @Override
        public void accept(SideMenu.MenuItem menuItem) {
            SideMenu menu = menuItem.getMenu();
            FrameOwner frameOwner = menu.getFrame().getFrameOwner();

            MenuItemCommand command = menuItemCommands.create(frameOwner, item);
            command.run();
        }
    }
}
