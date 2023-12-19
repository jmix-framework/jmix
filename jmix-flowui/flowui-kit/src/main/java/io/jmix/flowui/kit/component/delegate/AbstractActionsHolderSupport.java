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

package io.jmix.flowui.kit.component.delegate;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.Shortcuts;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;

import java.util.*;

import static io.jmix.flowui.kit.component.ComponentUtils.findActionIndexById;

public abstract class AbstractActionsHolderSupport<C extends Component> {

    protected final C component;

    protected List<Action> actions = new ArrayList<>();
    protected Map<Action, ShortcutRegistration> actionShortcutBinding;

    public AbstractActionsHolderSupport(C component) {
        this.component = component;
    }

    public void addAction(Action action) {
        addAction(action, actions.size());
    }

    public void addAction(Action action, int index) {
        Preconditions.checkNotNull(action, Action.class.getSimpleName() + " cannot be null");

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
        attachAction(action);
    }

    protected void attachAction(Action action) {
        addShortcutListenerIfNeeded(action);

        action.addPropertyChangeListener(propertyChangeEvent -> {
            if (Action.PROP_SHORTCUT_COMBINATION.equals(propertyChangeEvent.getPropertyName())) {
                removeShortcutListener(action);
                addShortcutListenerIfNeeded(action);
            }
        });
    }

    protected void addShortcutListenerIfNeeded(Action action) {
        KeyCombination keyCombination = action.getShortcutCombination();
        if (keyCombination != null) {
            ShortcutRegistration shortcutRegistration = Shortcuts.addShortcutListener(component,
                    () -> action.actionPerform(component),
                    keyCombination.getKey(),
                    keyCombination.getKeyModifiers());

            // Setting shortcut scope to a particular component(s) instead of global
            Component[] listenOnComponents = keyCombination.getListenOnComponents();
            if (listenOnComponents != null) {
                shortcutRegistration.listenOn(listenOnComponents);
            } else {
                shortcutRegistration.listenOn(component);
            }

            getActionShortcutBinding().put(action, shortcutRegistration);
        }
    }

    protected void removeShortcutListener(Action action) {
        if (getActionShortcutBinding().containsKey(action)) {
            ShortcutRegistration shortcutRegistration = getActionShortcutBinding().remove(action);
            shortcutRegistration.remove();
        }
    }

    public void removeAction(Action action) {
        Preconditions.checkNotNull(action, "Action cannot be null");

        removeActionInternal(action);
    }

    protected boolean removeActionInternal(Action action) {
        if (actions.remove(action)) {
            detachAction(action);

            return true;
        }

        return false;
    }

    protected void detachAction(Action action) {
        removeShortcutListener(action);
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

    protected Map<Action, ShortcutRegistration> getActionShortcutBinding() {
        if (actionShortcutBinding == null) {
            actionShortcutBinding = new HashMap<>();
        }

        return actionShortcutBinding;
    }
}
