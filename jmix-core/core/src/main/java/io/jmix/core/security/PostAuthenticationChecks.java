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

package io.jmix.core.security;

import io.jmix.core.security.event.PostAuthenticationCheckEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;


/**
 * Verifies the status of the loaded UserDetails after validation of the credentials takes place.
 * See {@link org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider#setPostAuthenticationChecks(UserDetailsChecker)}.
 */
public class PostAuthenticationChecks implements UserDetailsChecker {
    private ApplicationEventPublisher eventPublisher;
    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private static final Logger log = LoggerFactory.getLogger(PostAuthenticationChecks.class);

    @Autowired
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void check(UserDetails user) {
        if (!user.isCredentialsNonExpired()) {
            log.debug("Failed to authenticate since user account credentials have expired");
            throw new CredentialsExpiredException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.credentialsExpired",
                    "User credentials have expired"));
        }
        firePostAuthenticationCheckEvent(user);
    }

    protected void firePostAuthenticationCheckEvent(UserDetails user) {
        eventPublisher.publishEvent(new PostAuthenticationCheckEvent(user));
    }
}
