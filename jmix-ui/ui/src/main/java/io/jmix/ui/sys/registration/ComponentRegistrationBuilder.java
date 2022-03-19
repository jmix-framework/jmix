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

/**
 * Builds registration object that is used for adding or overriding UI components in the framework
 * <p>
 * For instance:
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
 * @see ComponentRegistration
 * @see CustomComponentsRegistry
 */
public class ComponentRegistrationBuilder {

    protected String name;
    protected String tag;
    protected Class<? extends Component> componentClass;
    protected Class<? extends ComponentLoader> componentClassLoader;

    /**
     * @param name component name
     */
    public ComponentRegistrationBuilder(String name) {
        this.name = name;
    }

    /**
     * @param name component name
     * @return builder instance
     */
    public static ComponentRegistrationBuilder create(String name) {
        return new ComponentRegistrationBuilder(name);
    }

    /**
     * Sets component name in the screen descriptor. If tag is {@code null} or empty
     * registration object will set tag = name.
     *
     * @param tag element name
     * @return builder instance
     */
    public ComponentRegistrationBuilder withTag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * Sets component class.
     *
     * @param componentClass component class
     * @return builder instance
     */
    public ComponentRegistrationBuilder withComponentClass(Class<? extends Component> componentClass) {
        this.componentClass = componentClass;
        return this;
    }

    /**
     * Sets component loader class.
     *
     * @param componentClassLoader component loader class
     * @return builder instance
     */
    public ComponentRegistrationBuilder withComponentLoaderClass(Class<? extends ComponentLoader> componentClassLoader) {
        this.componentClassLoader = componentClassLoader;
        return this;
    }

    /**
     * @return instance of registration object
     */
    public ComponentRegistration build() {
        return new ComponentRegistrationImpl(name, tag, componentClass, componentClassLoader);
    }
}
