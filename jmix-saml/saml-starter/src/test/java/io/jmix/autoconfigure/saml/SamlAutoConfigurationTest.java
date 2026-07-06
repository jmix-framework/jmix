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

import io.jmix.saml.mapper.user.SamlUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SamlAutoConfigurationTest {

    @Test
    void testUiSecurityNotRegisteredWithoutRelyingPartyRegistration() {
        // An application without a configured relying party has no RelyingPartyRegistrationRepository.
        // The default SAML login security configuration is interactive login and must not apply there,
        // mirroring how Spring Boot gates saml2Login security (jmix-framework/jmix#5383).
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SamlAutoConfiguration.class))
                .withBean(SamlUserMapper.class, () -> mock(SamlUserMapper.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(SamlAutoConfiguration.DefaulSamlVaadinWebSecurity.class);
                });
    }
}
