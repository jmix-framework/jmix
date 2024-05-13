/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.component.composite.impl;

import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.Shortcuts;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.composite.CompositeActions;
import io.jmix.flowui.component.composite.CompositeComponent;
import io.jmix.flowui.component.composite.CompositeComponentAction;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static io.jmix.flowui.kit.component.ComponentUtils.findActionIndexById;

// TODO: gg, base class with ViewActionsImpl
@Component("flowui_CompositeActions")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CompositeActionsImpl implements CompositeActions {

    protected CompositeComponent<?> compositeComponent;

    protected List<Action> actions = new ArrayList<>();
    protected Map<Action, ShortcutRegistration> actionShortcutBinding;

    public CompositeActionsImpl(CompositeComponent<?> compositeComponent) {
        this.compositeComponent = compositeComponent;
    }

    @Override
    public void addAction(Action action, int index) {
        Preconditions.checkNotNullArgument(action, Action.class.getSimpleName() + " cannot be null");
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

    @Override
    public void removeAction(Action action) {
        Preconditions.checkNotNullArgument(action, Action.class.getSimpleName() + " cannot be null");
        removeActionInternal(action);
    }

    protected void removeActionInternal(Action action) {
        if (actions.remove(action)) {
            detachAction(action);
        }
    }

    @Override
    public Collection<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return getActionInternal(id).orElse(null);
    }

    protected Optional<Action> getActionInternal(String id) {
        return getActions().stream()
                .filter(action ->
                        Objects.equals(action.getId(), id))
                .findFirst();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void attachAction(Action action) {
        addShortcutListenerIfNeeded(action);

        action.addPropertyChangeListener(propertyChangeEvent -> {
            if (Action.PROP_SHORTCUT_COMBINATION.equals(propertyChangeEvent.getPropertyName())) {
                removeShortcutListener(action);
                addShortcutListenerIfNeeded(action);
            }
        });

        if (action instanceof CompositeComponentAction compositeComponentAction) {
            compositeComponentAction.setTarget(compositeComponent);
        }
    }

    protected void addShortcutListenerIfNeeded(Action action) {
        KeyCombination keyCombination = action.getShortcutCombination();
        com.vaadin.flow.component.Component content = compositeComponent.getContent();
        if (keyCombination != null) {
            ShortcutRegistration shortcutRegistration = Shortcuts.addShortcutListener(content,
                    () -> action.actionPerform(compositeComponent),
                    keyCombination.getKey(),
                    keyCombination.getKeyModifiers());

            shortcutRegistration.setResetFocusOnActiveElement(keyCombination.isResetFocusOnActiveElement());

            // Setting shortcut scope to a particular component(s) instead of global
            com.vaadin.flow.component.Component[] listenOnComponents = keyCombination.getListenOnComponents();
            if (listenOnComponents != null) {
                shortcutRegistration.listenOn(listenOnComponents);
            } else {
                shortcutRegistration.listenOn(content);
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void detachAction(Action action) {
        removeShortcutListener(action);

        if (action instanceof CompositeComponentAction compositeComponentAction) {
            compositeComponentAction.setTarget(null);
        }
    }

    protected Map<Action, ShortcutRegistration> getActionShortcutBinding() {
        if (actionShortcutBinding == null) {
            actionShortcutBinding = new HashMap<>();
        }

        return actionShortcutBinding;
    }
}
