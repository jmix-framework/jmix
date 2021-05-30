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

package io.jmix.securityoauth2.impl.controller;

import io.jmix.securityoauth2.event.OAuth2TokenRevokedResponseEvent;
import io.jmix.securityoauth2.impl.TokenMasker;
import io.jmix.securityoauth2.impl.TokenRevoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST controller that is used for token revocation
 */
@RestController("sec_TokenController")
public class TokenController {

    private static final Logger log = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    protected TokenRevoker tokenRevoker;

    @Autowired
    protected TokenMasker tokenMasker;

    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    @PostMapping("/oauth/revoke")
    public ResponseEntity<?> revokeToken(@RequestParam("token") String token,
                                         @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint,
                                         Principal principal) {
        if (!(principal instanceof Authentication)) {
            throw new InsufficientAuthenticationException(
                    "There is no client authentication. Try adding an appropriate authentication filter.");
        }
        log.info("POST /oauth/revoke; token = {}, token_type_hint = {}", tokenMasker.maskToken(token), tokenTypeHint);

        String revokedTokenValue = null;
        if ("refresh_token".equals(tokenTypeHint)) {
            revokedTokenValue = tokenRevoker.revokeRefreshToken(token, (Authentication) principal);
            if (revokedTokenValue == null) {
                revokedTokenValue = tokenRevoker.revokeAccessToken(token, (Authentication) principal);
            }
        }

        if (revokedTokenValue == null) {
            revokedTokenValue = tokenRevoker.revokeAccessToken(token, (Authentication) principal);
            if (revokedTokenValue == null) {
                revokedTokenValue = tokenRevoker.revokeRefreshToken(token, (Authentication) principal);
            }
        }

        if (revokedTokenValue == null) {
            log.debug("No token with value {} was revoked.", tokenMasker.maskToken(token));
        }

        if (applicationEventPublisher != null) {
            OAuth2TokenRevokedResponseEvent event = new OAuth2TokenRevokedResponseEvent(token, revokedTokenValue);

            applicationEventPublisher.publishEvent(event);

            if (event.getResponseEntity() != null) {
                return event.getResponseEntity();
            }
        }

        return ResponseEntity.ok().build();
    }
}
