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

package io.jmix.flowui.view;

import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;
import java.util.Collection;

public interface ViewActions {

    default void addAction(Action action) {
        addAction(action, getActions().size());
    }

    void addAction(Action action, int index);

    void removeAction(Action action);

    default void removeAction(String id) {
        Action action = getAction(id);
        if (action != null) {
            removeAction(action);
        }
    }

    default void removeAllActions() {
        getActions().forEach(this::removeAction);
    }

    Collection<Action> getActions();

    @Nullable
    Action getAction(String id);
}
