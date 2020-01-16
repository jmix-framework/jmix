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
import io.jmix.core.Events;
import io.jmix.core.impl.logging.LogMdc;
import io.jmix.core.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;

@Component(Authenticator.NAME)
public class AuthenticatorImpl extends AuthenticatorSupport implements Authenticator {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatorImpl.class);

    @Inject
    protected AuthenticationManager authenticationManager;

    @Inject
    protected UserSessionFactory userSessionFactory;

    @Inject
    protected UserSessions userSessions;

    @Inject
    public AuthenticatorImpl(SystemSessions sessions) {
        super(sessions);
    }

    @EventListener
    @Order(Events.HIGHEST_CORE_PRECEDENCE + 5)
    protected void beginServerSessionOnStartup(ContextRefreshedEvent event) {
        begin();
    }

    @EventListener
    @Order(Events.LOWEST_CORE_PRECEDENCE - 5)
    protected void endServerSessionOnStartup(ContextRefreshedEvent event) {
        end();
    }

    @Override
    public UserSession begin(@Nullable String login) {
        UserSession userSession;

        if (!Strings.isNullOrEmpty(login)) {
            log.trace("Authenticating as {}", login);

            userSession = getFromCacheOrCreate(login, () -> {
                Authentication authToken = new SystemAuthenticationToken(login);
                Authentication authentication = authenticationManager.authenticate(authToken);
                UserSession session = userSessionFactory.create(authentication);
                session.setClientDetails(ClientDetails.builder().info("System authentication").build());
                userSessions.add(session);
                return session;
            });
        } else {
            log.trace("Authenticating as system");
            userSession = userSessionFactory.getSystemSession();
        }

        pushAuthentication(SecurityContextHolder.getContext().getAuthentication());

        CurrentUserSession.set(userSession);

        return userSession;
    }

    @Override
    public UserSession begin() {
        return begin(null);
    }

    @Override
    public void end() {
        log.trace("Set previous Authentication");
        Authentication previous = popAuthentication();
        SecurityContextHolder.getContext().setAuthentication(previous);
        LogMdc.setup(previous);
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
    public <T> T withSystem(AuthenticatedOperation<T> operation) {
        begin();
        try {
            return operation.call();
        } finally {
            end();
        }
    }
}