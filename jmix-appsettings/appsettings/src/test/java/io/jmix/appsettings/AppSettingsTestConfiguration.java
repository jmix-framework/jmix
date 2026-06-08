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

package io.jmix.appsettings;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.multitenancy.MultitenancyConfiguration;
import io.jmix.multitenancy.core.TenantProvider;
import io.jmix.security.SecurityConfiguration;
import io.jmix.securitydata.SecurityDataConfiguration;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.HsqlEmbeddedDataSourceTestConfiguration;
import io.jmix.testsupport.config.JpaMainStoreTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class,
        SecurityConfiguration.class, SecurityDataConfiguration.class,
        AppSettingsConfiguration.class, MultitenancyConfiguration.class,
        CommonCoreTestConfiguration.class, HsqlEmbeddedDataSourceTestConfiguration.class,
        JpaMainStoreTestConfiguration.class})
@PropertySource("classpath:/test_support/test-app.properties")
@JmixModule(id = "io.jmix.appsettings.test",
        dependsOn = {AppSettingsConfiguration.class, MultitenancyConfiguration.class,
                SecurityDataConfiguration.class, EclipselinkConfiguration.class})
public class AppSettingsTestConfiguration {

    @Bean(name = "core_UserRepository")
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    @Bean
    public AppSettingsTenantProvider appSettingsTenantProvider(TenantProvider tenantProvider) {
        return tenantProvider::getCurrentUserTenantId;
    }
}
