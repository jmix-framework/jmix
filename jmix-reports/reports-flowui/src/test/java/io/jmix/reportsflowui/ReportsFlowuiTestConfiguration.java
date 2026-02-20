/*
 * Copyright 2020 Haulmont.
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

package io.jmix.reportsflowui;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.MessageSourceBasenames;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.reports.ReportsConfiguration;
import io.jmix.reportsflowui.test_support.role.FullAccessRole;
import io.jmix.reportsflowui.test_support.role.TestResourceRole2;
import io.jmix.reportsflowui.test_support.role.TestResourceRole4;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.securitydata.SecurityDataConfiguration;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.HsqlEmbeddedDataSourceTestConfiguration;
import io.jmix.testsupport.config.JpaMainStoreTestConfiguration;
import io.jmix.testsupport.config.LiquibaseTestConfiguration;
import io.jmix.flowui.testassist.FlowuiServletTestBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class,
        SecurityConfiguration.class, SecurityDataConfiguration.class,
        ReportsConfiguration.class, FlowuiConfiguration.class, ReportsFlowuiConfiguration.class,
        CommonCoreTestConfiguration.class, HsqlEmbeddedDataSourceTestConfiguration.class,
        JpaMainStoreTestConfiguration.class, LiquibaseTestConfiguration.class,
        FlowuiServletTestBeans.class})
@PropertySource("classpath:/test_support/test-app.properties")
@MessageSourceBasenames({"test_support/messages"})
@EnableWebSecurity
public class ReportsFlowuiTestConfiguration {

    @Bean
    public ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    @Bean
    public UserRepository userRepository(RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.addUser(User.builder()
                .username("admin")
                .password("{noop}admin")
                .authorities(List.of(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(FullAccessRole.NAME)))
                .build());

        repository.addUser(User.builder()
                .username("with-no-access-user")
                .password("{noop}")
                .roles(TestResourceRole4.CODE)
                .build());

        repository.addUser(User.builder()
                .username("with-access-user")
                .password("{noop}")
                .roles(TestResourceRole2.CODE)
                .build());

        return repository;
    }

    @Bean
    @Primary
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
