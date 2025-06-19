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

package io.jmix.flowui.component;

import com.vaadin.flow.component.Component;

import java.util.Collection;
import java.util.Optional;

/**
 * An interface for component containers that provides methods for managing and interacting with child components
 * within a container. It defines utility methods to find, retrieve, and list components based on their identifiers
 * or their hierarchical relationship with the container.
 */
public interface ComponentContainer {

    /**
     * Finds a component within the container hierarchy by its unique identifier.
     *
     * @param id the unique identifier of the component to find
     * @return an {@link Optional} containing the found component if it exists,
     * or an empty {@link Optional} if no component with the given identifier is found
     */
    default Optional<Component> findComponent(String id) {
        return UiComponentUtils.findComponent(((Component) this), id);
    }

    /**
     * Returns a child component from the container hierarchy by its unique identifier.
     * If the component is not found, an {@link IllegalArgumentException} is thrown.
     *
     * @param id the unique identifier of the component to retrieve
     * @return the {@link Component} instance associated with the specified identifier
     * @throws IllegalArgumentException if no component with the given identifier is found
     */
    default Component getComponent(String id) {
        return findComponent(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("Not found component with id: '%s'", id)));
    }

    /**
     * Finds a component within immediate children of the container by its unique identifier.
     *
     * @param id the unique identifier of the component to find
     * @return an {@link Optional} containing the found component if it exists within the immediate
     * children of the container, or an empty {@link Optional} if no component with the
     * given identifier is found
     */
    Optional<Component> findOwnComponent(String id);

    /**
     * Returns a child component from the immediate children of the container by its unique identifier.
     * If the component is not found, an {@link IllegalArgumentException} is thrown.
     *
     * @param id the unique identifier of the component to retrieve
     * @return the {@link Component} instance associated with the specified identifier
     * @throws IllegalArgumentException if no component with the given identifier is found
     */
    default Component getOwnComponent(String id) {
        return findOwnComponent(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("Not found own component with id: '%s'", id)));
    }

    /**
     * Returns a collection of components that are direct children (immediate descendants) of the container.
     *
     * @return a collection of {@link Component} instances representing the immediate children
     * of the container, or an empty collection if no such components exist
     */
    Collection<Component> getOwnComponents();

    /**
     * Returns a collection of all components within the container hierarchy.
     *
     * @return a collection of {@link Component} instances representing all components
     * within the container hierarchy, or an empty collection if no components exist
     */
    default Collection<Component> getComponents() {
        return UiComponentUtils.getComponents(((Component) this));
    }
}
