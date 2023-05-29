/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.core.testsupport;

import com.haulmont.cuba.core.global.impl.UserSessionSourceImpl;
import com.haulmont.cuba.security.global.NoUserSessionException;
import com.haulmont.cuba.security.global.UserSession;
import io.jmix.core.session.SessionData;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class TestUserSessionSource extends UserSessionSourceImpl {

    public static final String USER_ID = "60885987-1b61-4247-94c7-dff348347f93";

    private UserSession session;
    private boolean exceptionOnGetUserSession;
    private TestSessionData sessionData = new TestSessionData();

    @Override
    public boolean checkCurrentUserSession() {
        return true;
    }

    public void setExceptionOnGetUserSession(boolean exceptionOnGetUserSession) {
        this.exceptionOnGetUserSession = exceptionOnGetUserSession;
    }

    @Override
    public synchronized UserSession getUserSession() {
        if (exceptionOnGetUserSession) {
            throw new NoUserSessionException(UUID.fromString(USER_ID));
        }
        if (session == null) {
            UserDetails user = User.builder()
                    .username("test_admin")
                    .password("test_admin")
                    .authorities(Collections.emptyList())
                    .build();
            session = new UserSession(
                    new UsernamePasswordAuthenticationToken(user, "test_admin"),
                    sessionData
            );
            session.setUser(user);
            session.setLocale(Locale.ENGLISH);
        }
        return session;
    }

    public void setUserSession(UserSession session) {
        this.session = session;
    }

    static class TestSessionData implements SessionData {

        Map<String, Object> attributes = new HashMap<>();

        @Override
        public Collection<String> getAttributeNames() {
            return attributes.keySet();
        }

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public void setAttribute(String name, Object attribute) {
            attributes.put(name, attribute);
        }

        @Override
        public String getSessionId() {
            return "test-session";
        }
    }
}