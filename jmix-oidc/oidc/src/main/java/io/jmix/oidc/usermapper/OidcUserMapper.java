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

package io.jmix.oidc.usermapper;

import io.jmix.oidc.user.JmixOidcUser;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Interface is responsible for transforming an instance of {@link OidcUser} (from Spring Security) to {@link
 * JmixOidcUser} which may be used by Jmix application. Authorities mapping is often delegated to the {@link
 * io.jmix.oidc.claimsmapper.ClaimsRolesMapper}.
 *
 * @param <T> type of user object used by Jmix application
 */
@NullMarked
public interface OidcUserMapper<T extends JmixOidcUser> {

    /**
     * Transforms an object with user information to the instance of the user used by Jmix. Method implementations may
     * also perform users synchronization, e.g. to store users in the database.
     *
     * @param oidcUser the object that stores information about the user received from the OpenID Provider
     * @return an instance of Jmix user that may be set into security context
     */
    T toJmixUser(OidcUser oidcUser);
}
