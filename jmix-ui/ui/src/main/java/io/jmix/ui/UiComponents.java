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

package io.jmix.ui;

import io.jmix.ui.component.Component;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;

/**
 * Factory to create UI components in client independent manner.
 * <br>
 * An instance of the factory can be injected into screen controllers or obtained through {@link ApplicationContext}.
 */
public interface UiComponents {

    /**
     * Create a component instance by its name.
     *
     * @param name component name. It is usually defined in NAME constant inside the component interface,
     *             e.g. {@link io.jmix.ui.component.Label#NAME}.
     *             It is also usually equal to component's XML name.
     * @return component instance
     */
    <T extends Component> T create(String name);

    /**
     * Create a component instance by its type.
     *
     * @param type component type
     * @return component instance
     */
    <T extends Component> T create(Class<T> type);

    /**
     * Create a component instance by its type.
     *
     * @param type component type reference
     * @return component instance
     * @see io.jmix.ui.component.Label#TYPE_DEFAULT
     * @see io.jmix.ui.component.TextField#TYPE_DEFAULT
     */
    <T extends Component> T create(ParameterizedTypeReference<T> type);

    /**
     * Checks that a component with given name is registered.
     *
     * @param name component name. It is usually defined in NAME constant inside the component interface,
     *             e.g. {@link io.jmix.ui.component.Label#NAME}.
     *             It is also usually equal to component's XML name.
     * @return true if the component is registered
     */
    boolean isComponentRegistered(String name);

    /**
     * Checks that a component with given type is registered.
     *
     * @param type component type reference
     * @return true if the component is registered
     */
    boolean isComponentRegistered(Class<?> type);
}