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

package io.jmix.tabbedmode.component.tabsheet.contextmenu;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.MenuItemBase;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.tabbedmode.component.tabsheet.contextmenu.MainTabSheetContextMenu.MainTabSheetContextMenuItemClickEvent;

public class MainTabSheetMenuItem
        extends MenuItemBase<MainTabSheetContextMenu, MainTabSheetMenuItem, MainTabSheetSubMenu> {

    protected final SerializableRunnable contentReset;

    public MainTabSheetMenuItem(MainTabSheetContextMenu contextMenu,
                                SerializableRunnable contentReset) {
        super(contextMenu);

        Preconditions.checkNotNullArgument(contentReset);
        this.contentReset = contentReset;
    }

    /**
     * Adds the given click listener for this menu item.
     *
     * @param clickListener the click listener to add
     * @return a handle for removing the listener
     */
    public Registration addMenuItemClickListener(
            ComponentEventListener<MainTabSheetContextMenuItemClickEvent> clickListener) {
        return getElement().addEventListener("click", event ->
                clickListener.onComponentEvent(new MainTabSheetContextMenuItemClickEvent(this, true)));
    }

    @Override
    protected MainTabSheetSubMenu createSubMenu() {
        return new MainTabSheetSubMenu(this, contentReset);
    }
}
