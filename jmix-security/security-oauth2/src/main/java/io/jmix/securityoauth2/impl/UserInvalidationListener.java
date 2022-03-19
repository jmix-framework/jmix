/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.securityoauth2.impl;

import io.jmix.core.security.event.AbstractUserInvalidationEvent;
import io.jmix.securityoauth2.SecurityOAuth2Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.Collection;

@Component("sec_UserInvalidationListener")
public class UserInvalidationListener {

    private final Logger log = LoggerFactory.getLogger(UserInvalidationListener.class);

    @Autowired
    private TokenStore tokenStore;
    @Autowired
    private SecurityOAuth2Properties properties;

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onUserInvalidation(AbstractUserInvalidationEvent event) {
        try {
            log.info("Handling user invalidation: {}", event.getUsername());

            Collection<OAuth2AccessToken> accessTokens = new ArrayList<>(
                    tokenStore.findTokensByClientIdAndUserName(properties.getClientId(), event.getUsername()));

            for (OAuth2AccessToken accessToken : accessTokens) {
                tokenStore.removeAccessToken(accessToken);
                if (accessToken.getRefreshToken() != null) {
                    tokenStore.removeRefreshToken(accessToken.getRefreshToken());
                }
            }

            log.info("Tokens were invalidated for a user: {}", event.getUsername());
        } catch (Throwable t) {
            log.error("An error occurred while handling invalidation for user: {}.", event.getUsername(), t);
        }
    }
}

