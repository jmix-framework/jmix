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

package io.jmix.flowui.sys.registration;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.vaadin.flow.component.Component;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.impl.UiComponentsImpl;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Registers external UI components or component loaders that should be used by the framework.
 * <p>
 * For instance, in the spring {@link Configuration} class create {@link ComponentRegistration} bean.
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
 */
@org.springframework.stereotype.Component("flowui_CustomComponentsRegistry")
public class CustomComponentsRegistry {

    private final Logger log = LoggerFactory.getLogger(CustomComponentsRegistry.class);

    @Autowired(required = false)
    protected List<ComponentRegistration> componentRegistrations;

    @Autowired
    protected UiComponentsImpl uiComponents;
    @Autowired
    protected CustomComponentsLoaderConfig loaderConfig;

    @EventListener
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
    public void init(ContextRefreshedEvent event) {
        registerComponents();
    }

    protected void registerComponents() {
        if (componentRegistrations == null) {
            return;
        }

        for (ComponentRegistration registration : Lists.reverse(componentRegistrations)) {
            registerComponent(registration);
        }
    }

    @SuppressWarnings("rawtypes")
    protected void registerComponent(ComponentRegistration registration) {
        Class<? extends Component> component = registration.getComponent();
        Class<? extends Component> replacedComponent = registration.getReplacedComponent();
        Class<? extends ComponentLoader> componentLoader = registration.getComponentLoader();
        String tag = registration.getTag() != null ? registration.getTag().trim() : null;

        if (componentLoader == null
                && Strings.isNullOrEmpty(tag)
                && replacedComponent == null) {
            throw new IllegalArgumentException(String.format("You have to provide at least overridden component class"
                    + " or tag with componentLoader class for custom component %s / <%s>", component, tag));
        }

        if (replacedComponent != null) {
            log.trace("Register component {} class {}", component, replacedComponent.getCanonicalName());

            uiComponents.register(component, replacedComponent);
        }

        if (componentLoader != null && !Strings.isNullOrEmpty(tag)) {
            log.trace("Register tag {} loader {}", tag, componentLoader.getCanonicalName());

            loaderConfig.registerLoader(tag, componentLoader);
        }
    }
}