/*
 * Copyright 2020 Haulmont.
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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Interface to be implemented by {@link com.vaadin.ui.Component}s that contain other {@link Component}s.
 */
public interface HasComponents {

    /**
     * Gets component directly owned by this container.
     *
     * @return component or null if not found
     */
    @Nullable
    Component getOwnComponent(String id);

    /**
     * Gets a component belonging to the whole components tree below this container.
     *
     * @return component or null if not found
     */
    @Nullable
    Component getComponent(String id);

    /**
     * Gets a component belonging to the whole components tree below this container.
     *
     * @return a component with the given id
     * @throws IllegalArgumentException if a component with the given id is not found
     */
    default Component getComponentNN(String id) {
        Component component = getComponent(id);
        if (component == null) {
            throw new IllegalArgumentException(String.format("Not found component with id '%s'", id));
        }
        return component;
    }

    /**
     * Gets all components directly owned by this container.
     *
     * @return all components directly owned by this container
     */
    Collection<Component> getOwnComponents();

    /**
     * Gets stream of all components directly owned by this container.
     *
     * @return stream of all components directly owned by this container
     */
    Stream<Component> getOwnComponentsStream();

    /**
     * Gets all components belonging to the whole components tree below this container.
     *
     * @return all components belonging to the whole components tree below this container
     */
    Collection<Component> getComponents();

    /**
     * Focuses the first {@link Component.Focusable} component, if present.
     */
    default void focusFirstComponent() {
        ComponentsHelper.walkComponents(this, component -> {
            if (component instanceof Component.Focusable) {
                ((Component.Focusable) component).focus();
                return true;
            }
            return false;
        });
    }
}
