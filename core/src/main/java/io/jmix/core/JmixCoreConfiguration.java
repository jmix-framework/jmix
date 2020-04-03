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

package io.jmix.core;

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.annotation.JmixProperty;
import io.jmix.core.compatibility.AppContext;
import io.jmix.core.impl.JmixMessageSource;
import io.jmix.core.security.JmixCoreSecurityConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.Order;

/**
 * Configuration of the core module.
 *
 * <p>It implements {@link BeanDefinitionRegistryPostProcessor} with {@link PriorityOrdered} in order to be processed
 * before {@code @Conditional} annotations that depend on {@code @JmixProperty} values.
 */
@Configuration
@Import(JmixCoreSecurityConfiguration.class)
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {}, properties = {
        @JmixProperty(name = "jmix.core.workDir", value = "${user.dir}/.jmix/work"),
        @JmixProperty(name = "jmix.core.confDir", value = "${user.dir}/.jmix/conf")
})
public class JmixCoreConfiguration {

    @Bean("jmix_ModulesProcessor")
    public static JmixModulesProcessor modulesProcessor() {
        return new JmixModulesProcessor();
    }

    @Bean("jmix_Modules")
    public JmixModules modules(JmixModulesProcessor processor) {
        return processor.getJmixModules();
    }

    @Bean
    public MessageSource messageSource(JmixModules modules, Resources resources) {
        return new JmixMessageSource(modules, resources);
    }

    @EventListener
    @Order(Events.HIGHEST_CORE_PRECEDENCE + 10)
    public void onApplicationContextRefreshFirst(ContextRefreshedEvent event) {
        AppContext.Internals.setApplicationContext(event.getApplicationContext());
    }

    @EventListener
    @Order(Events.LOWEST_CORE_PRECEDENCE - 10)
    public void onApplicationContextRefreshLast(ContextRefreshedEvent event) {
        AppContext.Internals.startContext();
    }

    @EventListener
    @Order(Events.HIGHEST_CORE_PRECEDENCE + 10)
    public void onApplicationContextClosedEvent(ContextClosedEvent event) {
        AppContext.Internals.onContextClosed(event.getApplicationContext());
    }
}
