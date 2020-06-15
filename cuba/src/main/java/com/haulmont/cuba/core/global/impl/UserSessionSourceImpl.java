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

package com.haulmont.cuba.core.global.impl;

import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.global.NoUserSessionException;
import com.haulmont.cuba.security.global.UserSession;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.authentication.CoreAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component(UserSessionSource.NAME)
public class UserSessionSourceImpl implements UserSessionSource {

    @Autowired
    protected UserRepository userRepository;

    @Override
    public boolean checkCurrentUserSession() {
//        UserSession userSession = CurrentUserSession.get();
//        if (userSession == null) {
//            return false;
//        }
//        if (userSession.getAuthentication() instanceof SystemAuthenticationToken) {
//            return true;
//        }
//        UserSession session = userSessions.getAndRefresh(userSession.getId());
//        return session != null;
        //todo MG
        return true;
    }

    @Override
    public UserSession getUserSession() throws NoUserSessionException {
        Authentication authentication = SecurityContextHelper.getAuthentication();

        UserSession session = new UserSession();
        if (authentication instanceof CoreAuthentication) {
            session.setUser(((CoreAuthentication) authentication).getUser());
            session.setLocale(((CoreAuthentication) authentication).getLocale());
        } else if (authentication instanceof AnonymousAuthenticationToken ||
                authentication instanceof SystemAuthenticationToken) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof BaseUser) {
                session.setUser((BaseUser) authentication.getPrincipal());
                session.setLocale(Locale.getDefault());
            } else {
                session.setUser(userRepository.getSystemUser());
                session.setLocale(Locale.getDefault());
            }
        } else if (authentication == null) {
            //todo MG should null authentication be possible?
            //todo MG what user to return?
            session.setUser(userRepository.getSystemUser());
            session.setLocale(Locale.getDefault());
        } else {
            throw new RuntimeException("Authentication type is not supported: " + authentication.getClass().getCanonicalName());
        }
        return session;
    }

    @Override
    public UUID currentOrSubstitutedUserId() {
        // todo user substitution
        return UUID.fromString(getUserSession().getUser().getKey());
    }

    @Override
    public Locale getLocale() {
        return getUserSession().getLocale();
    }
}
