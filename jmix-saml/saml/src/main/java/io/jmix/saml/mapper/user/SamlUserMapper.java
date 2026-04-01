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

package io.jmix.saml.mapper.user;

import io.jmix.saml.user.JmixSamlUserDetails;
import org.opensaml.saml.saml2.core.Assertion;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;

/**
 * Interface is responsible for mapping SAML assertion into {@link JmixSamlUserDetails}.
 * Authorities mapping is often delegated to the {@link io.jmix.saml.mapper.role.SamlAssertionRolesMapper}.
 *
 * @param <T> type of user object used by Jmix application
 */
public interface SamlUserMapper<T extends JmixSamlUserDetails> {

    /**
     * Transforms an assertion into the instance of the user used by Jmix. Method implementations may also perform users
     * synchronization, e.g., to store users in the database.
     *
     * @param assertion SAML assertion
     * @param responseToken the object that stores information about the authentication response from SAML provider
     * @return an instance of Jmix user that may be set into security context
     */
    T toJmixUser(Assertion assertion, OpenSaml5AuthenticationProvider.ResponseToken responseToken);
}
