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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenuBase;
import com.vaadin.flow.function.SerializableRunnable;
import io.jmix.core.common.util.Preconditions;
import io.jmix.tabbedmode.component.tabsheet.contextmenu.MainTabSheetContextMenu.MainTabSheetContextMenuItemClickEvent;
import org.springframework.lang.Nullable;

public class MainTabSheetSubMenu extends
        SubMenuBase<MainTabSheetContextMenu, MainTabSheetMenuItem, MainTabSheetSubMenu>
        implements HasMainTabSheetMenuItems {

    private final SerializableRunnable contentReset;

    public MainTabSheetSubMenu(MainTabSheetMenuItem parentMenuItem, SerializableRunnable contentReset) {
        super(parentMenuItem);

        Preconditions.checkNotNullArgument(contentReset);
        this.contentReset = contentReset;
    }

    @Override
    public MainTabSheetMenuItem addItem(String text,
                                        @Nullable ComponentEventListener<MainTabSheetContextMenuItemClickEvent> clickListener) {
        Preconditions.checkNotNullArgument(text);

        MainTabSheetMenuItem menuItem = addItem(text);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }

        return menuItem;
    }

    @Override
    public MainTabSheetMenuItem addItem(Component component,
                                        @Nullable ComponentEventListener<MainTabSheetContextMenuItemClickEvent> clickListener) {
        Preconditions.checkNotNullArgument(component);

        MainTabSheetMenuItem menuItem = addItem(component);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }

        return menuItem;
    }

    @Override
    protected MenuManager<MainTabSheetContextMenu, MainTabSheetMenuItem, MainTabSheetSubMenu> createMenuManager() {
        return new MenuManager<>(getParentMenuItem().getContextMenu(), contentReset, MainTabSheetMenuItem::new,
                MainTabSheetMenuItem.class, getParentMenuItem());
    }
}
