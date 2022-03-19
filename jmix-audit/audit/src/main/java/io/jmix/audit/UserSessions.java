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

package io.jmix.audit;

import io.jmix.audit.entity.UserSession;

import java.util.stream.Stream;

/**
 * Provides information about current active user sessions
 **/
public interface UserSessions {

    /**
     * Returns all active user sessions
     *
     * @return active user sessions stream
     **/
    Stream<UserSession> sessions();

    /**
     * Returns all principal sessions
     *
     * @param principal principal
     * @return principal sessions stream
     **/
    Stream<UserSession> sessions(Object principal);

    /**
     * Return current user sessions
     *
     * @param id user session id
     * @return user session
     **/
    UserSession get(String id);

    /**
     * Invalidate user session
     *
     * @param session user session to invalidate
     **/
    void invalidate(UserSession session);

}
