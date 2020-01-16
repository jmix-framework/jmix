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
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component(UserSessionFactory.NAME)
@Conditional(OnCoreSecurityImplementation.class)
public class CoreUserSessionFactory implements UserSessionFactory {

    private final UserSession SYSTEM_SESSION;

    public CoreUserSessionFactory() {
        CoreUser user = new CoreUser("system", "", "System");
        SystemAuthenticationToken authentication = new SystemAuthenticationToken(user, Collections.emptyList());
        SYSTEM_SESSION = new BuiltInSystemUserSession(authentication);
    }

    @Override
    public UserSession create(Authentication authentication) {
        return new UserSession(authentication);
    }

    @Override
    public UserSession getSystemSession() {
        return SYSTEM_SESSION;
    }

    private static class BuiltInSystemUserSession extends UserSession implements SystemUserSession {

        private static final long serialVersionUID = 6457244307815440998L;

        public BuiltInSystemUserSession(SystemAuthenticationToken authentication) {
            super(authentication);
            id = new UUID(1L, 1L);
            clientDetails = ClientDetails.builder().info("System authentication").build();
        }
    }
}
