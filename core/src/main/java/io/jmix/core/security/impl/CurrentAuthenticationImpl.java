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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Component("core_CurrentAuthentication")
public class CurrentAuthenticationImpl implements CurrentAuthentication {

    @Autowired(required = false)
    protected List<AuthenticationResolver> authenticationResolvers;
    @Autowired(required = false)
    protected List<AuthenticationLocaleResolver> localeResolvers;
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
            Locale locale = null;
            if (details instanceof ClientDetails) {
                locale = ((ClientDetails) details).getLocale();
            }
            if (locale == null && localeResolvers != null) {
                locale = localeResolvers.stream()
                        .filter(resolver -> resolver.supports(authentication))
                        .findFirst()
                        .map(resolver -> resolver.getLocale(authentication))
                        .orElse(null);
            }
            return locale == null ? messagesTools.getDefaultLocale() : locale;
        }
        throw new IllegalStateException("Authentication is not set");
    }

    @Override
    public TimeZone getTimeZone() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
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
        throw new IllegalStateException("Authentication is not set");
    }

    @Override
    public boolean isSet() {
        return getAuthentication() != null;
    }
}
