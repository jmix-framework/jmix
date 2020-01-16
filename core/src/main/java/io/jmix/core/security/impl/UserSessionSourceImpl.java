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
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Locale;
import java.util.UUID;

@Component(UserSessionSource.NAME)
public class UserSessionSourceImpl implements UserSessionSource {

    @Inject
    protected UserSessions userSessions;

    @Override
    public boolean checkCurrentUserSession() {
        UserSession userSession = CurrentUserSession.get();
        if (userSession == null) {
            return false;
        }
        if (userSession.getAuthentication() instanceof SystemAuthenticationToken) {
            return true;
        }
        UserSession session = userSessions.getAndRefresh(userSession.getId());
        return session != null;
    }

    @Override
    public UserSession getUserSession() throws NoUserSessionException {
        UserSession userSession = CurrentUserSession.get();
        if (userSession == null) {
            throw new NoUserSessionException();
        }
        if (userSession.getAuthentication() instanceof SystemAuthenticationToken) {
            return userSession;
        }

        UserSession cachedSession = userSessions.getAndRefresh(userSession.getId());
        if (cachedSession == null) {
            throw new NoUserSessionException(userSession.getId());
        }
        return cachedSession;
    }

    @Override
    public UUID currentOrSubstitutedUserId() {
        // todo user substitution
        return getUserSession().getUser().getId();
    }

    @Override
    public Locale getLocale() {
        return getUserSession().getLocale();
    }
}
