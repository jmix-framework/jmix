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
import io.jmix.core.impl.JmixMessageSource;
import io.jmix.core.security.JmixCoreSecurityConfiguration;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
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
@JmixModule(dependsOn = {})
@PropertySource(name = "io.jmix.core", value = "classpath:/io/jmix/core/module.properties")
public class CoreConfiguration {

    @Bean("core_ModulesProcessor")
    public static JmixModulesProcessor modulesProcessor() {
        return new JmixModulesProcessor();
    }

    @Bean("core_Modules")
    public JmixModules modules(JmixModulesProcessor processor) {
        return processor.getJmixModules();
    }

    @Bean
    public MessageSource messageSource(JmixModules modules, Resources resources) {
        return new JmixMessageSource(modules, resources);
    }

    @Bean
    public MeterRegistry simpleMeterRegistry() {
        return new SimpleMeterRegistry();
    }

    @EventListener
    @Order(Events.HIGHEST_CORE_PRECEDENCE + 5)
    public void onApplicationContextRefreshFirst(ContextRefreshedEvent event) {
        AppBeans.setApplicationContext(event.getApplicationContext());
    }
}
