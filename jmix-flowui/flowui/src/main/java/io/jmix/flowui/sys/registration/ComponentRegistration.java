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

package io.jmix.flowui.sys.registration;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.xml.layout.ComponentLoader;

import javax.annotation.Nullable;

/**
 * Registers new component loader or override a UI component in the framework. For instance:
 * <pre>
 * &#64;Configuration
 * public class ComponentConfiguration {
 *
 *     &#64;Bean
 *     public ComponentRegistration extJmixButton() {
 *         return ComponentRegistrationBuilder.create(ExtJmixButton.class)
 *                 .replaceComponent(JmixButton.class)
 *                 .withComponentLoader("button", ExtButtonLoader.class)
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
     * @return component class
     */
    Class<? extends Component> getComponent();

    /**
     * @return component name that should be used in the view descriptor or {@code null} if not set
     */
    @Nullable
    String getTag();

    /**
     * @return component class that should be replaced by {@link #getComponent()} or {@code null} if not set
     */
    @Nullable
    <T extends Component> Class<T> getReplacedComponent();

    /**
     * @return component loader class or {@code null} if not set
     */
    @Nullable
    Class<? extends ComponentLoader> getComponentLoader();
}
