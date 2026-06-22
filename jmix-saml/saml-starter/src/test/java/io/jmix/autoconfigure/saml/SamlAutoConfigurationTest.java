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

package io.jmix.autoconfigure.saml;

import io.jmix.saml.SamlProperties;
import io.jmix.saml.SamlVaadinWebSecurity;
import io.jmix.saml.mapper.user.SamlUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SamlAutoConfigurationTest {

    @Test
    void testUiSecurityNotRegisteredWithoutRelyingPartyRegistration() {
        // Reproduces jmix-framework/jmix#5373 for SAML: an application without a configured relying party
        // has no RelyingPartyRegistrationRepository. The default SAML login security configuration is
        // interactive login and must not apply there, mirroring how Spring Boot gates saml2Login security.
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SamlAutoConfiguration.class))
                .withBean(SamlUserMapper.class, () -> mock(SamlUserMapper.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(SamlAutoConfiguration.DefaulSamlVaadinWebSecurity.class);
                });
    }

    @Test
    void testSecurityBeanDoesNotRequireFlowuiViewRegistry() {
        // Guards the split of FlowUI-specific request-cache logic out of AbstractFlowuiWebSecurity:
        // SamlVaadinWebSecurity must be creatable without the FlowUI ViewRegistry bean. A
        // RelyingPartyRegistrationRepository is provided because the SAML login configuration legitimately
        // needs one; ViewRegistry is deliberately absent.
        new WebApplicationContextRunner()
                .withBean(SamlUserMapper.class, () -> mock(SamlUserMapper.class))
                .withBean(RelyingPartyRegistrationRepository.class, () -> mock(RelyingPartyRegistrationRepository.class))
                .withBean(SamlProperties.class, () -> mock(SamlProperties.class))
                .withBean(WebProperties.class)
                .withUserConfiguration(TestSamlWebSecurity.class)
                .run(context -> assertThat(context).hasNotFailed());
    }

    /**
     * Mimics {@link SamlAutoConfiguration.DefaulSamlVaadinWebSecurity} without building the Vaadin filter chain:
     * the inherited dependencies are injected regardless of how the chain is configured.
     */
    @EnableWebSecurity
    static class TestSamlWebSecurity extends SamlVaadinWebSecurity {

        @Override
        protected void configureJmixSpecifics(HttpSecurity http) {
        }

        @Override
        protected void configureVaadinSpecifics(HttpSecurity http) {
        }
    }
}
