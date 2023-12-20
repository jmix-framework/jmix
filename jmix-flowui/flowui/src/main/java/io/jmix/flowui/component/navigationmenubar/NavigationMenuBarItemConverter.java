/*
 * Copyright 2023 Haulmont.
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.MessageTools;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.menu.MenuItemCommand;
import io.jmix.flowui.menu.MenuItemCommands;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    public Optional<NavigationMenuBar.AbstractMenuItem<?>> createMenuItemWithChildren(MenuItem menuItemDescriptor) {
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
            removeLastChildSeparators(parentMenuItem);

            if (!parentMenuItem.hasChildren()) {
                log.debug("Parent menu item '{}' is skipped as it does not have children or they are not " +
                        "permitted by access constraint", menuItemDescriptor.getId());
                return Optional.empty();
            }

            return Optional.of(parentMenuItem);
        } else if (menuItemDescriptor.isSeparator()) {
            NavigationMenuBar.SeparatorMenuItem separatorItem = createSeparatorItem();

            return Optional.of(separatorItem);
        } else {
            NavigationMenuBar.MenuItem menuItem = createMenuItem(menuItemDescriptor);

            return Optional.ofNullable(menuItem);
        }
    }

    /**
     * Removes trailing child separators
     * @param parentMenuItem parent menu item to trim
     */
    protected void removeLastChildSeparators(NavigationMenuBar.ParentMenuItem parentMenuItem) {
        List<NavigationMenuBar.AbstractMenuItem<?>> childItems = new ArrayList<>(parentMenuItem.getChildItems());
        for (int i = childItems.size() - 1; i >= 0; i--) {
            NavigationMenuBar.AbstractMenuItem<?> childItem = childItems.get(i);
            if (childItem.isSeparator()) {
                parentMenuItem.removeChildItem(childItem);
            } else {
                break;
            }
        }
    }

    protected NavigationMenuBar.ParentMenuItem createParentMenuItem(MenuItem menuItemDescriptor) {
        NavigationMenuBar.ParentMenuItem parentMenuItem =
                new NavigationMenuBar.ParentMenuItem(menuItemDescriptor.getId());

        parentMenuItem.setIcon(getIcon(menuItemDescriptor));
        parentMenuItem.setTitle(menuConfig.getItemTitle(menuItemDescriptor));
        parentMenuItem.addClassNames(getClassNames(menuItemDescriptor));
        //noinspection DataFlowIssue
        parentMenuItem.setTooltipText(getDescription(menuItemDescriptor));

        return parentMenuItem;
    }

    @Nullable
    protected Icon getIcon(MenuItem menuItemDescriptor) {
        VaadinIcon vaadinIcon = Strings.isNullOrEmpty(menuItemDescriptor.getIcon())
                ? null
                : VaadinIcon.valueOf(menuItemDescriptor.getIcon());
        return vaadinIcon == null ? null : vaadinIcon.create();
    }

    @Nullable
    protected String getDescription(MenuItem menuItem) {
        String description = menuItem.getDescription();
        if (!Strings.isNullOrEmpty(description)) {
            return messageTools.loadString(description);
        }
        return null;
    }

    protected String[] getClassNames(MenuItem menuItem) {
        if (Strings.isNullOrEmpty(menuItem.getClassNames())) {
            return new String[0];
        }
        return Splitter.on(",")
                .trimResults()
                .splitToStream(menuItem.getClassNames())
                .toArray(String[]::new);
    }

    protected NavigationMenuBar.SeparatorMenuItem createSeparatorItem() {
        return new NavigationMenuBar.SeparatorMenuItem(generateSeparatorId());
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
        NavigationMenuBar.ViewMenuItem viewMenuItem =
                new NavigationMenuBar.ViewMenuItem(menuItemDescriptor.getId(), getViewClass(menuItemDescriptor));

        viewMenuItem.setIcon(getIcon(menuItemDescriptor));
        viewMenuItem.setTitle(menuConfig.getItemTitle(menuItemDescriptor));
        //noinspection DataFlowIssue
        viewMenuItem.setTooltipText(getDescription(menuItemDescriptor));
        viewMenuItem.addClassNames(getClassNames(menuItemDescriptor));
        viewMenuItem.setUrlQueryParameters(menuItemDescriptor.getUrlQueryParameters());
        viewMenuItem.setRouteParameters(menuItemDescriptor.getRouteParameters());
        viewMenuItem.setShortcutCombination(menuItemDescriptor.getShortcutCombination());

        return viewMenuItem;
    }

    protected Class<? extends View<?>> getViewClass(MenuItem menuItem) {
        String viewId = menuItem.getView();
        if (Strings.isNullOrEmpty(viewId)) {
            viewId = menuItem.getId();
        }
        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
        Class<? extends View<?>> viewClass = viewInfo.getControllerClass();
        if (!isSupportedView(viewClass)) {
            throw new IllegalArgumentException("View class '%s' is not supported".formatted(viewClass.getSimpleName()));
        }
        return viewClass;
    }

    protected boolean isSupportedView(Class<?> targetView) {
        return View.class.isAssignableFrom(targetView)
                && targetView.getAnnotation(ViewController.class) != null;
    }

    protected NavigationMenuBar.MenuItem createBeanMenuItem(MenuItem menuItemDescriptor) {
        NavigationMenuBar.MenuItem beanMenuItem = new NavigationMenuBar.MenuItem(menuItemDescriptor.getId());

        beanMenuItem.setIcon(getIcon(menuItemDescriptor));
        beanMenuItem.setTitle(menuConfig.getItemTitle(menuItemDescriptor));
        //noinspection DataFlowIssue
        beanMenuItem.setTooltipText(getDescription(menuItemDescriptor));
        beanMenuItem.addClassNames(getClassNames(menuItemDescriptor));
        beanMenuItem.setShortcutCombination(menuItemDescriptor.getShortcutCombination());
        beanMenuItem.setClickHandler(new MenuCommandExecutor(menuItemDescriptor, menuItemCommands));

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
            NavigationMenuBar menuComponent = menuItem.getMenu();

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
