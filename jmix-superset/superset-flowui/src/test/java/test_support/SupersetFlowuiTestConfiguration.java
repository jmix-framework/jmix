/*
 * Copyright 2024 Haulmont.
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

package test_support;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier;
import io.jmix.core.impl.JmixMessageSource;
import io.jmix.core.security.CoreSecurityConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.testassist.vaadin.TestServletContext;
import io.jmix.superset.SupersetConfiguration;
import io.jmix.superset.SupersetTokenManager;
import io.jmix.superset.client.cookie.SupersetCookieManager;
import io.jmix.supersetflowui.SupersetFlowuiConfiguration;
import jakarta.servlet.ServletContext;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@Import({FlowuiConfiguration.class, CoreConfiguration.class, SupersetFlowuiConfiguration.class,
        SupersetConfiguration.class})
@PropertySource("classpath:/test_support/application-test.properties")
@JmixModule(dependsOn = SupersetFlowuiConfiguration.class)
public class SupersetFlowuiTestConfiguration {

    @Bean
    public MessageSource messageSource(JmixModules modules, Resources resources) {
        return new JmixMessageSource(modules, resources);
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @Primary
    public ServletContext servletContext() {
        return new TestServletContext();
    }

    @Bean
    public ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
        return new LocalApplicationEventChannelSupplier();
    }

    @Bean
    public SupersetCookieManager supersetCookieManager() {
        return new SupersetCookieManager();
    }

    @Bean
    @Primary
    public SupersetTokenManager tokenManager() {
        return new TestSupersetTokenManager();
    }

    @Bean("test_TestDatasetConstraintsProvider")
    public TestDatasetConstraintsProvider testDatasetConstraintsProvider() {
        return new TestDatasetConstraintsProvider();
    }

    @EnableWebSecurity
    protected static class CoreSecurity extends CoreSecurityConfiguration {
    }
}
