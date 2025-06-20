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
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.Shortcuts;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;

import java.util.*;

import static io.jmix.flowui.kit.component.ComponentUtils.findActionIndexById;

/**
 * Abstract base class for managing and binding actions to a component. This class provides
 * support for adding, removing, and managing {@link Action} instances associated with a
 * component, while also handling shortcut registrations and property change listeners for
 * these actions.
 *
 * @param <C> the type of the component that holds actions
 */
public abstract class AbstractActionsHolderSupport<C extends Component> {

    protected final C component;

    protected List<Action> actions = new ArrayList<>();
    protected Map<Action, ShortcutRegistration> actionShortcutBinding;

    public AbstractActionsHolderSupport(C component) {
        this.component = component;
    }

    /**
     * Adds an {@link Action} to the collection of actions at the default position.
     *
     * @param action the action to be added; must not be null
     */
    public void addAction(Action action) {
        addAction(action, actions.size());
    }

    /**
     * Adds an {@link Action} to the collection of actions at the specified position.
     *
     * @param action the action to be added; must not be null
     * @param index  the position at which the specified action is to be inserted;
     *               if the action already exists, it will be moved to the new position
     */
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
            ShortcutRegistration shortcutRegistration = Shortcuts.addShortcutListener(getShortcutLifecycleOwner(),
                    () -> action.actionPerform(component),
                    keyCombination.getKey(),
                    keyCombination.getKeyModifiers());

            shortcutRegistration.setResetFocusOnActiveElement(keyCombination.isResetFocusOnActiveElement());

            // Setting shortcut scope to a particular component(s) instead of global
            Component[] listenOnComponents = keyCombination.getListenOnComponents();
            if (listenOnComponents != null) {
                shortcutRegistration.listenOn(listenOnComponents);
            }

            getActionShortcutBinding().put(action, shortcutRegistration);
        }
    }

    protected Component getShortcutLifecycleOwner() {
        return component instanceof Composite<?> composite
                ? composite.getContent()
                : component;
    }

    protected void removeShortcutListener(Action action) {
        if (getActionShortcutBinding().containsKey(action)) {
            ShortcutRegistration shortcutRegistration = getActionShortcutBinding().remove(action);
            shortcutRegistration.remove();
        }
    }

    /**
     * Removes the specified {@link Action} from the collection of actions.
     *
     * @param action the action to be removed; must not be null
     */
    public void removeAction(Action action) {
        Preconditions.checkNotNull(action, Action.class.getSimpleName() + " cannot be null");

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

    /**
     * Finds an {@link Action} by its identifier.
     *
     * @param id the identifier of the action to retrieve; must not be null
     * @return an {@code Optional} containing the found {@link Action}, or {@code Optional.empty()}
     * if no action with the specified identifier exists
     */
    public Optional<Action> getAction(String id) {
        return getActions().stream()
                .filter(action ->
                        Objects.equals(action.getId(), id))
                .findFirst();
    }

    /**
     * Returns a collection of all actions associated with this instance.
     *
     * @return an unmodifiable collection of {@link Action} objects
     */
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
