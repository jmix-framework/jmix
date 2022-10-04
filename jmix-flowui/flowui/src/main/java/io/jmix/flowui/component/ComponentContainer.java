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

public interface ComponentContainer {

    default Optional<Component> findComponent(String id) {
        return UiComponentUtils.findComponent(((Component) this), id);
    }

    default Component getComponent(String id) {
        return findComponent(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("Not found component with id: '%s'", id)));
    }

    Optional<Component> findOwnComponent(String id);

    default Component getOwnComponent(String id) {
        return findOwnComponent(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("Not found own component with id: '%s'", id)));
    }

    Collection<Component> getOwnComponents();

    default Collection<Component> getComponents() {
        return UiComponentUtils.getComponents(((Component) this));
    }
}
