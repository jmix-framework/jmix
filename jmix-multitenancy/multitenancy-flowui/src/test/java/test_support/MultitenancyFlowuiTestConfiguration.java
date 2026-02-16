/*
 * Copyright 2022 Haulmont.
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
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.multitenancy.MultitenancyConfiguration;
import io.jmix.multitenancyflowui.MultitenancyFlowuiConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.testsupport.config.CommonCoreTestBeans;
import io.jmix.testsupport.config.HsqlMemDataSourceTestBeans;
import io.jmix.testsupport.config.JpaMainStoreTestBeans;
import io.jmix.flowui.testassist.FlowuiServletTestBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ComponentScan
@PropertySource("classpath:/test_support/test-app.properties")
@Import({CoreConfiguration.class, DataConfiguration.class, SecurityConfiguration.class,
        FlowuiConfiguration.class, EclipselinkConfiguration.class,
        CommonCoreTestBeans.class, HsqlMemDataSourceTestBeans.class,
        JpaMainStoreTestBeans.class, FlowuiServletTestBeans.class})
@JmixModule(dependsOn = {MultitenancyConfiguration.class, MultitenancyFlowuiConfiguration.class})
@EnableWebSecurity
public class MultitenancyFlowuiTestConfiguration {

    @Bean
    public ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    @Bean(name = "core_UserRepository")
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
