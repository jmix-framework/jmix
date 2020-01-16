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

import io.jmix.core.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component(UserSessionManager.NAME)
public class UserSessionManagerImpl implements UserSessionManager {

    private static final Logger log = LoggerFactory.getLogger(UserSessionManagerImpl.class);

    @Inject
    protected AuthenticationManager authenticationManager;

    @Inject
    protected UserSessions userSessions;

    @Inject
    protected UserSessionFactory userSessionFactory;

    @Override
    public UserSession createSession(Authentication authToken) {
        try {
            Authentication authentication = authenticationManager.authenticate(authToken);
            UserSession session = userSessionFactory.create(authentication);
            userSessions.add(session);
            CurrentUserSession.set(session);

            log.info("Created session: {}", session);
            return session;
        } catch (AuthenticationException e) {
            throw new LoginException(e.getMessage(), e);
        }
    }

    @Override
    public void removeSession() {
        UserSession userSession = CurrentUserSession.get();
        if (userSession != null) {
            userSessions.remove(userSession);
            CurrentUserSession.set(null);
            log.info("Removed session: {}", userSession);
        } else {
            log.debug("There is no UserSession in the current thread");
        }
    }
}
