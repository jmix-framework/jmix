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

package io.jmix.flowui.kit.component.menubar;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.function.SerializableRunnable;
import io.jmix.flowui.kit.component.contextmenu.JmixMenuManager;

// CAUTION: copied from com.vaadin.flow.component.menubar.MenuBarSubMenu [last update Vaadin 24.2.1]
public class JmixMenuBarSubMenu extends JmixSubMenu {

    public JmixMenuBarSubMenu(JmixMenuItem parentMenuItem, SerializableRunnable contentReset) {
        super(parentMenuItem, contentReset);
    }

    @Override
    protected MenuManager<ContextMenu, MenuItem, SubMenu> createMenuManager() {
        return new JmixMenuManager<>(getParentMenuItem().getContextMenu(),
                contentReset, JmixMenuBarItem::new, MenuItem.class,
                getParentMenuItem());
    }

    @Override
    protected JmixMenuManager<ContextMenu, MenuItem, SubMenu> getMenuManager() {
        return super.getMenuManager();
    }
}
