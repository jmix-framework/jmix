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

package test_support;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.datatools.DatatoolsConfiguration;
import io.jmix.datatoolsflowui.DatatoolsFlowuiConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.testassist.FlowuiServletTestBeans;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.StandardSecurityConfiguration;
import io.jmix.securitydata.SecurityDataConfiguration;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.HsqlMemDataSourceTestConfiguration;
import io.jmix.testsupport.config.JpaMainStoreTestConfiguration;
import io.jmix.testsupport.config.LiquibaseTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class,
        FlowuiConfiguration.class, DatatoolsConfiguration.class, DatatoolsFlowuiConfiguration.class,
        CommonCoreTestConfiguration.class, HsqlMemDataSourceTestConfiguration.class,
        JpaMainStoreTestConfiguration.class, LiquibaseTestConfiguration.class,
        FlowuiServletTestBeans.class,
        FlowuiTestAssistConfiguration.class, SecurityConfiguration.class, SecurityDataConfiguration.class,
        EntityInspectorUiTestConfiguration.TestStandardSecurityConfiguration.class})
@JmixModule
public class EntityInspectorUiTestConfiguration {

    @Bean
    UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @EnableWebSecurity
    public static class TestStandardSecurityConfiguration extends StandardSecurityConfiguration {
    }
}
