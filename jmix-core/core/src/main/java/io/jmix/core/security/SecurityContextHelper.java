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
import org.springframework.security.core.context.SecurityContextHolderStrategy;

import org.springframework.lang.Nullable;

/**
 * Helper class to get/set Authentication in the current {@link SecurityContext}.
 */
public class SecurityContextHelper {

    private static final Logger log = LoggerFactory.getLogger(SecurityContextHelper.class);

    /**
     * Returns current Authentication or null if the current context has no Authentication
     */
    @Nullable
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Sets the Authentication in the current {@link SecurityContext}.
     * <p>
     * Note that this method modifies the current {@link SecurityContext} instance, which may be shared with other
     * threads of the same HTTP session. To execute code on behalf of another user for a limited time,
     * use {@link SystemAuthenticator} instead.
     */
    public static void setAuthentication(@Nullable Authentication authentication) {
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            LogMdc.setup(authentication);
        } else {
            SecurityContextHolder.clearContext();
            LogMdc.setup(null);
        }
    }

    /**
     * Makes the given context current for this thread only, without modifying the previously current context
     * instance. If the installed {@link SecurityContextHolderStrategy} supports {@link ThreadSecurityContextOverride},
     * the context takes precedence over other sources such as the Vaadin session. Also sets up the logging MDC.
     * <p>
     * Must be paired with {@link #restoreContext(SecurityContext)} in a "finally" block:
     * <pre>
     *     SecurityContext previous = SecurityContextHolder.getContext();
     *     SecurityContextHelper.installContext(context);
     *     try {
     *         // ...
     *     } finally {
     *         SecurityContextHelper.restoreContext(previous);
     *     }
     * </pre>
     */
    public static void installContext(SecurityContext context) {
        SecurityContextHolderStrategy strategy = SecurityContextHolder.getContextHolderStrategy();
        if (strategy instanceof ThreadSecurityContextOverride override) {
            override.pushContext(context);
        } else {
            strategy.setContext(context);
        }
        LogMdc.setup(context.getAuthentication());
    }

    /**
     * Reverts the matching {@link #installContext(SecurityContext)} call.
     *
     * @param previous the context that was current before {@code installContext()}, or null to clear the context
     */
    public static void restoreContext(@Nullable SecurityContext previous) {
        SecurityContextHolderStrategy strategy = SecurityContextHolder.getContextHolderStrategy();
        if (strategy instanceof ThreadSecurityContextOverride override) {
            override.popContext();
        } else if (previous != null) {
            strategy.setContext(previous);
        } else {
            strategy.clearContext();
        }
        LogMdc.setup(previous != null ? previous.getAuthentication() : null);
    }
}
