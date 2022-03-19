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
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;

/**
 * Component supporting an action.
 */
public interface ActionOwner {

    /**
     * @return an action or {@code null}
     */
    @Nullable
    Action getAction();

    /**
     * Sets the action to the owner. Action properties override owner properties.
     * <p>
     * List of properties that the action and the owner have and which can be overridden:
     * <ul>
     *     <li>{@code caption}</li>
     *     <li>{@code description}</li>
     *     <li>{@code shortcut}</li>
     *     <li>{@code enabled}</li>
     *     <li>{@code visible}</li>
     *     <li>{@code primary}</li>
     *     <li>{@code icon}</li>
     * </ul>
     *
     * @param action an action
     */
    @StudioProperty(type = PropertyType.COMPONENT_REF, options = "io.jmix.ui.action.Action")
    default void setAction(@Nullable Action action) {
        setAction(action, true);
    }

    /**
     * Sets the action to the owner. If {@code overrideOwnerProperties} is {@code true} then the action properties will
     * override owner properties, otherwise the owner properties will be overridden if they are {@code null}.
     * <p>
     * List of properties that the action and the owner have and which can be overridden:
     * <ul>
     *     <li>{@code caption}</li>
     *     <li>{@code description}</li>
     *     <li>{@code shortcut}</li>
     *     <li>{@code enabled}</li>
     *     <li>{@code visible}</li>
     *     <li>{@code primary}</li>
     *     <li>{@code icon}</li>
     * </ul>
     *
     * @param action                  an action
     * @param overrideOwnerProperties whether action properties override owner properties
     */
    void setAction(@Nullable Action action, boolean overrideOwnerProperties);
}