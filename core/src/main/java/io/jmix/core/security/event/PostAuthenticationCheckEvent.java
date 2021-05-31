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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

/**
 * Event is fired during authentication process to verify the status of the loaded UserDetails
 * after validation of the credentials takes place.
 * See {@link org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider#setPostAuthenticationChecks(UserDetailsChecker)}.
 */
public class PostAuthenticationCheckEvent extends ApplicationEvent {
    private static final long serialVersionUID = 894886319582273173L;

    /**
     * Create a new {@code PreAuthenticationCheckEvent}.
     *
     * @param user - {@link UserDetails} to validate.
     */
    public PostAuthenticationCheckEvent(UserDetails user) {
        super(user);
    }

    /**
     * @return {@link UserDetails} to validate
     */
    public UserDetails getUser() {
        return (UserDetails) getSource();
    }
}
