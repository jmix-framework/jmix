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

package io.jmix.oidc.user;

import io.jmix.security.authentication.JmixUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * An interface to be implemented by a user Principal that is registered with an OpenID Connect 1.0 Provider. The
 * interface extends {@link UserDetails} because Jmix framework requires the user principal put to the {@link
 * org.springframework.security.core.context.SecurityContext} to implement it. Implementations of {@link io.jmix.oidc.usermapper.OidcUserMapper}
 * must return instances of this interface.
 *
 * @see io.jmix.oidc.usermapper.OidcUserMapper
 */
public interface JmixOidcUser extends OidcUser, JmixUserDetails {
}
