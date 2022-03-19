/*
 * Copyright 2020 Haulmont.
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

package io.jmix.audit.impl;

import io.jmix.audit.UserSessions;
import io.jmix.audit.entity.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component("audit_UserSessions")
public class UserSessionsImpl implements UserSessions {

    @Autowired(required = false)
    protected SessionRegistry sessionRegistry;

    @Override
    public Stream<UserSession> sessions() {
        return sessionRegistry.getAllPrincipals().stream()
                .flatMap(p -> sessionRegistry.getAllSessions(p, false).stream())
                .filter(distinctByKey(SessionInformation::getSessionId))
                .map(UserSession::new);
    }

    @Override
    public Stream<UserSession> sessions(Object principal) {
        return sessionRegistry.getAllSessions(principal, false).stream()
                .map(UserSession::new);
    }

    @Override
    public UserSession get(String id) {
        return new UserSession(sessionRegistry.getSessionInformation(id));
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @Override
    public void invalidate(UserSession session) {
        session.getSessionInformation().expireNow();
    }
}
