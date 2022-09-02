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
import io.jmix.core.impl.CircularBeanReferencesEnabler;
import io.jmix.core.impl.logging.LogMdcFilter;
import io.jmix.core.impl.validation.JmixLocalValidatorFactoryBean;
import io.jmix.core.impl.validation.ValidationClockProvider;
import io.jmix.core.impl.validation.ValidationTraversableResolver;
import io.jmix.core.security.CurrentAuthentication;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.MessageInterpolator;
import java.util.Set;

/**
 * Configuration of the core module.
 */
@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {})
@PropertySource(name = "io.jmix.core", value = "classpath:/io/jmix/core/module.properties")
@EnableCaching
@EnableAspectJAutoProxy
@Import(CoreScheduleConfiguration.class)
public class CoreConfiguration {

    @Bean("core_ModulesProcessor")
    public static JmixModulesProcessor modulesProcessor() {
        return new JmixModulesProcessor();
    }

    @Bean("core_BeanExclusionProcessor")
    public static BeanExclusionProcessor beanExclusionProcessor() {
        return new BeanExclusionProcessor();
    }

    @Bean("core_Modules")
    public JmixModules modules(JmixModulesProcessor processor) {
        return processor.getJmixModules();
    }

    @Bean
    public MeterRegistry simpleMeterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean("core_Validator")
    public static LocalValidatorFactoryBean validator(ValidationClockProvider clockProvider,
                                                      ValidationTraversableResolver traversableResolver,
                                                      MessageInterpolator messageInterpolator) {
        JmixLocalValidatorFactoryBean validatorFactory = new JmixLocalValidatorFactoryBean();

        validatorFactory.setClockProvider(clockProvider);
        validatorFactory.setTraversableResolver(traversableResolver);
        validatorFactory.setJmixMessageInterpolator(messageInterpolator);

        return validatorFactory;
    }

    @Bean("core_CircularBeanReferencesEnabler")
    public static CircularBeanReferencesEnabler circularBeanReferencesEnabler() {
        return new CircularBeanReferencesEnabler();
    }

    @Bean("core_LogMdcFilterRegistrationBean")
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 300)
    public FilterRegistrationBean<LogMdcFilter> logMdcFilterFilterRegistrationBean(CurrentAuthentication currentAuthentication) {
        LogMdcFilter logMdcFilter = new LogMdcFilter(currentAuthentication);
        FilterRegistrationBean<LogMdcFilter> filterRegistration = new FilterRegistrationBean<>(logMdcFilter);
        filterRegistration.setUrlPatterns(Set.of("/*"));
        return filterRegistration;
    }
}
