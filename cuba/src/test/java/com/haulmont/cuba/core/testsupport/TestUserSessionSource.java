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
import io.jmix.core.security.impl.CoreUser;

import java.util.Locale;
import java.util.UUID;

public class TestUserSessionSource extends UserSessionSourceImpl {

    public static final String USER_ID = "60885987-1b61-4247-94c7-dff348347f93";

    private UserSession session;
    private boolean exceptionOnGetUserSession;

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
            CoreUser user = new CoreUser("test_admin", "test_admin", "Test Administrator");
            session = new UserSession();
            session.setUser(user);
            session.setLocale(Locale.ENGLISH);
        }
        return session;
    }

    public void setUserSession(UserSession session) {
        this.session = session;
    }
}