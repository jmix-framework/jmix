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

package io.jmix.tabbedmode.component.tabsheet;

import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.delegate.AbstractActionsHolderSupport;
import io.jmix.tabbedmode.action.tabsheet.TabbedViewsContainerAction;
import io.jmix.tabbedmode.component.tabsheet.contextmenu.MainTabSheetContextMenu;
import io.jmix.tabbedmode.component.tabsheet.contextmenu.MainTabSheetMenuItem;
import io.jmix.tabbedmode.component.tabsheet.contextmenu.MainTabSheetMenuItemActionWrapper;

import java.util.HashMap;
import java.util.Map;

public class MainTabSheetActionsSupport extends AbstractActionsHolderSupport<JmixMainTabSheet> {

    protected Map<Action, MainTabSheetMenuItemActionWrapper> actionBinding = new HashMap<>();

    protected MainTabSheetContextMenu contextMenu;

    public MainTabSheetActionsSupport(JmixMainTabSheet component) {
        super(component);
    }

    @Override
    protected void addActionInternal(Action action, int index) {
        super.addActionInternal(action, index);

        addContextMenuItem(action);
        updateContextMenu();
    }

    protected void addContextMenuItem(Action action) {
        int index = actions.indexOf(action);
        MainTabSheetMenuItemActionWrapper wrapper = createContextMenuItemComponent();
        MainTabSheetMenuItem menuItem = getContextMenu().addItemAtIndex(index, wrapper);

        wrapper.setMenuItem(menuItem);
        wrapper.setAction(action);

        actionBinding.put(action, wrapper);
    }

    protected MainTabSheetMenuItemActionWrapper createContextMenuItemComponent() {
        return new MainTabSheetMenuItemActionWrapper();
    }

    @Override
    protected boolean removeActionInternal(Action action) {
        if (super.removeActionInternal(action)) {
            removeContextMenuItem(action);
            updateContextMenu();

            return true;
        }

        return false;
    }

    protected void removeContextMenuItem(Action action) {
        MainTabSheetMenuItemActionWrapper item = actionBinding.remove(action);
        item.setAction(null);

        getContextMenu().remove(item.getMenuItem());
    }

    @Override
    protected void attachAction(Action action) {
        super.attachAction(action);

        if (action instanceof TabbedViewsContainerAction<?> tabbedViewsContainerAction) {
            tabbedViewsContainerAction.setTarget(component);
        }
    }

    @Override
    protected void detachAction(Action action) {
        super.detachAction(action);

        if (action instanceof TabbedViewsContainerAction<?> tabbedViewsContainerAction) {
            tabbedViewsContainerAction.setTarget(null);
        }
    }

    protected MainTabSheetContextMenu getContextMenu() {
        if (contextMenu == null) {
            initContextMenu();
        }
        return contextMenu;
    }

    protected void initContextMenu() {
        contextMenu = new MainTabSheetContextMenu();
        contextMenu.setTarget(component);
        contextMenu.setVisible(false);
    }

    protected void updateContextMenu() {
        MainTabSheetContextMenu contextMenu = getContextMenu();
        boolean empty = contextMenu.getItems().isEmpty();
        boolean visible = contextMenu.isVisible();

        // empty | visible | result visible
        //  true |    true |   -> false
        //  true |   false | keep false
        // false |    true | keep  true
        // false |   false |   ->  true
        if (empty == visible) {
            contextMenu.setVisible(!visible);
        }
    }
}
