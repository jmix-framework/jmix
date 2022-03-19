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

package io.jmix.ui.component;

import io.jmix.ui.action.Action;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A component containing {@link Action}s.
 */
public interface ActionsHolder extends Component, HasSubParts {
    /**
     * Add an action to the component
     */
    void addAction(Action action);

    /**
     * Add an action to the component with index.
     */
    void addAction(Action action, int index);

    /**
     * Remove the action from the component
     */
    void removeAction(Action action);

    /**
     * Remove the action by its ID. If there is no action with that ID, nothing happens.
     */
    void removeAction(String id);

    /**
     * Remove all actions from the component
     */
    void removeAllActions();

    /**
     * @return unmodifiable collection of actions
     */
    Collection<Action> getActions();

    /**
     * @return an action by its ID, or null if not found
     */
    @Nullable
    Action getAction(String id);

    /**
     * @return an action by its ID
     * @throws IllegalArgumentException if not found
     */
    default Action getActionNN(String id) {
        Action action = getAction(id);
        if (action == null) {
            throw new IllegalStateException("Unable to find action with id " + id);
        }
        return action;
    }

    @Nullable
    @Override
    default Object getSubPart(String name) {
        return getAction(name);
    }
}