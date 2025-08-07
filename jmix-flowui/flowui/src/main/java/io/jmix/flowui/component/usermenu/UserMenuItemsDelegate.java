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

import io.jmix.flowui.action.usermenu.UserMenuAction;
import io.jmix.flowui.kit.component.menubar.JmixSubMenu;
import io.jmix.flowui.kit.component.usermenu.ActionUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenu;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenuItemsDelegate;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem;

/**
 * Delegate class for managing {@link UserMenuItem} collection.
 */
public class UserMenuItemsDelegate extends JmixUserMenuItemsDelegate implements HasViewMenuItems {

    public UserMenuItemsDelegate(JmixUserMenu<?> userMenu, JmixSubMenu subMenu) {
        super(userMenu, subMenu);
    }

    @Override
    public ViewUserMenuItem addItem(String id, Class<?> viewClass) {
        return addItem(id, viewClass, -1);
    }

    @Override
    public ViewUserMenuItem addItem(String id, Class<?> viewClass, int index) {
        // TODO: gg, implement
        return null;
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
