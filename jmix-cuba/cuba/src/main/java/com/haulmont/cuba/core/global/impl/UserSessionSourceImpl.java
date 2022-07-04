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
import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.core.security.UserRepository;
import io.jmix.core.session.SessionData;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component(UserSessionSource.NAME)
public class UserSessionSourceImpl implements UserSessionSource {

    @Autowired
    protected BeanFactory beanFactory;

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
        UserSession session = new UserSession(authentication, beanFactory.getBean(SessionData.class));
        updateUserSessionFromAuthentication(authentication, session);
        return session;
    }

    protected void updateUserSessionFromAuthentication(Authentication authentication, UserSession session) {
        UserRepository userRepository = beanFactory.getBean(UserRepository.class);
        if (authentication instanceof UsernamePasswordAuthenticationToken
                || authentication instanceof RememberMeAuthenticationToken) {
            session.setUser((UserDetails) authentication.getPrincipal());
            if (authentication.getDetails() instanceof ClientDetails) {
                ClientDetails clientDetails = (ClientDetails) authentication.getDetails();
                session.setLocale(clientDetails.getLocale());
            } else {
                session.setLocale(Locale.getDefault());
            }
        } else if (authentication instanceof AnonymousAuthenticationToken ||
                authentication instanceof SystemAuthenticationToken) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                session.setUser((UserDetails) authentication.getPrincipal());
                session.setLocale(Locale.getDefault());
            } else {
                session.setUser(userRepository.getSystemUser());
                session.setLocale(Locale.getDefault());
            }
        } else if (authentication instanceof OAuth2Authentication) {
            Authentication userAuthentication = ((OAuth2Authentication) authentication).getUserAuthentication();
            if (userAuthentication != authentication) {
                updateUserSessionFromAuthentication(userAuthentication, session);
            }
        } else if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                session.setUser((UserDetails) principal);
                session.setLocale(Locale.getDefault());
            } else {
                throw new RuntimeException("Authentication type is not supported: " + authentication.getClass().getCanonicalName());
            }
        } else {
            //todo MG should null authentication be possible?
            //todo MG what user to return?
            session.setUser(userRepository.getSystemUser());
            session.setLocale(Locale.getDefault());
        }
    }

    @Override
    public UUID currentOrSubstitutedUserId() {
        // todo user substitution
        UserDetails user = getUserSession().getUser();
        return UserIdUtils.hasUserId(user) ? UserIdUtils.getUserId(getUserSession().getUser()) : null;
    }

    @Override
    public Locale getLocale() {
        return getUserSession().getLocale();
    }
}
