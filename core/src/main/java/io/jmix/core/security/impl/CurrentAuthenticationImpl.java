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

package io.jmix.core.security.impl;

import io.jmix.core.MessageTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.security.AuthenticationResolver;
import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.SecurityContextHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Component("core_CurrentAuthentication")
@Internal
public class CurrentAuthenticationImpl implements CurrentAuthentication {

    @Autowired(required = false)
    protected List<AuthenticationResolver> authenticationResolvers;
    @Autowired
    protected MessageTools messagesTools;

    @Nullable
    @Override
    public Authentication getAuthentication() {
        Authentication authentication = SecurityContextHelper.getAuthentication();
        if (authentication != null && authenticationResolvers != null) {
            return authenticationResolvers.stream()
                    .filter(resolver -> resolver.supports(authentication))
                    .findFirst()
                    .map(resolver -> resolver.resolveAuthentication(authentication))
                    .orElse(authentication);
        }
        return authentication;
    }

    @Override
    public UserDetails getUser() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return (UserDetails) principal;
            } else {
                throw new RuntimeException("Authentication principal must be BaseUser");
            }
        }
        throw new IllegalStateException("Authentication is not set");
    }

    @Override
    public Locale getLocale() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            Object details = authentication.getDetails();
            if (details instanceof ClientDetails) {
                return ((ClientDetails) details).getLocale();
            } else {
                return Locale.getDefault();
            }
        }
        throw new IllegalStateException("Authentication is not set");
    }

    @Override
    public TimeZone getTimeZone() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            //todo MG
            return TimeZone.getDefault();
        }
        throw new IllegalStateException("Authentication is not set");
    }

    @Override
    public boolean isSet() {
        return getAuthentication() != null;
    }
}
