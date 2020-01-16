/*
 * Copyright 2019 Haulmont.
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

import io.jmix.core.impl.logging.LogMdc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Nullable;

/**
 * Helper class to get/set UserSession in the current {@link SecurityContext}.
 */
public class CurrentUserSession {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserSession.class);

    /**
     * Returns current UserSession or null if the current context has no Authentication or if the Authentication
     * is not a UserSession.
     */
    @Nullable
    public static UserSession get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof UserSession) {
            return (UserSession) authentication;
        } else {
            log.trace("Current authentication is not a UserSession: " + authentication.getClass().getName());
            return null;
        }
    }

    /**
     * Sets the UserSession in the current  {@link SecurityContext}.
     */
    public static void set(@Nullable UserSession userSession) {
        if (userSession != null) {
            SecurityContextHolder.getContext().setAuthentication(userSession);
            LogMdc.setup(userSession);
        } else {
            SecurityContextHolder.clearContext();
            LogMdc.setup(null);
        }
    }
}
