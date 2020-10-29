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

import io.jmix.core.JmixOrder;
import io.jmix.ui.component.Component;
import io.jmix.ui.sys.UiComponentsImpl;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.LoaderResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

/**
 * Registers external UI components that should be used by the framework.
 * <p>
 * For instance, in the spring {@link Configuration} class create {@link ComponentRegistration} bean.
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
 * <p><br>
 * Note, the order of providing {@link ComponentRegistration} beans is very important
 * because components with the same name will be filtered if they have lower priority.
 * For instance, the configuration provides two {@link ComponentRegistration} with
 * the same name:
 * <pre>
 * &#64;Bean
 * &#64;Order(100)
 * public ComponentRegistration extButton() {
 *     return ComponentRegistrationBuilder.create(ExtButton.NAME)
 *             .withComponentClass(ExtWebButton.class)
 *             .build();
 * }
 * &#64;Bean
 * &#64;Order(200)
 * public ComponentRegistration extButton1() {
 *     return ComponentRegistrationBuilder.create(ExtButton.NAME)
 *             .withComponentClass(ExtWebButton.class)
 *             .withComponentLoaderClass(ExtButtonLoader.class)
 *             .build();
 * }
 * </pre>
 * Component with loader will be filtered as it has a lower priority. Another example,
 * the configuration provides {@link ComponentRegistration} that overrides registration
 * from some add-on. In this case, if the component from the add-on has lower priority
 * it will not be registered at all. It means that our component registration must
 * provide full information: name, tag (if it not the same as name), component class,
 * and loader class.
 *
 * @see ComponentRegistrationBuilder
 */
@org.springframework.stereotype.Component("ui_CustomComponentsRegistry")
public class CustomComponentsRegistry {

    private final Logger log = LoggerFactory.getLogger(CustomComponentsRegistry.class);

    @Autowired(required = false)
    protected List<ComponentRegistration> componentRegistrations;

    @Autowired
    protected UiComponentsImpl uiComponents;
    @Autowired
    protected CustomComponentsLoaderConfig loaderConfig;
    @Autowired
    protected LoaderResolver loaderResolver;

    @EventListener
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
    public void init(ContextRefreshedEvent event) {
        registerComponents();
    }

    protected void registerComponents() {
        if (componentRegistrations == null) {
            return;
        }

        Set<String> registeredSet = new HashSet<>(componentRegistrations.size());

        for (ComponentRegistration registration : componentRegistrations) {
            String componentName = registration.getName();
            if (registeredSet.contains(componentName)) {
                log.debug("Component '{}' with higher priority has already been added to the configuration. " +
                        "Skip: {}.", componentName, registration);
                continue;
            }

            registerComponent(registration);
            registeredSet.add(componentName);
        }
    }

    @SuppressWarnings("rawtypes")
    protected void registerComponent(ComponentRegistration registration) {
        String name = trimToEmpty(registration.getName());
        String tag = trimToEmpty(registration.getTag());
        Class<? extends Component> componentClass = registration.getComponentClass();
        Class<? extends ComponentLoader> componentLoaderClass = registration.getComponentLoaderClass();

        if (componentLoaderClass == null && componentClass == null) {
            throw new IllegalArgumentException(String.format("You have to provide at least component class or" +
                    " componentLoader class for custom component %s / <%s>", name, tag));
        }

        if (componentClass != null) {
            log.trace("Register component {} class {}", name, componentClass.getCanonicalName());

            uiComponents.register(name, componentClass);
        }

        if (componentLoaderClass != null) {
            log.trace("Register tag {} loader {}", tag, componentLoaderClass.getCanonicalName());

            loaderConfig.registerLoader(tag, componentLoaderClass);
        }
    }
}