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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.function.SerializableRunnable;

// CAUTION: copied from com.vaadin.flow.component.menubar.MenuBarItem [last update Vaadin 24.0.3]
@Tag("vaadin-menu-bar-item")
public class JmixMenuBarItem extends JmixMenuItem {

    public JmixMenuBarItem(ContextMenu contextMenu, SerializableRunnable contentReset) {
        super(contextMenu, contentReset);
    }

    @Override
    public JmixMenuBarSubMenu getSubMenu() {
        return ((JmixMenuBarSubMenu) super.getSubMenu());
    }

    @Override
    protected JmixMenuBarSubMenu createSubMenu() {
        return new JmixMenuBarSubMenu(this, contentReset);
    }
}
