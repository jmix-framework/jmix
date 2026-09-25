/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.sys;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategy;
import io.jmix.core.security.SystemAuthenticator;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;

/**
 * Replaces Vaadin's {@link VaadinAwareSecurityContextHolderStrategy} bean with a {@link JmixSecurityContextHolderStrategy}
 * that delegates to it, and installs the new strategy into {@link SecurityContextHolder}. This lets
 * {@link SystemAuthenticator} override the security context for the current thread only.
 * <p>
 * Vaadin defines its strategy bean in {@code VaadinAwareSecurityContextHolderStrategyConfiguration}, which is imported
 * by {@code VaadinWebSecurity}, and installs it into {@link SecurityContextHolder} when the bean is created.
 * The bean is replaced rather than defined next to Vaadin's one, so that the application context keeps a single
 * {@link SecurityContextHolderStrategy} bean, and Spring Security filters and method security, which take the strategy
 * from the application context, use the same strategy as {@link SecurityContextHolder}.
 */
public class SecurityContextHolderStrategyPostProcessor implements BeanPostProcessor, PriorityOrdered {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof VaadinAwareSecurityContextHolderStrategy vaadinStrategy) {
            SecurityContextHolderStrategy strategy = new JmixSecurityContextHolderStrategy(vaadinStrategy);
            SecurityContextHolder.setContextHolderStrategy(strategy);
            return strategy;
        }
        return bean;
    }

    @Override
    public int getOrder() {
        // Post-processors that are not PriorityOrdered are created after this one is registered, so the strategy
        // bean is replaced even if it is created as their dependency.
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
