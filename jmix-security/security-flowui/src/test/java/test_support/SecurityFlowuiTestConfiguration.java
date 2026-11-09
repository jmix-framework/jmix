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

import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.VaadinConfigurationProperties;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.testassist.FlowuiServletTestBeans;
import io.jmix.flowuidata.FlowuiDataConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.securitydata.SecurityDataConfiguration;
import io.jmix.securityflowui.SecurityFlowuiConfiguration;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.HsqlMemDataSourceTestConfiguration;
import io.jmix.testsupport.config.JpaMainStoreTestConfiguration;
import io.jmix.testsupport.config.LiquibaseTestConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@PropertySource("classpath:/test_support/test-app.properties")
@Import({CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class, FlowuiConfiguration.class,
        FlowuiDataConfiguration.class,
        SecurityConfiguration.class, SecurityDataConfiguration.class, SecurityFlowuiConfiguration.class,
        CommonCoreTestConfiguration.class, HsqlMemDataSourceTestConfiguration.class,
        JpaMainStoreTestConfiguration.class, LiquibaseTestConfiguration.class, FlowuiServletTestBeans.class})
@JmixModule
public class SecurityFlowuiTestConfiguration {

    @Bean
    UserRepository userRepository() {
        return new TestUserRepository();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    // The beans below are required by PrevVaadinRequestUtil. In an application they come from the Vaadin
    // Spring Boot auto-configuration, which the tests do not run.

    @Bean
    AccessAnnotationChecker accessAnnotationChecker() {
        return new AccessAnnotationChecker();
    }

    @Bean
    VaadinConfigurationProperties vaadinConfigurationProperties() {
        return new VaadinConfigurationProperties();
    }

    @Bean
    ServletRegistrationBean<SpringServlet> springServletRegistration(ApplicationContext applicationContext) {
        return new ServletRegistrationBean<>(new SpringServlet(applicationContext, false), "/*");
    }
}
