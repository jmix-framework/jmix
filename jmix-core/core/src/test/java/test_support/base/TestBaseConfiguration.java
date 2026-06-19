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

package test_support.base;


import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier;
import io.jmix.core.impl.JmixMessageSource;
import io.jmix.core.security.CoreSecurityConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import test_support.TestBean;

@Configuration
@JmixModule(dependsOn = CoreConfiguration.class)
@PropertySource(name = "test_support.base", value = "classpath:/test_support/base/base-module.properties")
public class TestBaseConfiguration {

    @Bean
    public MessageSource messageSource(JmixModules modules, Resources resources) {
        return new JmixMessageSource(modules, resources);
    }

    @Bean
    TestBean testBaseBean() {
        return new TestBaseBean();
    }

    @Bean
    public CacheManager baseCacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    TestBaseFooBean baseFooBean() {
        return new TestBaseFooBean();
    }

    @Bean
    TestBaseBarBean baseBarBean() {
        return new TestBaseBarBean();
    }

    @EnableWebSecurity
    static class TestSecurityConfiguration extends CoreSecurityConfiguration {
    }

    @Bean
    public ClusterApplicationEventChannelSupplier baseClusterApplicationEventChannelSupplier() {
        return new LocalApplicationEventChannelSupplier();
    }
}
