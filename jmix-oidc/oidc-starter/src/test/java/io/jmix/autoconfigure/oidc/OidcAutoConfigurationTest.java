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

package io.jmix.autoconfigure.oidc;

import io.jmix.oidc.OidcProperties;
import io.jmix.oidc.OidcVaadinWebSecurity;
import io.jmix.oidc.userinfo.JmixOidcUserService;
import io.jmix.oidc.usermapper.OidcUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OidcAutoConfigurationTest {

    @Test
    void testStartsWithoutFlowuiWhenViewRegistryBeanIsAbsent() {
        // Reproduces jmix-framework/jmix#5373: an application that uses OIDC as an OAuth2 resource
        // server only, without FlowUI starters, has no ViewRegistry bean. The OIDC web security
        // configuration must not depend on FlowUI beans, so creating it must not require a ViewRegistry.
        new WebApplicationContextRunner()
                .withBean(JmixOidcUserService.class, () -> mock(JmixOidcUserService.class))
                .withBean(OidcProperties.class, () -> mock(OidcProperties.class))
                .withBean(ClientRegistrationRepository.class, () -> mock(ClientRegistrationRepository.class))
                .withBean(WebProperties.class)
                .withUserConfiguration(TestOidcWebSecurity.class)
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void testDefaultUiConfigurationCanBeDisabledExplicitly() {
        // The documented workaround keeps working: the property switches the UI configuration off.
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(OidcAutoConfiguration.class))
                .withPropertyValues(
                        "jmix.oidc.use-default-ui-configuration=false",
                        "jmix.oidc.use-default-jwt-configuration=false")
                .withBean(OidcUserMapper.class, () -> mock(OidcUserMapper.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(OidcAutoConfiguration.DefaulOidcVaadinWebSecurity.class);
                });
    }

    /**
     * Mimics {@link OidcAutoConfiguration.DefaulOidcVaadinWebSecurity} without building the Vaadin filter
     * chain: the regression is the inherited {@code @Autowired ViewRegistry}, injected regardless of how
     * the chain is configured.
     */
    @EnableWebSecurity
    static class TestOidcWebSecurity extends OidcVaadinWebSecurity {

        @Override
        protected void configureJmixSpecifics(HttpSecurity http) {
        }

        @Override
        protected void configureVaadinSpecifics(HttpSecurity http) {
        }
    }
}
