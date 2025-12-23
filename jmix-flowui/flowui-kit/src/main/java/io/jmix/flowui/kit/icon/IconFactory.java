/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.kit.icon;

import com.vaadin.flow.component.Component;

/**
 * Interface for enumerations representing a factory for creating icons.
 *
 * @param <T> the type of {@link Component} that the factory creates
 */
public interface IconFactory<T extends Component> {

    /**
     * Returns an icon name.
     *
     * @return an icon name, e.g. "OK", "CREATE_ACTION", "CHECK".
     */
    String name();

    /**
     * Creates a new instance of a component representing an icon.
     *
     * @return a new instance of a component representing an icon
     */
    T create();
}
