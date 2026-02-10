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

package io.jmix.flowui.kit.component;

import io.jmix.flowui.kit.action.Action;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Interface to be implemented by UI components containing {@link Action Actions}.
 */
public interface HasActions {

    /**
     * Add an action to the component.
     *
     * @param action action to add
     */
    default void addAction(Action action) {
        addAction(action, getActions().size());
    }

    /**
     * Add an action to the component at the specified index.
     *
     * @param action action to add
     * @param index  index at which the specified action is to be added
     */
    void addAction(Action action, int index);

    /**
     * Removes the action from the component.
     *
     * @param action action to remove
     */
    void removeAction(Action action);

    /**
     * Removes the action with the given id. If there is no action
     * with given id, nothing happens.
     *
     * @param id id of the action to remove
     */
    default void removeAction(String id) {
        Action action = getAction(id);
        if (action != null) {
            removeAction(action);
        }
    }

    /**
     * Remove all actions from the component
     */
    default void removeAllActions() {
        new ArrayList<>(getActions()).forEach(this::removeAction);
    }

    /**
     * @return unmodifiable collection of actions
     */
    Collection<Action> getActions();

    /**
     * Returns an action with passed id.
     *
     * @param id id of the action to find
     * @return an action by its id, or {@code null} if not found
     */
    @Nullable
    Action getAction(String id);
}
