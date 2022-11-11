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
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.MessageTools;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.main.JmixListMenu;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.sys.FlowuiAccessChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component("flowui_ListMenuBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ListMenuBuilder {
    private static final Logger log = LoggerFactory.getLogger(ListMenuBuilder.class);

    protected MenuConfig menuConfig;
    protected ViewRegistry viewRegistry;
    protected UiComponents uiComponents;
    protected MessageTools messageTools;
    protected FlowuiAccessChecker flowuiAccessChecker;

    public ListMenuBuilder(MenuConfig menuConfig,
                           ViewRegistry viewRegistry,
                           UiComponents uiComponents,
                           MessageTools messageTools,
                           FlowuiAccessChecker flowuiAccessChecker) {
        this.menuConfig = menuConfig;
        this.viewRegistry = viewRegistry;
        this.uiComponents = uiComponents;
        this.messageTools = messageTools;
        this.flowuiAccessChecker = flowuiAccessChecker;
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
        } else {
            if (!isPermitted(menuItem)) {
                log.debug("Menu item '{}' is not permitted by access constraint", menuItem.getId());
                return Optional.empty();
            }

            JmixListMenu.MenuItem listMenuItem = createMenuItem(menuItem);

            return Optional.of(listMenuItem);
        }
    }

    protected JmixListMenu.MenuBarItem createMenuBar(MenuItem menuItem) {
        JmixListMenu.MenuBarItem menuBarItem = new JmixListMenu.MenuBarItem(menuItem.getId())
                .withOpened(menuItem.isOpened())
                .withTitle(menuConfig.getItemTitle(menuItem))
                .withDescription(getDescription(menuItem))
                .withClassNames(Arrays.asList(getClassNames(menuItem)));

        if (!Strings.isNullOrEmpty(menuItem.getIcon())) {
            menuBarItem.withIcon(VaadinIcon.valueOf(menuItem.getIcon()));
        }
        return menuBarItem;
    }

    protected JmixListMenu.MenuItem createMenuItem(MenuItem menuItem) {
        JmixListMenu.ViewMenuItem listMenuItem = new JmixListMenu.ViewMenuItem(menuItem.getId())
                .withTitle(menuConfig.getItemTitle(menuItem))
                .withDescription(getDescription(menuItem))
                .withClassNames(Arrays.asList(getClassNames(menuItem)));

        if (!Strings.isNullOrEmpty(menuItem.getIcon())) {
            listMenuItem.withIcon(VaadinIcon.valueOf(menuItem.getIcon()));
        }
        return listMenuItem;
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
        if (Strings.isNullOrEmpty(menuItem.getClassName())) {
            return new String[0];
        }

        return menuItem.getClassName().split(",");
    }

    protected boolean isPermitted(MenuItem menuItem) {
        return flowuiAccessChecker.isMenuPermitted(menuItem);
    }
}
