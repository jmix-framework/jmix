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

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Interface to be implemented by classes that represent application user and that wrap the {@link OidcUser}. Such
 * classes delegate some method invocations to the wrapped {@code oidcUser}. Classes implementing this interface will be
 * handled, for example, in {@link io.jmix.oidc.usermapper.BaseOidcUserMapper}.
 */
public interface HasOidcUserDelegate {

    OidcUser getDelegate();

    void setDelegate(OidcUser delegate);
}
