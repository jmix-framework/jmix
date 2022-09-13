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

import com.google.common.base.Strings;
import io.jmix.core.HasTimeZone;
import io.jmix.core.MessageTools;
import io.jmix.core.security.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("core_CurrentAuthentication")
public class CurrentAuthenticationImpl implements CurrentAuthentication {

    @Autowired(required = false)
    private List<AuthenticationResolver> authenticationResolvers;

    @Autowired(required = false)
    private List<AuthenticationLocaleResolver> localeResolvers;

    @Autowired
    private MessageTools messagesTools;

    @Autowired
    private CurrentAuthenticationUserLoader authenticationUserLoader;

    @Override
    public Authentication getAuthentication() {
        Authentication authentication = SecurityContextHelper.getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication is not set. " +
                    "Use SystemAuthenticator in non-user requests like schedulers or asynchronous calls.");
        }
        if (authenticationResolvers != null) {
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
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return authenticationUserLoader.reloadUser((UserDetails) principal, Collections.emptyMap());
        } else {
            throw new RuntimeException("Authentication principal must be UserDetails");
        }
    }

    @Override
    public UserDetails getUser(Map<String, Object> hints) {
        Authentication authentication = getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return authenticationUserLoader.reloadUser((UserDetails) principal, hints);
        } else {
            throw new RuntimeException("Authentication principal must be UserDetails");
        }
    }

    @Override
    public Locale getLocale() {
        Authentication authentication = getAuthentication();
        Object details = authentication.getDetails();
        if (details instanceof ClientDetails) {
            Locale locale = ((ClientDetails) details).getLocale();
            if (locale != null) {
                return locale;
            }
        }

        if (CollectionUtils.isNotEmpty(localeResolvers)) {
            for (AuthenticationLocaleResolver resolver : localeResolvers) {
                if (resolver.supports(authentication)) {
                    Locale resolvedLocale = resolver.getLocale(authentication);
                    if (resolvedLocale != null) {
                        return resolvedLocale;
                    }
                }
            }
        }

        return messagesTools.getDefaultLocale();
    }

    @Override
    public TimeZone getTimeZone() {
        Authentication authentication = getAuthentication();
        Object details = authentication.getDetails();
        Object principal = authentication.getPrincipal();
        TimeZone timeZone = null;
        if (principal instanceof HasTimeZone) {
            String timeZoneId = ((HasTimeZone) principal).getTimeZoneId();
            if (!Strings.isNullOrEmpty(timeZoneId)) {
                timeZone = TimeZone.getTimeZone(timeZoneId);
            }
        } else if (details instanceof ClientDetails) {
            timeZone = ((ClientDetails) details).getTimeZone();
        }
        return timeZone == null ? TimeZone.getDefault() : timeZone;
    }

    @Override
    public boolean isSet() {
        return SecurityContextHelper.getAuthentication() != null;
    }
}
