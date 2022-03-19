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

import com.haulmont.cuba.CubaConfiguration;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.model.common.UserEntityListener;
import com.haulmont.cuba.core.testsupport.TestEventsListener;
import com.haulmont.cuba.core.testsupport.TestJpqlSortExpressionProvider;
import com.haulmont.cuba.core.testsupport.TestSecureOperations;
import com.haulmont.cuba.core.testsupport.TestUserSessionSource;
import com.haulmont.cuba.web.testsupport.TestUiSecureOperations;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.Stores;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.data.persistence.JpqlSortExpressionProvider;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.datatools.DatatoolsConfiguration;
import io.jmix.datatoolsui.DatatoolsUiConfiguration;
import io.jmix.dynattr.DynAttrConfiguration;
import io.jmix.dynattrui.DynAttrUiConfiguration;
import io.jmix.localfs.LocalFileStorageConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.StandardSecurityConfiguration;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.securitydata.SecurityDataConfiguration;
import io.jmix.securityui.SecurityUiConfiguration;
import io.jmix.securityui.constraint.UiSecureOperations;
import io.jmix.ui.UiConfiguration;
import io.jmix.uidata.UiDataConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@Import({
        CoreConfiguration.class,
        DataConfiguration.class, EclipselinkConfiguration.class,
        UiConfiguration.class, UiDataConfiguration.class,
        SecurityConfiguration.class, SecurityDataConfiguration.class, SecurityUiConfiguration.class,
        WebTestConfiguration.TestStandardSecurityConfiguration.class,
        DynAttrConfiguration.class, DynAttrUiConfiguration.class,
        LocalFileStorageConfiguration.class,
        DatatoolsConfiguration.class, DatatoolsUiConfiguration.class,
        CubaConfiguration.class
})
@PropertySource("classpath:/com/haulmont/cuba/core/test-web-app.properties")
public class WebTestConfiguration {

    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    protected DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
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

    @Bean(name = "test_UserEntityListener")
    UserEntityListener userEntityListener() {
        return new UserEntityListener();
    }

    @Bean("test_UserSessionSource")
    @Primary
    UserSessionSource userSessionSource() {
        return new TestUserSessionSource();
    }

    @Bean("test_JpqlSortExpressionProvider")
    @Primary
    JpqlSortExpressionProvider jpqlSortExpressionProvider() {
        return new TestJpqlSortExpressionProvider();
    }

    @Bean
    TestEventsListener testEventsListener() {
        return new TestEventsListener();
    }

    @Bean(name = "test_SecureOperations")
    @Primary
    public SecureOperations secureOperations() {
        return new TestSecureOperations();
    }

    @Bean(name = "test_UiSecureOperations")
    @Primary
    public UiSecureOperations uiSecureOperations() {
        return new TestUiSecureOperations();
    }

    @Bean
    public ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

//    @Bean
//    DataLoadCoordinatorFacetProvider dataLoadCoordinatorFacetProvider() {
//        return new CubaDataLoadCoordinatorFacetProvider(); // this is normally done in CubaAutoConfiguration
//    }

    @EnableWebSecurity
    public static class TestStandardSecurityConfiguration extends StandardSecurityConfiguration {
    }
}
