/*
 * Copyright 2025 Haulmont.
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

package io.jmix.authserver.service.cleanup.impl;

import io.jmix.authserver.introspection.UserDetailsOAuth2AuthenticatedPrincipal;
import io.jmix.core.security.event.AbstractUserInvalidationEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component("sec_UserInvalidationListener")
public class UserInvalidationListener {
    public static final String AUTHORIZATION_ID = "AUTHORIZATION_ID";
    private final Logger log = LoggerFactory.getLogger(UserInvalidationListener.class);

    @Autowired
    protected OAuth2AuthorizationService oAuth2AuthorizationService;

    @Autowired
    protected SessionRegistry sessionRegistry;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserInvalidation(AbstractUserInvalidationEvent event) {
        try {
            log.info("Handling user invalidation: {}", event.getUsername());

            List<UserDetails> principals = sessionRegistry.getAllPrincipals().stream()
                    .filter(p -> p instanceof UserDetails)
                    .map(p -> (UserDetails) p)
                    .filter(p -> p.getUsername().equals(event.getUsername()))
                    .toList();

            for (UserDetails principal : principals) {
                if (principal instanceof UserDetailsOAuth2AuthenticatedPrincipal authPrincipal
                        && authPrincipal.getAttributes().containsKey(AUTHORIZATION_ID)) {
                    OAuth2Authorization authorization = oAuth2AuthorizationService.findById(authPrincipal.getAttribute(AUTHORIZATION_ID));
                    if (authorization != null) {
                        oAuth2AuthorizationService.remove(authorization);
                    } else {
                        log.warn("No authorization for principal with name '{}'", principal.getUsername());
                    }
                }
            }
            log.info("Tokens were invalidated for a user: {}", event.getUsername());
        } catch (Throwable t) {
            log.error("An error occurred while handling invalidation for user: {}.", event.getUsername(), t);
        }
    }
}

