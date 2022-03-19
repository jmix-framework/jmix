/*
 * Copyright 2021 Haulmont.
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

package io.jmix.core.security.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Event published after user substituted and new {@link Authentication} is set to current {@link SecurityContext}.
 */
public class UserSubstitutedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1782141271679862065L;

    private final UserDetails authenticatedUser;

    private final UserDetails substitutedUser;

    public UserSubstitutedEvent(UserDetails authenticatedUser, UserDetails substitutedUser) {
        super(authenticatedUser);
        this.authenticatedUser = authenticatedUser;
        this.substitutedUser = substitutedUser;
    }

    public UserDetails getAuthenticatedUser() {
        return authenticatedUser;
    }

    public UserDetails getSubstitutedUser() {
        return substitutedUser;
    }
}
