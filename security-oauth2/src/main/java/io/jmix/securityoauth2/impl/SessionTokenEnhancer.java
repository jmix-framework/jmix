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

package io.jmix.securityoauth2.impl;

import io.jmix.core.session.SessionData;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

/**
 * A token enhancer that provides session info details
 */
public class SessionTokenEnhancer implements TokenEnhancer {
    protected ObjectProvider<SessionData> sessionDataProvider;

    public SessionTokenEnhancer(ObjectProvider<SessionData> sessionDataProvider) {
        this.sessionDataProvider = sessionDataProvider;
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        SessionData sessionData = sessionDataProvider.getObject();
        sessionData.setAttribute("OAuth2.ACCESS_TOKEN", accessToken.getValue());

        Map<String, Object> additionalInformation = new HashMap<>(accessToken.getAdditionalInformation());
        additionalInformation.put("OAuth2.SESSION_ID", sessionData.getSessionId());

        DefaultOAuth2AccessToken mutableAccessToken = (DefaultOAuth2AccessToken) accessToken;
        mutableAccessToken.setAdditionalInformation(additionalInformation);

        return mutableAccessToken;
    }
}
