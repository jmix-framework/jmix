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

package io.jmix.flowui.menu;

import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.MessageTools;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.main.JmixListMenu;
import io.jmix.flowui.kit.component.main.ListMenu;
import io.jmix.flowui.sys.FlowuiAccessChecker;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component("flowui_ListMenuBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ListMenuBuilder {
    private static final Logger log = LoggerFactory.getLogger(ListMenuBuilder.class);
    protected static final String GENERATED_SEPARATOR_ID_PREFIX = "separator-";

    protected MenuConfig menuConfig;
    protected ViewRegistry viewRegistry;
    protected UiComponents uiComponents;
    protected MessageTools messageTools;
    protected FlowuiAccessChecker flowuiAccessChecker;
    protected MenuItemCommands menuItemCommands;

    public ListMenuBuilder(MenuConfig menuConfig,
                           ViewRegistry viewRegistry,
                           UiComponents uiComponents,
                           MessageTools messageTools,
                           FlowuiAccessChecker flowuiAccessChecker,
                           MenuItemCommands menuItemCommands) {
        this.menuConfig = menuConfig;
        this.viewRegistry = viewRegistry;
        this.uiComponents = uiComponents;
        this.messageTools = messageTools;
        this.flowuiAccessChecker = flowuiAccessChecker;
        this.menuItemCommands = menuItemCommands;
    }

    public JmixListMenu build() {
        JmixListMenu listMenu = uiComponents.create(JmixListMenu.class);

        build(listMenu);

        return listMenu;
    }

    public void build(JmixListMenu listMenu) {
        List<MenuItem> rootItems = menuConfig.getRootItems();

        for (MenuItem menuItem : rootItems) {
            createListMenu(menuItem)
                    .ifPresent(listMenu::addMenuItem);
        }
    }

    protected Optional<JmixListMenu.MenuItem> createListMenu(MenuItem menuItem) {
        if (menuItem.isMenu()) {
            if (menuItem.getChildren().isEmpty()) {
                log.warn("Menu bar item '{}' is skipped as it does not have children", menuItem.getId());
                return Optional.empty();
            }

            JmixListMenu.MenuBarItem menuBarItem = createMenuBar(menuItem);

            for (MenuItem item : menuItem.getChildren()) {
                createListMenu(item)
                        .ifPresent(menuBarItem::addChildItem);
            }

            if (!menuBarItem.hasChildren()) {
                log.debug("Menu bar item '{}' is skipped as it does not have children or they are not permitted by " +
                        "access constraint", menuItem.getId());
                return Optional.empty();
            }

            return Optional.of(menuBarItem);
        } else if (menuItem.isSeparator()) {
            JmixListMenu.MenuItem listMenuSeparator = createMenuSeparator();

            return Optional.of(listMenuSeparator);
        } else {
            JmixListMenu.MenuItem listMenuItem = createMenuItem(menuItem);

            return Optional.ofNullable(listMenuItem);
        }
    }

    protected JmixListMenu.MenuBarItem createMenuBar(MenuItem menuItem) {
        JmixListMenu.MenuBarItem menuBarItem = new JmixListMenu.MenuBarItem(menuItem.getId())
                .withOpened(menuItem.isOpened())
                .withTitle(menuConfig.getItemTitle(menuItem))
                .withDescription(getDescription(menuItem))
                .withClassNames(Arrays.stream(getClassNames(menuItem)).collect(Collectors.toList()));

        if (!Strings.isNullOrEmpty(menuItem.getIcon())) {
            menuBarItem.withIcon(VaadinIcon.valueOf(menuItem.getIcon()));
        }
        return menuBarItem;
    }

    @Nullable
    protected JmixListMenu.MenuItem createMenuItem(MenuItem menuItem) {
        if (menuItem.getView() != null) {
            return createViewMenuItem(menuItem);
        } else if (menuItem.getBean() != null && menuItem.getBeanMethod() != null) {
            return createBeanMenuItem(menuItem);
        }

        throw new IllegalStateException("Unknown time of menu item");
    }

    @Nullable
    protected ListMenu.MenuItem createViewMenuItem(MenuItem menuItem) {
        if (!isPermitted(menuItem)) {
            log.debug("Menu item '{}' is not permitted by access constraint", menuItem.getId());
            return null;
        }

        JmixListMenu.ViewMenuItem listMenuItem = new JmixListMenu.ViewMenuItem(menuItem.getId())
                .withControllerClass(getControllerClass(menuItem))
                .withTitle(menuConfig.getItemTitle(menuItem))
                .withDescription(getDescription(menuItem))
                .withClassNames(Arrays.stream(getClassNames(menuItem)).collect(Collectors.toList()))
                .withUrlQueryParameters(menuItem.getUrlQueryParameters())
                .withRouteParameters(menuItem.getRouteParameters())
                .withShortcutCombination(menuItem.getShortcutCombination());

        if (!Strings.isNullOrEmpty(menuItem.getIcon())) {
            listMenuItem.withIcon(VaadinIcon.valueOf(menuItem.getIcon()));
        }

        return listMenuItem;
    }

    protected JmixListMenu.MenuItem createBeanMenuItem(MenuItem menuItem) {
        JmixListMenu.BeanMenuItem beanMenuItem = new JmixListMenu.BeanMenuItem(menuItem.getId())
                .withTitle(menuConfig.getItemTitle(menuItem))
                .withDescription(getDescription(menuItem))
                .withClassNames(Arrays.stream(getClassNames(menuItem)).collect(Collectors.toList()))
                .withShortcutCombination(menuItem.getShortcutCombination());

        if (!Strings.isNullOrEmpty(menuItem.getIcon())) {
            beanMenuItem.withIcon(VaadinIcon.valueOf(menuItem.getIcon()));
        }

        beanMenuItem.withClickHandler(new MenuCommandExecutor(menuItem, menuItemCommands));

        return beanMenuItem;
    }

    protected JmixListMenu.MenuItem createMenuSeparator() {
        return new ListMenu.MenuSeparatorItem(generateSeparatorId());
    }

    @Nullable
    protected Class<? extends View<?>> getControllerClass(MenuItem menuItem) {
        if (Strings.isNullOrEmpty(menuItem.getView())) {
            return null;
        }
        ViewInfo viewInfo = viewRegistry.getViewInfo(menuItem.getView());
        return viewInfo.getControllerClass();
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

        return menuItem.getClassNames().split(",");
    }

    protected boolean isPermitted(MenuItem menuItem) {
        return flowuiAccessChecker.isMenuPermitted(menuItem);
    }

    protected String generateSeparatorId() {
        return GENERATED_SEPARATOR_ID_PREFIX + RandomStringUtils.randomAlphanumeric(8);
    }

    public static class MenuCommandExecutor implements Consumer<ListMenu.MenuItem> {
        protected final MenuItem item;
        protected final MenuItemCommands menuItemCommands;

        public MenuCommandExecutor(MenuItem item, MenuItemCommands menuItemCommands) {
            this.item = item;
            this.menuItemCommands = menuItemCommands;
        }

        @Override
        public void accept(ListMenu.MenuItem menuItem) {
            ListMenu menuComponent = menuItem.getMenuComponent();

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
