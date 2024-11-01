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

import jakarta.annotation.Nullable;

/**
 * Interface to be implemented by UI components supporting an action.
 */
public interface HasAction {

    /**
     * Sets the action to the component. Action properties override component's properties.
     * <p>
     * List of properties that the action and the component have and which can be overridden:
     * <ul>
     *     <li>{@code caption}</li>
     *     <li>{@code description}</li>
     *     <li>{@code shortcutCombination}</li>
     *     <li>{@code enabled}</li>
     *     <li>{@code visible}</li>
     *     <li>{@code icon}</li>
     *     <li>{@code variant}</li>
     * </ul>
     *
     * @param action an action to set
     */
    default void setAction(@Nullable Action action) {
        setAction(action, true);
    }

    /**
     * Sets the action to the component. If {@code overrideComponentProperties} is
     * {@code true} then the action properties will override component's properties,
     * otherwise the component's properties will be overridden if they are {@code null}.
     * <p>
     * List of properties that the action and the component have and which can be overridden:
     * <ul>
     *     <li>{@code text}</li>
     *     <li>{@code description}</li>
     *     <li>{@code shortcutCombination}</li>
     *     <li>{@code enabled}</li>
     *     <li>{@code visible}</li>
     *     <li>{@code icon}</li>
     *     <li>{@code variant}</li>
     * </ul>
     *
     * @param action                      an action to set
     * @param overrideComponentProperties whether action properties override component properties
     */
    void setAction(@Nullable Action action, boolean overrideComponentProperties);

    /**
     * @return an action or {@code null}
     */
    @Nullable
    Action getAction();
}
