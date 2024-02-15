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

package io.jmix.flowui.view.impl;

import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.Shortcuts;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.action.binder.ActionBinder;
import io.jmix.flowui.action.view.ViewAction;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewActions;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

import static io.jmix.flowui.kit.component.ComponentUtils.findActionIndexById;

@Component("flowui_ViewActions")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ViewActionsImpl implements ViewActions {

    protected View<?> view;

    protected List<Action> actions = new ArrayList<>();
    protected Map<Action, ShortcutRegistration> actionShortcutBinding;

    protected ActionBinder<View<?>> actionBinder;

    public ViewActionsImpl(View<?> view) {
        this.view = view;
    }

    /**
     * @deprecated Use {@link ViewActionsImpl#ViewActionsImpl(View)} instead
     */
    @Deprecated(since = "2.2", forRemoval = true)
    public ViewActionsImpl(ActionBinder<View<?>> actionBinder) {
        this(actionBinder.getHolder());
        this.actionBinder = actionBinder;
    }

    @Override
    public void addAction(Action action, int index) {
        Preconditions.checkNotNullArgument(action, Action.class.getSimpleName() + " cannot be null");

        if (actionBinder != null) {
            if (action.getShortcutCombination() != null) {
                actionBinder.createShortcutActionsHolderBinding(action, getView().getContent(), this::shortcutHandler, index);
            } else {
                actionBinder.addAction(action, index);
            }

            attachAction(action);
        } else {
            addActionInternal(action, index);
        }
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

        if (actionBinder != null) {
            actionBinder.removeAction(action);
        } else {
            removeActionInternal(action);
        }
    }

    protected void removeActionInternal(Action action) {
        if (actions.remove(action)) {
            detachAction(action);
        }
    }

    @Override
    public Collection<Action> getActions() {
        return actionBinder != null
                ? actionBinder.getActions()
                : Collections.unmodifiableList(actions);
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return actionBinder != null
                ? actionBinder.getAction(id).orElse(null)
                : getActionInternal(id).orElse(null);
    }

    protected Optional<Action> getActionInternal(String id) {
        return getActions().stream()
                .filter(action ->
                        Objects.equals(action.getId(), id))
                .findFirst();
    }

    protected <C extends com.vaadin.flow.component.Component> ShortcutRegistration shortcutHandler(C viewLayout,
                                                                                                   ShortcutEventListener shortcutEventListener,
                                                                                                   KeyCombination keyCombination) {
        ShortcutRegistration shortcutRegistration = Shortcuts.addShortcutListener(viewLayout, shortcutEventListener,
                        keyCombination.getKey(), keyCombination.getKeyModifiers())
                .listenOn(viewLayout);
        shortcutRegistration.setResetFocusOnActiveElement(keyCombination.isResetFocusOnActiveElement());
        return shortcutRegistration;
    }

    protected View<?> getView() {
        return actionBinder != null ? actionBinder.getHolder() : view;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void attachAction(Action action) {
        addShortcutListenerIfNeeded(action);

        action.addPropertyChangeListener(propertyChangeEvent -> {
            if (Action.PROP_SHORTCUT_COMBINATION.equals(propertyChangeEvent.getPropertyName())) {
                removeShortcutListener(action);
                addShortcutListenerIfNeeded(action);
            }
        });

        if (action instanceof ViewAction) {
            ((ViewAction) action).setTarget(getView());
        }
    }

    protected void addShortcutListenerIfNeeded(Action action) {
        KeyCombination keyCombination = action.getShortcutCombination();
        com.vaadin.flow.component.Component viewLayout = getView().getContent();
        if (keyCombination != null) {
            ShortcutRegistration shortcutRegistration = Shortcuts.addShortcutListener(viewLayout,
                    () -> action.actionPerform(view),
                    keyCombination.getKey(),
                    keyCombination.getKeyModifiers());

            shortcutRegistration.setResetFocusOnActiveElement(keyCombination.isResetFocusOnActiveElement());

            // Setting shortcut scope to a particular component(s) instead of global
            com.vaadin.flow.component.Component[] listenOnComponents = keyCombination.getListenOnComponents();
            if (listenOnComponents != null) {
                shortcutRegistration.listenOn(listenOnComponents);
            } else {
                shortcutRegistration.listenOn(viewLayout);
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

    protected void detachAction(Action action) {
        removeShortcutListener(action);
    }

    protected Map<Action, ShortcutRegistration> getActionShortcutBinding() {
        if (actionShortcutBinding == null) {
            actionShortcutBinding = new HashMap<>();
        }

        return actionShortcutBinding;
    }
}
