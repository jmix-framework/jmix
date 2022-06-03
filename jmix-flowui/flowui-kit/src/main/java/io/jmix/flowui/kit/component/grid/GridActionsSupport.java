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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import io.jmix.flowui.kit.action.Action;

import java.util.*;

import static io.jmix.flowui.kit.component.FlowUiComponentUtils.findActionIndexById;

public class GridActionsSupport<C extends Grid<T>, T> {

    protected final C component;

    protected List<Action> actions = new ArrayList<>();
    protected Map<Action, GridMenuItemActionWrapper<T>> actionBinding = new HashMap<>();

    protected JmixGridContextMenu<T> contextMenu;

    public GridActionsSupport(C component) {
        this.component = component;
        initContextMenu();
    }

    protected void initContextMenu() {
        contextMenu = new JmixGridContextMenu<>();
        contextMenu.setTarget(component);
        contextMenu.setVisible(false);
    }

    public void addAction(Action action) {
        addAction(action, actions.size());
    }

    public void addAction(Action action, int index) {
        Preconditions.checkNotNull(action, "Action cannot be null");

        addActionInternal(action, index);
    }

    protected void addActionInternal(Action action, int index) {
        int oldIndex = findActionIndexById(actions, action.getId());
        if (oldIndex >= 0) {
            removeActionInternal(actions.get(oldIndex));
            if (index > oldIndex) {
                index--;
            }
        }

        actions.add(index, action);

        if (!Strings.isNullOrEmpty(action.getText())) {
            addContextMenuItem(action, index);
            updateContextMenu();
        }
    }

    protected void addContextMenuItem(Action action, int index) {
        String text = action.getText();

        GridMenuItem<T> menuItem = contextMenu.addItemAtIndex(index, text);

        GridMenuItemActionWrapper<T> wrapper = new GridMenuItemActionWrapper<>(menuItem);
        wrapper.setAction(action);

        actionBinding.put(action, wrapper);
    }

    protected void updateContextMenu() {
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

    public void removeAction(Action action) {
        Preconditions.checkNotNull(action, "Action cannot be null");

        removeActionInternal(action);
    }

    protected void removeActionInternal(Action action) {
        if (actions.remove(action)) {
            removeContextMenuItem(action);
            updateContextMenu();
        }
    }

    protected void removeContextMenuItem(Action action) {
        GridMenuItemActionWrapper<T> item = actionBinding.remove(action);
        item.setAction(null);

        contextMenu.remove(item.getMenuItem());
    }

    public Optional<Action> getAction(String id) {
        return getActions().stream()
                .filter(action ->
                        Objects.equals(action.getId(), id))
                .findFirst();
    }

    public Collection<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }
}
