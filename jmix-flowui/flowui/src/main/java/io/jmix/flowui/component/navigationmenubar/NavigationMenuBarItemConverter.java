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

package io.jmix.flowui.component.navigationmenubar;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.MessageTools;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.menu.MenuItemCommand;
import io.jmix.flowui.menu.MenuItemCommands;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Component("flowui_NavigationMenuBarItemConverter")
public class NavigationMenuBarItemConverter {

    private static final Logger log = LoggerFactory.getLogger(NavigationMenuBarItemConverter.class);
    protected static final String GENERATED_SEPARATOR_ID_PREFIX = "separator-";

    protected MenuConfig menuConfig;
    protected ViewRegistry viewRegistry;
    protected MessageTools messageTools;
    protected UiAccessChecker uiAccessChecker;
    protected MenuItemCommands menuItemCommands;

    public NavigationMenuBarItemConverter(MenuConfig menuConfig,
                                          ViewRegistry viewRegistry,
                                          MessageTools messageTools,
                                          UiAccessChecker uiAccessChecker,
                                          MenuItemCommands menuItemCommands) {
        this.menuConfig = menuConfig;
        this.viewRegistry = viewRegistry;
        this.messageTools = messageTools;
        this.uiAccessChecker = uiAccessChecker;
        this.menuItemCommands = menuItemCommands;
    }

    public Optional<NavigationMenuBar.MenuItem> createMenuItemWithChildren(MenuItem menuItemDescriptor) {
        if (menuItemDescriptor.isMenu()) {
            if (menuItemDescriptor.getChildren().isEmpty()) {
                log.warn("Parent menu item '{}' is skipped as it does not have children", menuItemDescriptor.getId());
                return Optional.empty();
            }

            NavigationMenuBar.ParentMenuItem parentMenuItem = createParentMenuItem(menuItemDescriptor);

            for (MenuItem childItem : menuItemDescriptor.getChildren()) {
                createMenuItemWithChildren(childItem)
                        .ifPresent(parentMenuItem::addChildItem);
            }

            if (!parentMenuItem.hasChildren()) {
                log.debug("Parent menu item '{}' is skipped as it does not have children or they are not " +
                        "permitted by access constraint", menuItemDescriptor.getId());
                return Optional.empty();
            }

            return Optional.of(parentMenuItem);
        } else if (menuItemDescriptor.isSeparator()) {
            NavigationMenuBar.MenuItem separatorItem = createSeparatorItem();

            return Optional.of(separatorItem);
        } else {
            NavigationMenuBar.MenuItem menuItem = createMenuItem(menuItemDescriptor);

            return Optional.ofNullable(menuItem);
        }
    }

    protected NavigationMenuBar.ParentMenuItem createParentMenuItem(MenuItem menuItemDescriptor) {
        NavigationMenuBar.ParentMenuItem menuBarItem = new NavigationMenuBar.ParentMenuItem(menuItemDescriptor.getId())
                .withTitle(menuConfig.getItemTitle(menuItemDescriptor))
                .withDescription(getDescription(menuItemDescriptor))
                .withClassNames(getClassNames(menuItemDescriptor));

        if (!Strings.isNullOrEmpty(menuItemDescriptor.getIcon())) {
            menuBarItem.withIcon(VaadinIcon.valueOf(menuItemDescriptor.getIcon()));
        }
        return menuBarItem;
    }

    @Nullable
    protected String getDescription(MenuItem menuItem) {
        String description = menuItem.getDescription();
        if (!Strings.isNullOrEmpty(description)) {
            return messageTools.loadString(description);
        }
        return null;
    }

    protected List<String> getClassNames(MenuItem menuItem) {
        if (Strings.isNullOrEmpty(menuItem.getClassNames())) {
            return Collections.emptyList();
        }
        return Splitter.on(",")
                .trimResults()
                .splitToList(menuItem.getClassNames());
    }

    protected NavigationMenuBar.MenuItem createSeparatorItem() {
        return new NavigationMenuBar.MenuSeparatorItem(generateSeparatorId());
    }

    protected String generateSeparatorId() {
        return GENERATED_SEPARATOR_ID_PREFIX + RandomStringUtils.randomAlphanumeric(8);
    }

    @Nullable
    protected NavigationMenuBar.MenuItem createMenuItem(MenuItem menuItemDescriptor) {
        if (!isPermitted(menuItemDescriptor)) {
            log.debug("Menu item '{}' is not permitted by access constraint", menuItemDescriptor.getId());
            return null;
        }

        if (menuItemDescriptor.getView() != null) {
            return createViewMenuItem(menuItemDescriptor);
        } else if (menuItemDescriptor.getBean() != null && menuItemDescriptor.getBeanMethod() != null) {
            return createBeanMenuItem(menuItemDescriptor);
        }

        throw new IllegalArgumentException("View or bean method must be specified for menu item");
    }

    protected boolean isPermitted(MenuItem menuItemDescriptor) {
        return uiAccessChecker.isMenuPermitted(menuItemDescriptor);
    }

    protected NavigationMenuBar.MenuItem createViewMenuItem(MenuItem menuItemDescriptor) {
        NavigationMenuBar.ViewMenuItem viewMenuItem = new NavigationMenuBar.ViewMenuItem(menuItemDescriptor.getId())
                .withViewClass(getViewClass(menuItemDescriptor))
                .withTitle(menuConfig.getItemTitle(menuItemDescriptor))
                .withDescription(getDescription(menuItemDescriptor))
                .withClassNames(getClassNames(menuItemDescriptor))
                .withUrlQueryParameters(menuItemDescriptor.getUrlQueryParameters())
                .withRouteParameters(menuItemDescriptor.getRouteParameters())
                .withShortcutCombination(menuItemDescriptor.getShortcutCombination());

        if (!Strings.isNullOrEmpty(menuItemDescriptor.getIcon())) {
            viewMenuItem.withIcon(VaadinIcon.valueOf(menuItemDescriptor.getIcon()));
        }

        return viewMenuItem;
    }

    @Nullable
    protected Class<? extends View<?>> getViewClass(MenuItem menuItem) {
        if (Strings.isNullOrEmpty(menuItem.getView())) {
            return null;
        }
        ViewInfo viewInfo = viewRegistry.getViewInfo(menuItem.getView());
        return viewInfo.getControllerClass();
    }

    protected NavigationMenuBar.MenuItem createBeanMenuItem(MenuItem menuItemDescriptor) {
        NavigationMenuBar.MenuItem beanMenuItem = new NavigationMenuBar.MenuItem(menuItemDescriptor.getId())
                .withTitle(menuConfig.getItemTitle(menuItemDescriptor))
                .withDescription(getDescription(menuItemDescriptor))
                .withClassNames(getClassNames(menuItemDescriptor))
                .withShortcutCombination(menuItemDescriptor.getShortcutCombination())
                .withClickHandler(new MenuCommandExecutor(menuItemDescriptor, menuItemCommands));

        if (!Strings.isNullOrEmpty(menuItemDescriptor.getIcon())) {
            beanMenuItem.withIcon(VaadinIcon.valueOf(menuItemDescriptor.getIcon()));
        }

        return beanMenuItem;
    }

    protected static class MenuCommandExecutor implements Consumer<NavigationMenuBar.MenuItem> {

        protected MenuItem item;
        protected MenuItemCommands menuItemCommands;

        public MenuCommandExecutor(MenuItem item, MenuItemCommands menuItemCommands) {
            this.item = item;
            this.menuItemCommands = menuItemCommands;
        }

        @Override
        public void accept(NavigationMenuBar.MenuItem menuItem) {
            NavigationMenuBar menuComponent = menuItem.getMenuComponent();

            if (menuComponent != null) {
                menuComponent.getUI()
                        .ifPresent(this::executeCommand);
            }
        }

        protected void executeCommand(UI ui) {
            MenuItemCommand command = menuItemCommands.create(ui, item);

            if (command != null) {
                command.run();
            }
        }
    }
}
