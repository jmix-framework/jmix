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

package io.jmix.securityoauth2.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.jmix.core.security.UserRepository;
import io.jmix.securityoauth2.SecurityOAuth2Properties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Collections;

public class DevTokenService extends DefaultTokenServices {
    private final SecurityOAuth2Properties oauth2Properties;
    private final UserRepository userRepository;

    public DevTokenService(UserRepository userRepository,
                           SecurityOAuth2Properties oauth2Properties) {
        this.userRepository = userRepository;
        this.oauth2Properties = oauth2Properties;
        Preconditions.checkState(oauth2Properties.getDevUsername() != null, "Dev username is empty");
    }

    @Override
    public OAuth2Authentication loadAuthentication(String accessTokenValue) throws AuthenticationException, InvalidTokenException {
        OAuth2Authentication oauth2Authentication = new OAuth2Authentication(buildRequest(), buildAuthentication());
        oauth2Authentication.setAuthenticated(true);
        return oauth2Authentication;
    }

    protected OAuth2Request buildRequest() {
        return new OAuth2Request(Collections.emptyMap(),
                oauth2Properties.getClientId(),
                Collections.emptyList(),
                true,
                Sets.newHashSet("api"),
                Collections.emptySet(),
                null,
                Collections.emptySet(),
                Collections.emptyMap());
    }

    protected Authentication buildAuthentication() {
        UserDetails userDetails = userRepository.loadUserByUsername(oauth2Properties.getDevUsername());
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
