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
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultJmixSamlUserDetailsTest {

    @Test
    void testEqualsAndHashCodeAreBasedOnUsername() {
        // SessionRegistryImpl groups sessions by principal equals/hashCode; without them the
        // maximum-sessions-per-user limit is silently not enforced for SAML users.
        DefaultJmixSamlUserDetails first = userDetails("john");
        DefaultJmixSamlUserDetails second = userDetails("john");
        DefaultJmixSamlUserDetails other = userDetails("jane");

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first).isNotEqualTo(other);
        assertThat(first).isNotEqualTo(null);
    }

    private DefaultJmixSamlUserDetails userDetails(String username) {
        DefaultJmixSamlUserDetails userDetails = new DefaultJmixSamlUserDetails();
        userDetails.setDelegate(new DefaultSaml2AuthenticatedPrincipal(username, Map.of()));
        return userDetails;
    }
}
