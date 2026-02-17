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
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.Stores;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.reports.ReportsConfiguration;
import io.jmix.reportsrest.ReportsRestConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.model.*;
import io.jmix.security.role.ResourceRoleProvider;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import test_support.report.TestReport;
import test_support.role.FullAccessRole;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;

@Configuration
@Import({
        CoreConfiguration.class,
        DataConfiguration.class,
        EclipselinkConfiguration.class,
        SecurityConfiguration.class,
        ReportsConfiguration.class,
        ReportsRestConfiguration.class,
        TestReport.class
})
@PropertySource("classpath:/test_support/test-app.properties")
public class ReportsRestTestConfiguration {

    @Bean
    @Primary
    DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean
    @Primary
    JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    @Primary
    protected LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                          JpaVendorAdapter jpaVendorAdapter,
                                                                          DbmsSpecifics dbmsSpecifics,
                                                                          JmixModules jmixModules,
                                                                          Resources resources) {
        return new JmixEntityManagerFactoryBean(Stores.MAIN, dataSource, jpaVendorAdapter, dbmsSpecifics, jmixModules, resources);
    }

    @Bean
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JmixTransactionManager(Stores.MAIN, entityManagerFactory);
    }

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    public ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    @Bean
    public UserRepository userRepository(RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        InMemoryUserRepository repository = new InMemoryUserRepository() {
            @Override
            @NonNull
            protected UserDetails createSystemUser() {
                return User.builder()
                        .username("system")
                        .password("{noop}")
                        .authorities(List.of(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(FullAccessRole.NAME)))
                        .build();
            }
        };
        repository.addUser(User.builder()
                .username("admin")
                .password("{noop}admin")
                .authorities(List.of(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(FullAccessRole.NAME)))
                .build());
        return repository;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
        return new LocalApplicationEventChannelSupplier();
    }

    @Bean
    ResourceRoleProvider testResourceRoleProvider() {
        ResourceRole role = new ResourceRole();
        role.setCode(FullAccessRole.NAME);
        role.setName(FullAccessRole.NAME);
        role.setScopes(Set.of(SecurityScope.UI, SecurityScope.API));
        role.setResourcePolicies(List.of(
                ResourcePolicy.builder(ResourcePolicyType.ENTITY, "*")
                        .withAction(EntityPolicyAction.ALL.getId())
                        .build(),
                ResourcePolicy.builder(ResourcePolicyType.ENTITY_ATTRIBUTE, "*")
                        .withAction(EntityAttributePolicyAction.MODIFY.getId())
                        .build(),
                ResourcePolicy.builder(ResourcePolicyType.SPECIFIC, "*")
                        .build()
        ));

        return new ResourceRoleProvider() {
            @Override
            @NonNull
            public java.util.Collection<ResourceRole> getAllRoles() {
                return List.of(role);
            }

            @Override
            public ResourceRole findRoleByCode(@NonNull String code) {
                if (FullAccessRole.NAME.equals(code)) {
                    return role;
                }
                return null;
            }

            @Override
            public boolean deleteRole(@NonNull ResourceRole role) {
                return false;
            }
        };
    }
}
