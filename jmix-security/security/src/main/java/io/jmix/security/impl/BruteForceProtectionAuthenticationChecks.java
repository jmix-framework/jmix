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

package io.jmix.security.impl;

import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.core.security.event.PreAuthenticationCheckEvent;
import io.jmix.security.BruteForceProtection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;

@Component("sec_BruteForceAuthenticationChecks")
public class BruteForceProtectionAuthenticationChecks {
    @Autowired
    private BruteForceProtection bruteForceProtection;
    @Autowired(required = false)
    private HttpServletRequest httpRequest;
    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        if (bruteForceProtection.isProtectionEnabled()) {
            String username = getUsername(event.getAuthentication());
            if (username != null && !isSystem(event.getAuthentication())) {
                bruteForceProtection.registerLoginFailed(username, getIpAddress());
            }
        }
    }

    @EventListener
    public void onPreAuthenticationCheck(PreAuthenticationCheckEvent event) {
        if (bruteForceProtection.isProtectionEnabled()) {
            UserDetails userDetails = event.getUser();
            if (bruteForceProtection.isBlocked(userDetails.getUsername(), getIpAddress())) {
                throw new LockedException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked",
                        "User account is locked"));
            }
        }
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        if (bruteForceProtection.isProtectionEnabled()) {
            String username = getUsername(event.getAuthentication());
            if (username != null && !isSystem(event.getAuthentication())) {
                bruteForceProtection.registerLoginSucceeded(username, getIpAddress());
            }
        }
    }

    @Nullable
    private String getUsername(Authentication authentication) {
        if (authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        } else {
            return null;
        }
    }

    private String getIpAddress() {
        if (httpRequest != null) {
            String xForwardedHeader = httpRequest.getHeader("X-Forwarded-For");
            if (xForwardedHeader == null) {
                return httpRequest.getRemoteAddr();
            } else {
                return xForwardedHeader.split(",")[0];
            }
        }
        return "";
    }

    private boolean isSystem(Authentication authentication) {
        return authentication instanceof SystemAuthenticationToken;
    }
}
