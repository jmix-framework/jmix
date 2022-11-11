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

/**
 * Builds registration object that is used for adding new component loader or overriding UI components in the
 * framework.
 * <p>
 * For instance:
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
 * @see ComponentRegistration
 * @see CustomComponentsRegistry
 */
public class ComponentRegistrationBuilder {

    protected Class<? extends Component> component;
    protected String tag;
    protected Class<? extends Component> replacedComponent;
    protected Class<? extends ComponentLoader> componentLoader;

    /**
     * @param component component name
     */
    public ComponentRegistrationBuilder(Class<? extends Component> component) {
        this.component = component;
    }

    /**
     * @param component component class
     * @return builder instance
     */
    public static ComponentRegistrationBuilder create(Class<? extends Component> component) {
        return new ComponentRegistrationBuilder(component);
    }

    /**
     * Sets the component class that should be replaced.
     *
     * @param component component class to replace
     * @return builder instance
     */
    public ComponentRegistrationBuilder replaceComponent(Class<? extends Component> component) {
        replacedComponent = component;
        return this;
    }

    /**
     * Sets component loader class.
     *
     * @param tag             component name in the view descriptor
     * @param componentLoader component loader class
     * @return builder instance
     */
    public ComponentRegistrationBuilder withComponentLoader(String tag,
                                                            Class<? extends ComponentLoader> componentLoader) {
        this.tag = tag;
        this.componentLoader = componentLoader;
        return this;
    }

    /**
     * @return instance of registration object
     */
    public ComponentRegistration build() {
        return new ComponentRegistrationImpl(component, tag, replacedComponent, componentLoader);
    }
}
