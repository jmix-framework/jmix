/*
 * Copyright 2022 Haulmont.
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

package io.jmix.core.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

/**
 * Interface that encapsulates functionality for reloading the user returned by {@link CurrentAuthentication} and
 * {@link io.jmix.core.usersubstitution.CurrentUserSubstitution}. The reloading is necessary in order to always return
 * the user entity in the actual state. Also in some cases security context may contain a JPA entity instance where lazy
 * loading is broken. Reloading fixes this problem as well.
 */
public interface CurrentAuthenticationUserLoader {

    /**
     * Returns the reloaded user instance. See supported hints in the {@link CurrentUserHints}.
     */
    UserDetails reloadUser(UserDetails user, Map<String, Object> hints);
}
