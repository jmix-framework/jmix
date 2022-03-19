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

/**
 * Component which can contain other components.
 */
public interface ComponentContainer extends Component, HasComponents {
    /**
     * Adds a component to this container.
     *
     * @param childComponent a component to add
     */
    void add(Component childComponent);

    /**
     * Sequentially adds components to this container.
     *
     * @param childComponents components to add
     */
    default void add(Component... childComponents) {
        for (Component component : childComponents) {
            add(component);
        }
    }

    /**
     * Removes a component from this container.
     *
     * @param childComponent a component to remove
     */
    void remove(Component childComponent);

    /**
     * Sequentially removes components from this container.
     *
     * @param childComponents components to remove
     */
    default void remove(Component... childComponents) {
        for (Component component : childComponents) {
            remove(component);
        }
    }

    /**
     * Removes all components from this container.
     */
    void removeAll();
}