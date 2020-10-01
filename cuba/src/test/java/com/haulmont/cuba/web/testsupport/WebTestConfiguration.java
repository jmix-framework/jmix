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

package com.haulmont.cuba.web.testsupport;

import com.haulmont.cuba.CubaConfiguration;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.model.common.UserEntityListener;
import com.haulmont.cuba.core.testsupport.TestEventsListener;
import com.haulmont.cuba.core.testsupport.TestJpqlSortExpressionProvider;
import com.haulmont.cuba.core.testsupport.TestSecureOperations;
import com.haulmont.cuba.core.testsupport.TestUserSessionSource;
import com.haulmont.cuba.web.gui.CubaUiComponents;
import io.jmix.data.persistence.JpqlSortExpressionProvider;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.securitydata.SecurityDataConfiguration;
import io.jmix.securityui.SecurityUiConfiguration;
import io.jmix.securityui.constraint.UiSecureOperations;
import io.jmix.ui.UiComponents;
import io.jmix.uidata.UiDataConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
@Import({
        SecurityConfiguration.class,
        UiDataConfiguration.class,
        CubaConfiguration.class,
        SecurityConfiguration.class,
        SecurityDataConfiguration.class,
        SecurityUiConfiguration.class})
@PropertySource("classpath:/com/haulmont/cuba/core/test-web-app.properties")
public class WebTestConfiguration {

    @Bean(name = UiComponents.NAME)
    UiComponents uiComponents() {
        return new CubaUiComponents();
    }

    @Bean
    protected DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean(name = "test_UserEntityListener")
    UserEntityListener userEntityListener() {
        return new UserEntityListener();
    }

    @Bean(name = UserSessionSource.NAME)
    UserSessionSource userSessionSource() {
        return new TestUserSessionSource();
    }

    @Bean(name = JpqlSortExpressionProvider.NAME)
    JpqlSortExpressionProvider jpqlSortExpressionProvider() {
        return new TestJpqlSortExpressionProvider();
    }

    @Bean
    TestEventsListener testEventsListener() {
        return new TestEventsListener();
    }

    @Bean(name = "sec_SecureOperations")
    public SecureOperations secureOperations() {
        return new TestSecureOperations();
    }

    @Bean(name = "sec_UiSecureOperations")
    public UiSecureOperations uiSecureOperations() {
        return new TestUiSecureOperations();
    }

//    @Bean
//    DataLoadCoordinatorFacetProvider dataLoadCoordinatorFacetProvider() {
//        return new CubaDataLoadCoordinatorFacetProvider(); // this is normally done in CubaAutoConfiguration
//    }
}
