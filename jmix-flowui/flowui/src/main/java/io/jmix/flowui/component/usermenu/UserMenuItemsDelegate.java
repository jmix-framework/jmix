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

package io.jmix.flowui.component.usermenu;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.action.usermenu.UserMenuAction;
import io.jmix.flowui.kit.component.menubar.JmixSubMenu;
import io.jmix.flowui.kit.component.usermenu.ActionUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenu;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenuItemsDelegate;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;

/**
 * Delegate class for managing {@link UserMenuItem} collection.
 */
@org.springframework.stereotype.Component("flowui_UserMenuItemsDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UserMenuItemsDelegate extends JmixUserMenuItemsDelegate implements HasViewMenuItems {

    protected ViewNavigators viewNavigators;
    protected DialogWindows dialogWindows;

    public UserMenuItemsDelegate(JmixUserMenu<?> userMenu, JmixSubMenu subMenu) {
        super(userMenu, subMenu);
    }

    @Autowired
    public void setViewNavigators(ViewNavigators viewNavigators) {
        this.viewNavigators = viewNavigators;
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Override
    public ViewUserMenuItem addViewItem(String id,
                                        Class<? extends View<?>> viewClass,
                                        String text) {
        return addViewItem(id, viewClass, text, -1);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id,
                                        Class<? extends View<?>> viewClass,
                                        String text, int index) {
        return addViewItemInternal(id, viewClass, text, null, index);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id,
                                        Class<? extends View<?>> viewClass,
                                        String text, Component icon) {
        return addViewItem(id, viewClass, text, icon, -1);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id,
                                        Class<? extends View<?>> viewClass,
                                        String text, Component icon,
                                        int index) {
        return addViewItemInternal(id, viewClass, text, icon, index);
    }

    protected ViewUserMenuItem addViewItemInternal(String id,
                                                   Class<? extends View<?>> viewClass,
                                                   String text, @Nullable Component icon,
                                                   int index) {
        ViewUserMenuItem menuItem = new UserMenu.ViewUserMenuItemImpl(id,
                userMenu,
                createMenuItem(id, new Text(text), index),
                text,
                viewClass,
                viewNavigators,
                dialogWindows);

        if (icon != null) {
            menuItem.setIcon(icon);
        }

        addItemInternal(menuItem, index);

        return menuItem;
    }

    @Override
    public ViewUserMenuItem addViewItem(String id, String viewId, String text) {
        return addViewItem(id, viewId, text, -1);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id, String viewId, String text, int index) {
        return addViewItemInternal(id, viewId, text, null, index);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id, String viewId, String text, Component icon) {
        return addViewItem(id, viewId, text, icon, -1);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id, String viewId, String text, Component icon, int index) {
        return addViewItemInternal(id, viewId, text, icon, index);
    }

    protected ViewUserMenuItem addViewItemInternal(String id,
                                                   String viewId,
                                                   String text, @Nullable Component icon,
                                                   int index) {
        ViewUserMenuItem menuItem = new UserMenu.ViewUserMenuItemImpl(id,
                userMenu,
                createMenuItem(id, new Text(text), index),
                text,
                viewId,
                viewNavigators,
                dialogWindows);

        if (icon != null) {
            menuItem.setIcon(icon);
        }

        addItemInternal(menuItem, index);

        return menuItem;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void attachItem(UserMenuItem item) {
        super.attachItem(item);

        if (item instanceof ActionUserMenuItem actionUserMenuItem) {
            if (actionUserMenuItem.getAction() instanceof UserMenuAction userMenuAction) {
                userMenuAction.setMenuItem(actionUserMenuItem);
                userMenuAction.setTarget(userMenu);
            }
        }
    }

    @Override
    protected void detachItem(UserMenuItem item) {
        super.detachItem(item);

        if (item instanceof ActionUserMenuItem actionUserMenuItem) {
            if (actionUserMenuItem.getAction() instanceof UserMenuAction<?, ?> userMenuAction) {
                userMenuAction.setMenuItem(null);
                userMenuAction.setTarget(null);
            }
        }
    }
}
