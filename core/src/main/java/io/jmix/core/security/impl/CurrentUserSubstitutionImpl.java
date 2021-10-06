/*
 * Copyright 2021 Haulmont.
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

package io.jmix.core.security.impl;

import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("sec_CurrentUserSubstitution")
public class CurrentUserSubstitutionImpl implements CurrentUserSubstitution {

    private CurrentAuthentication currentAuthentication;

    public CurrentUserSubstitutionImpl(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Override
    public UserDetails getAuthenticatedUser() {
        return currentAuthentication.getUser();
    }

    @Override
    public UserDetails getSubstitutedUser() {
        Authentication authentication = currentAuthentication.getAuthentication();
        if (authentication != null &&
                SubstitutedUserAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            Object substitutedPrincipal = ((SubstitutedUserAuthenticationToken) authentication).getSubstitutedPrincipal();
            if (substitutedPrincipal instanceof UserDetails) {
                return (UserDetails) substitutedPrincipal;
            } else {
                throw new RuntimeException("Substituted principal must be UserDetails");
            }
        }
        return null;
    }

    @Override
    public UserDetails getEffectiveUser() {
        UserDetails substitutedUser = getSubstitutedUser();
        return substitutedUser != null ? substitutedUser : getAuthenticatedUser();
    }
}