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

package security_filter_registration;

import io.jmix.autoconfigure.core.CoreAutoConfiguration;
import io.jmix.autoconfigure.core.cluster.LocalApplicationEventChannelAutoConfiguration;
import io.jmix.autoconfigure.security.SecurityAutoConfiguration;
import io.jmix.core.security.InMemoryUserRepository;
import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletContextInitializerBeans;
import test_support.TestStandardSecurityConfiguration;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Registration of the Spring Security filter chain in the servlet context. The starter brings
 * {@code spring-boot-security}, whose {@link SecurityFilterAutoConfiguration} registers the chain before the other
 * filters and for all dispatcher types. Without it Spring Boot registers the chain as a plain filter: last, and for
 * {@code REQUEST} dispatches only.
 */
class SecurityFilterRegistrationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    CacheAutoConfiguration.class,
                    CoreAutoConfiguration.class,
                    LocalApplicationEventChannelAutoConfiguration.class,
                    SecurityAutoConfiguration.class,
                    SecurityFilterAutoConfiguration.class))
            .withUserConfiguration(TestStandardSecurityConfiguration.class)
            .withBean(InMemoryUserRepository.class)
            .withAllowBeanDefinitionOverriding(true);

    @Test
    void securityFilterChain_registeredWithDefaultOrderForAllDispatcherTypes() {
        contextRunner.run(context -> {
            DelegatingFilterProxyRegistrationBean registration =
                    context.getBean(DelegatingFilterProxyRegistrationBean.class);

            assertThat(registration.getOrder()).isEqualTo(SecurityFilterProperties.DEFAULT_FILTER_ORDER);
            assertThat(registration.determineDispatcherTypes()).isEqualTo(EnumSet.allOf(DispatcherType.class));
        });
    }

    @Test
    void securityFilterChain_registeredOnceBeforeLogMdcFilter() {
        contextRunner.run(context -> {
            Object securityFilterChain = context.getBean("springSecurityFilterChain");
            List<ServletContextInitializer> initializers =
                    new ArrayList<>(new ServletContextInitializerBeans(context.getBeanFactory()));

            assertThat(initializers)
                    .noneMatch(initializer -> initializer instanceof FilterRegistrationBean<?> registration
                            && registration.getFilter() == securityFilterChain);
            assertThat(initializers.indexOf(context.getBean(DelegatingFilterProxyRegistrationBean.class)))
                    .isNotNegative()
                    .isLessThan(initializers.indexOf(context.getBean("core_LogMdcFilterRegistrationBean")));
        });
    }
}
