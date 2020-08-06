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

package io.jmix.audit.sessions;
import io.jmix.audit.entity.UserSession;

import java.util.stream.Stream;

public interface UserSessions {

    String NAME = "core_UserSessions";

    Stream<UserSession> sessions();

    Stream<UserSession> sessions(Object principal);

    UserSession get(String id);

    void invalidate(UserSession session);

}
