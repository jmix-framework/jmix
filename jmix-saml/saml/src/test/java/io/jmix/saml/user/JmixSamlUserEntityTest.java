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

package io.jmix.saml.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JmixSamlUserEntityTest {

    @Test
    void testMethodsFallBackToNeutralValuesWithoutDelegate() {
        // The delegate is set only during SAML authentication. The same entity loaded through system
        // authentication, user substitution or UI views must not fail on principal methods.
        TestUser user = new TestUser();
        user.setUsername("john");

        assertThat(user.getName()).isEqualTo("john");
        assertThat(user.getAttributes()).isEmpty();
        assertThat(user.getSessionIndexes()).isEmpty();
        assertThat(user.<Object>getFirstAttribute("mail")).isNull();
        assertThat(user.<Object>getAttribute("mail")).isNull();
        assertThat(user.getRelyingPartyRegistrationId()).isNull();
    }

    @Test
    void testMethodsDelegateWhenDelegateIsSet() {
        DefaultSaml2AuthenticatedPrincipal principal = new DefaultSaml2AuthenticatedPrincipal(
                "john@idp",
                Map.of("mail", List.of("john@example.com")),
                List.of("index-1"));
        principal.setRelyingPartyRegistrationId("okta");

        TestUser user = new TestUser();
        user.setUsername("john");
        user.setDelegate(principal);

        assertThat(user.getName()).isEqualTo("john@idp");
        assertThat(user.<String>getFirstAttribute("mail")).isEqualTo("john@example.com");
        assertThat(user.getSessionIndexes()).containsExactly("index-1");
        assertThat(user.getRelyingPartyRegistrationId()).isEqualTo("okta");
    }

    private static class TestUser extends JmixSamlUserEntity {

        private String username;
        private Collection<? extends GrantedAuthority> authorities = List.of();

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
        }

        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public String getUsername() {
            return username;
        }

        void setUsername(String username) {
            this.username = username;
        }
    }
}
