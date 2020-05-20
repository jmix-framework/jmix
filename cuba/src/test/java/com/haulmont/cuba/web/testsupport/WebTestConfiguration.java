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

import com.haulmont.cuba.JmixCubaConfiguration;
import com.haulmont.cuba.core.model.common.UserEntityListener;
import com.haulmont.cuba.core.testsupport.TestEventsListener;
import com.haulmont.cuba.core.testsupport.TestJpqlSortExpressionProvider;
import com.haulmont.cuba.core.testsupport.TestUserSessionSource;
import com.haulmont.cuba.web.gui.CubaUiComponents;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;
import io.jmix.core.metamodel.datatypes.FormatStrings;
import io.jmix.core.metamodel.datatypes.FormatStringsRegistry;
import com.haulmont.cuba.core.global.UserSessionSource;
import io.jmix.data.persistence.JpqlSortExpressionProvider;
import io.jmix.security.JmixSecurityConfiguration;
import io.jmix.ui.UiComponents;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Locale;

@Configuration
@Import({
        JmixSecurityConfiguration.class,
        JmixCubaConfiguration.class})
@PropertySource("classpath:/com/haulmont/cuba/core/test-app.properties")
public class WebTestConfiguration {

    @Inject
    protected FormatStringsRegistry formatStringsRegistry;

    protected VaadinSession vaadinSession;

    @EventListener
    public void init(ContextRefreshedEvent event) {
        // saving session to avoid it be GC'ed
        VaadinSession.setCurrent(vaadinSession = createTestVaadinSession());

        formatStringsRegistry.setFormatStrings(Locale.ENGLISH, new FormatStrings(
                '.', ',',
                "#,##0", "#,##0.###", "#,##0.##",
                "dd/MM/yyyy", "dd/MM/yyyy HH:mm", "dd/MM/yyyy HH:mm Z", "HH:mm", "HH:mm Z",
                "True", "False"));
    }

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

    protected VaadinSession createTestVaadinSession() {
        return new TestVaadinSession(new WebBrowser(), Locale.ENGLISH);
    }
}
