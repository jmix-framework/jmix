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

package io.jmix.core.security.impl;

import com.google.common.base.Strings;
import io.jmix.core.JmixOrder;
import io.jmix.core.impl.logging.LogMdc;
import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.core.security.SystemAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Transient;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("core_SystemAuthenticator")
public class SystemAuthenticatorImpl extends SystemAuthenticatorSupport implements SystemAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(SystemAuthenticatorImpl.class);

    @Autowired(required = false)
    protected AuthenticationManager authenticationManager;

    @EventListener
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 5)
    protected void beginServerSessionOnStartup(ContextRefreshedEvent event) {
        if (authenticationManager != null) {
            begin();
        }
    }

    @EventListener
    @Order(JmixOrder.LOWEST_PRECEDENCE - 5)
    protected void endServerSessionOnStartup(ContextRefreshedEvent event) {
        if (authenticationManager != null) {
            end();
        }
    }

    @Override
    public Authentication begin(@Nullable String login) {
        if (authenticationManager == null) {
            throw new IllegalStateException("AuthenticationManager is not defined");
        }

        // The previous context is saved as an instance and never modified: it may be shared with other threads
        // of the same HTTP session.
        pushSecurityContext(SecurityContextHolder.getContext());
        try {
            Authentication authentication;

            if (!Strings.isNullOrEmpty(login)) {
                log.trace("Authenticating as {}", login);
                Authentication authToken = new SystemAuthenticationToken(login);
                authentication = authenticationManager.authenticate(authToken);
            } else {
                log.trace("Authenticating as system");
                Authentication authToken = new SystemAuthenticationToken(null);
                authentication = authenticationManager.authenticate(authToken);
            }

            SecurityContextHolder.setContext(new SystemAuthenticatorSecurityContext(authentication));
            LogMdc.setup(authentication);

            return authentication;

        } catch (AuthenticationException e) {
            pollSecurityContext();
            throw e;
        }
    }

    @Override
    public Authentication begin() {
        return begin(null);
    }

    @Override
    public void end() {
        log.trace("Set previous SecurityContext");
        SecurityContext previous = pollSecurityContext();
        if (previous != null) {
            SecurityContextHolder.setContext(previous);
            LogMdc.setup(previous.getAuthentication());
        } else {
            SecurityContextHolder.clearContext();
            LogMdc.setup(null);
        }
    }

    @Override
    public <T> T withUser(@Nullable String login, AuthenticatedOperation<T> operation) {
        begin(login);
        try {
            return operation.call();
        } finally {
            end();
        }
    }

    @Override
    public void runWithUser(@Nullable String login, Runnable operation) {
        begin(login);
        try {
            operation.run();
        } finally {
            end();
        }
    }

    @Override
    public <T> T withSystem(AuthenticatedOperation<T> operation) {
        begin();
        try {
            return operation.call();
        } finally {
            end();
        }
    }

    @Override
    public void runWithSystem(Runnable operation) {
        begin();
        try {
            operation.run();
        } finally {
            end();
        }
    }

    /**
     * Context installed by {@code begin()}. It is marked as {@link Transient}, so Spring Security never stores it
     * in the HTTP session, for example when the response is committed inside a {@code begin()}/{@code end()} block.
     */
    @Transient
    protected static class SystemAuthenticatorSecurityContext extends SecurityContextImpl {

        private static final long serialVersionUID = -3436346475498453405L;

        public SystemAuthenticatorSecurityContext(Authentication authentication) {
            super(authentication);
        }
    }
}
