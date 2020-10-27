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

package io.jmix.ui.sys.registration;

import io.jmix.ui.component.Component;
import io.jmix.ui.xml.layout.ComponentLoader;

import javax.annotation.Nullable;

/**
 * Registers a UI component in the framework. For instance:
 * <pre>
 * &#64;Configuration
 * public class ComponentConfiguration {
 *
 *     &#64;Bean
 *     public ComponentRegistration extButton() {
 *         return ComponentRegistrationBuilder.create(ExtButton.NAME)
 *                 .withComponentClass(ExtWebButton.class)
 *                 .withComponentLoaderClass(ExtButtonLoader.class)
 *                 .build();
 *     }
 * }
 * </pre>
 *
 * @see ComponentRegistrationBuilder
 * @see CustomComponentsRegistry
 */
public interface ComponentRegistration {

    /**
     * @return component name
     */
    String getName();

    /**
     * @return component name in the screen descriptor. If it was not explicitly set
     * returns the same value as {@link #getName()}.
     */
    String getTag();

    /**
     * @return component class
     */
    @Nullable
    Class<? extends Component> getComponentClass();

    /**
     * @return component loader class
     */
    @Nullable
    Class<? extends ComponentLoader> getComponentLoaderClass();
}
