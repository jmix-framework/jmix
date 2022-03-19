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
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.KeyCombination;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.mainwindow.AppMenu;
import io.jmix.ui.accesscontext.UiMenuContext;
import io.jmix.ui.screen.FrameOwner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main menu builder.
 */
@Internal
@Component("ui_AppMenuBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MenuBuilder {

    @Autowired
    protected MenuConfig menuConfig;
    @Autowired
    protected MenuItemCommands menuItemCommands;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected AccessManager accessManager;

    protected AppMenu appMenu;

    public MenuBuilder() {
    }

    public void build(AppMenu appMenu) {
        build(appMenu, menuConfig.getRootItems());
    }

    protected void build(AppMenu appMenu, List<MenuItem> rootItems) {
        this.appMenu = appMenu;

        Window window = ComponentsHelper.getWindowImplementation(appMenu);
        if (window == null) {
            throw new IllegalStateException("AppMenu is not belong to Window");
        }

        for (MenuItem menuItem : rootItems) {
            // AppMenu does not load top-level separators
            if (isPermitted(menuItem) && !menuItem.isSeparator()) {
                createMenuBarItem(window, menuItem);
            }
        }

        removeExtraSeparators();
    }

    protected void removeExtraSeparators() {
        for (AppMenu.MenuItem item : new ArrayList<>(appMenu.getMenuItems())) {
            removeExtraSeparators(item);

            if (isMenuItemEmpty(item)) {
                appMenu.removeMenuItem(item);
            }
        }
    }

    protected void removeExtraSeparators(AppMenu.MenuItem item) {
        if (!item.hasChildren()) {
            return;
        }

        boolean done;
        do {
            done = true;
            if (item.hasChildren()) {
                AppMenu.MenuItem[] children =
                        item.getChildren().toArray(new AppMenu.MenuItem[0]);

                for (int i = 0; i < children.length; i++) {
                    AppMenu.MenuItem child = children[i];

                    removeExtraSeparators(child);

                    if (isMenuItemEmpty(child)) {
                        item.removeChildItem(child);
                        done = false;
                    } else if (child.isSeparator()) {
                        if (i == 0 || i == children.length - 1 || children[i + 1].isSeparator()) {
                            item.removeChildItem(child);
                            done = false;
                        }
                    }
                }
            }
        } while (!done);
    }

    protected void createMenuBarItem(Window webWindow, MenuItem item) {
        if (isPermitted(item)) {
            AppMenu.MenuItem menuItem = appMenu.createMenuItem(item.getId(), menuConfig.getItemCaption(item),
                    null, createMenuBarCommand(item));

            assignShortcut(webWindow, menuItem, item);
            assignStyleName(menuItem, item);
            assignIcon(menuItem, item);
            assignDescription(menuItem, item);

            createSubMenu(webWindow, menuItem, item);

            if (!isMenuItemEmpty(menuItem)) {
                appMenu.addMenuItem(menuItem);
            }
        }
    }

    protected void createSubMenu(Window webWindow, AppMenu.MenuItem vItem, MenuItem item) {
        if (isPermitted(item) && !item.getChildren().isEmpty()) {
            for (MenuItem child : item.getChildren()) {
                if (child.getChildren().isEmpty()) {
                    if (isPermitted(child)) {
                        if (child.isSeparator()) {
                            vItem.addChildItem(appMenu.createSeparator());
                            continue;
                        }

                        AppMenu.MenuItem menuItem = appMenu.createMenuItem(child.getId(),
                                menuConfig.getItemCaption(child), null, createMenuBarCommand(child));

                        assignShortcut(webWindow, menuItem, child);
                        assignDescription(menuItem, child);
                        assignIcon(menuItem, child);
                        assignStyleName(menuItem, child);

                        vItem.addChildItem(menuItem);
                    }
                } else {
                    if (isPermitted(child)) {
                        AppMenu.MenuItem menuItem = appMenu.createMenuItem(child.getId(),
                                menuConfig.getItemCaption(child), null, null);

                        assignShortcut(webWindow, menuItem, child);
                        assignDescription(menuItem, child);
                        assignIcon(menuItem, child);
                        assignStyleName(menuItem, child);

                        createSubMenu(webWindow, menuItem, child);

                        if (!isMenuItemEmpty(menuItem)) {
                            vItem.addChildItem(menuItem);
                        }
                    }
                }
            }
        }
    }

    @Nullable
    protected Consumer<AppMenu.MenuItem> createMenuBarCommand(final MenuItem item) {
        if (CollectionUtils.isNotEmpty(item.getChildren()) || item.isMenu())     //check item is menu
        {
            return null;
        }

        return createMenuCommandExecutor(item);
    }

    protected Consumer<AppMenu.MenuItem> createMenuCommandExecutor(MenuItem item) {
        return new MenuCommandExecutor(menuItemCommands, item);
    }

    protected boolean isMenuItemEmpty(AppMenu.MenuItem menuItem) {
        return menuItem.getCommand() == null
                && !menuItem.isSeparator()
                && !menuItem.hasChildren();
    }

    protected void assignShortcut(Window webWindow, AppMenu.MenuItem menuItem, MenuItem item) {
        KeyCombination itemShortcut = item.getShortcut();
        if (itemShortcut != null) {
            ShortcutListener shortcut = new MenuShortcutAction(menuItem, "shortcut_" + item.getId(), item.getShortcut());

            AbstractComponent windowImpl = webWindow.unwrap(AbstractComponent.class);
            windowImpl.addShortcutListener(shortcut);

            appMenu.setMenuItemShortcutCaption(menuItem, itemShortcut.format());
        }
    }

    protected void assignStyleName(AppMenu.MenuItem menuItem, MenuItem conf) {
        if (StringUtils.isNotEmpty(conf.getStylename())) {
            menuItem.setStyleName("ms " + conf.getStylename());
        }
    }

    protected void assignDescription(AppMenu.MenuItem menuItem, MenuItem conf) {
        String description = conf.getDescription();
        if (StringUtils.isNotEmpty(description)) {
            menuItem.setDescription(messageTools.loadString(description));
        }
    }

    protected void assignIcon(AppMenu.MenuItem menuItem, MenuItem conf) {
        if (conf.getIcon() != null) {
            menuItem.setIcon(conf.getIcon());
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

    public static class MenuCommandExecutor implements Consumer<AppMenu.MenuItem> {
        private final MenuItem item;
        private final MenuItemCommands menuItemCommands;

        public MenuCommandExecutor(MenuItemCommands menuItemCommands, MenuItem item) {
            this.menuItemCommands = menuItemCommands;
            this.item = item;
        }

        @Override
        public void accept(AppMenu.MenuItem menuItem) {
            AppMenu menu = menuItem.getMenu();
            Frame frame = menu.getFrame();
            if (frame != null) {
                FrameOwner frameOwner = frame.getFrameOwner();
                MenuItemCommand command = menuItemCommands.create(frameOwner, item);
                if (command != null) {
                    command.run();
                }
            }
        }
    }
}
