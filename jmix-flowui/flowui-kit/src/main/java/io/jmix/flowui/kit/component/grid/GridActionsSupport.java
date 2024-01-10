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

package io.jmix.flowui.kit.component.grid;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.delegate.AbstractActionsHolderSupport;

import java.util.HashMap;
import java.util.Map;

public class GridActionsSupport<C extends Grid<T>, T> extends AbstractActionsHolderSupport<C> {

    protected Map<Action, GridMenuItemActionWrapper<T>> actionBinding = new HashMap<>();

    protected JmixGridContextMenu<T> contextMenu;
    protected boolean showActionsInContextMenuEnabled = true;

    public GridActionsSupport(C component) {
        super(component);
    }

    @Override
    protected void addActionInternal(Action action, int index) {
        super.addActionInternal(action, index);

        if (showActionsInContextMenuEnabled) {
            addContextMenuItem(action, index);
            updateContextMenu();
        }
    }

    protected void addContextMenuItem(Action action, int index) {
        GridContextMenuItemComponent component = new GridContextMenuItemComponent();

        GridMenuItem<T> menuItem = getContextMenu().addItemAtIndex(index, component);

        GridMenuItemActionWrapper<T> wrapper = new GridMenuItemActionWrapper<>(menuItem, component);
        wrapper.setAction(action);

        actionBinding.put(action, wrapper);
    }

    protected JmixGridContextMenu<T> getContextMenu() {
        if (contextMenu == null) {
            initContextMenu();
        }
        return contextMenu;
    }

    protected void initContextMenu() {
        contextMenu = new JmixGridContextMenu<>();
        contextMenu.setTarget(component);
        contextMenu.setVisible(false);
    }

    protected void updateContextMenu() {
        JmixGridContextMenu<T> contextMenu = getContextMenu();
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

    @Override
    protected boolean removeActionInternal(Action action) {
        if (super.removeActionInternal(action)) {
            if (showActionsInContextMenuEnabled) {
                removeContextMenuItem(action);
                updateContextMenu();
            }

            return true;
        }

        return false;
    }

    protected void removeContextMenuItem(Action action) {
        GridMenuItemActionWrapper<T> item = actionBinding.remove(action);
        item.setAction(null);

        getContextMenu().remove(item.getMenuItem());
    }

    /**
     * @return true if actions are showed in grid context menu
     */
    public boolean isShowActionsInContextMenuEnabled() {
        return showActionsInContextMenuEnabled;
    }

    /**
     * Sets whether to show actions in grid context menu.
     *
     * @param showActionsInContextMenuEnabled true if actions should be showed to context menu, false otherwise
     */
    public void setShowActionsInContextMenuEnabled(boolean showActionsInContextMenuEnabled) {
        this.showActionsInContextMenuEnabled = showActionsInContextMenuEnabled;
    }
}
