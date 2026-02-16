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

package test_support;

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.repository.EnableJmixDataRepositories;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.StandardSecurityConfiguration;
import io.jmix.testsupport.config.CommonCoreTestBeans;
import io.jmix.testsupport.config.HsqlMemDataSourceTestBeans;
import io.jmix.testsupport.config.JpaMainStoreTestBeans;
import io.jmix.testsupport.config.LiquibaseTestBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@PropertySource("classpath:/test_support/test-app.properties")
@JmixModule(dependsOn = {SecurityConfiguration.class, EclipselinkConfiguration.class})
@Import({SecurityDataTestConfiguration.TestStandardSecurityConfiguration.class,
        CommonCoreTestBeans.class, HsqlMemDataSourceTestBeans.class,
        JpaMainStoreTestBeans.class, LiquibaseTestBeans.class})
@EnableJmixDataRepositories
public class SecurityDataTestConfiguration {

    @Bean
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

    @EnableWebSecurity
    public static class TestStandardSecurityConfiguration extends StandardSecurityConfiguration {
    }
}
