/*
 * Copyright 2020 Haulmont.
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

import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.authentication.CoreAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * An AuthenticationProvider that supports {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}
 * and returns authenticated {@link io.jmix.core.security.authentication.CoreAuthentication}
 */
public class CoreAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        if (!(user instanceof BaseUser)) {
            throw new IllegalArgumentException("UserDetails must be an instance of " + BaseUser.class.getCanonicalName());
        }
        CoreAuthenticationToken result = new CoreAuthenticationToken((BaseUser) user, authentication.getAuthorities());
        Object details = authentication.getDetails();
        if (details instanceof ClientDetails) {
            result.setLocale(((ClientDetails) details).getLocale());
        }
        return result;
    }
}
