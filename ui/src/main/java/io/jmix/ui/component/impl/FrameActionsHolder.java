/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.KeyCombination;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.function.Consumer;

import static io.jmix.ui.component.ComponentsHelper.findActionById;

/**
 * Encapsulates {@link ActionsHolder} functionality for web frames and windows.
 */
public class FrameActionsHolder implements com.vaadin.event.Action.Handler {
    protected List<Action> actionList = new ArrayList<>(4);
    protected BiMap<com.vaadin.event.Action, Action> actions = HashBiMap.create();

    protected Component actionSource;
    protected Consumer<PropertyChangeEvent> actionPropertyChangeListener = this::actionPropertyChanged;

    public FrameActionsHolder(Component actionSource) {
        this.actionSource = actionSource;
    }

    public void addAction(Action action) {
        int index = findActionById(actionList, action.getId());
        if (index < 0) {
            index = actionList.size();
        }

        addAction(action, index);
    }

    public void addAction(Action action, int index) {
        int oldIndex = findActionById(actionList, action.getId());
        if (oldIndex >= 0) {
            removeAction(actionList.get(oldIndex));
            if (index > oldIndex) {
                index--;
            }
        }

        if (action.getShortcutCombination() != null) {
            actions.put(createShortcutAction(action), action);
        }

        actionList.add(index, action);

        action.addPropertyChangeListener(actionPropertyChangeListener);
    }

    protected void actionPropertyChanged(PropertyChangeEvent propertyChangeEvent) {
        if (Action.PROP_SHORTCUT.equals(propertyChangeEvent.getPropertyName())) {
            Action action = (Action) propertyChangeEvent.getSource();

            actions.inverse().remove(action);

            if (action.getShortcutCombination() != null) {
                actions.put(createShortcutAction(action), action);
            }
        }
    }

    @Nullable
    protected com.vaadin.event.ShortcutAction createShortcutAction(Action action) {
        KeyCombination keyCombination = action.getShortcutCombination();
        if (keyCombination != null) {
            return new com.vaadin.event.ShortcutAction(
                    action.getCaption(),
                    keyCombination.getKey().getCode(),
                    KeyCombination.Modifier.codes(keyCombination.getModifiers())
            );
        } else {
            return null;
        }
    }

    public void removeAction(@Nullable Action action) {
        if (actionList.remove(action)) {
            actions.inverse().remove(action);

            if (action != null) {
                action.removePropertyChangeListener(actionPropertyChangeListener);
            }
        }
    }

    public void removeAction(String id) {
        Action action = getAction(id);
        if (action != null) {
            removeAction(action);
        }
    }

    public void removeAllActions() {
        actionList.clear();
        actions.clear();
    }

    public Collection<Action> getActions() {
        return Collections.unmodifiableCollection(actionList);
    }

    @Nullable
    public Action getAction(String id) {
        for (Action action : getActions()) {
            if (Objects.equals(action.getId(), id)) {
                return action;
            }
        }
        return null;
    }

    public com.vaadin.event.Action[] getActionImplementations() {
        List<com.vaadin.event.Action> orderedActions = new ArrayList<>(actionList.size());
        for (Action action : actionList) {
            com.vaadin.event.Action e = actions.inverse().get(action);
            if (e != null) {
                orderedActions.add(e);
            }
        }
        return orderedActions.toArray(new com.vaadin.event.Action[0]);
    }

    @Nullable
    public Action getAction(com.vaadin.event.Action actionImpl) {
        return actions.get(actionImpl);
    }

    @Override
    public com.vaadin.event.Action[] getActions(Object target, Object sender) {
        return getActionImplementations();
    }

    @Override
    public void handleAction(com.vaadin.event.Action actionImpl, Object sender, Object target) {
        Action jmixAction = getAction(actionImpl);
        if (jmixAction != null && jmixAction.isEnabled() && jmixAction.isVisible()) {
            jmixAction.actionPerform(actionSource);
        }
    }
}