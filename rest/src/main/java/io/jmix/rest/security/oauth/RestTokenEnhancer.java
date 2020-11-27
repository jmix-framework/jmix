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

package io.jmix.rest.security.oauth;

import io.jmix.core.session.SessionData;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * A token enhancer that provides session info details
 */
@Component
public class RestTokenEnhancer implements TokenEnhancer {
    @Autowired
    private ObjectFactory<SessionData> sessionDataFactory;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Map<String, Object> additionalInformation = new HashMap<>(accessToken.getAdditionalInformation());

        SessionData sessionData = sessionDataFactory.getObject();
        HttpSession session = sessionData.getHttpSession();

        additionalInformation.put(OAuth2AccessTokenSessionIdResolver.SESSION_ID, session.getId());

        DefaultOAuth2AccessToken mutableAccessToken = (DefaultOAuth2AccessToken) accessToken;
        mutableAccessToken.setAdditionalInformation(additionalInformation);

        sessionData.setAttribute(OAuth2AccessTokenSessionIdResolver.ACCESS_TOKEN, accessToken.getValue());

        return accessToken;
    }
}
