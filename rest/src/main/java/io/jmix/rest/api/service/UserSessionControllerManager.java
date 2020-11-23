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
 *
 */

package io.jmix.rest.api.service;

import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.rest.api.auth.OAuthTokenRevoker;
import io.jmix.rest.api.common.RestAuthUtils;
import io.jmix.rest.api.exception.RestAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Component("rest_UserSessionControllerManager")
public class UserSessionControllerManager {

    @Autowired
    protected RestAuthUtils restAuthUtils;
    @Autowired
    protected TokenStore tokenStore;

    public void setSessionLocale(HttpServletRequest request) {
        Locale locale = restAuthUtils.extractLocaleFromRequestHeader(request);

        if (locale != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof OAuth2Authentication) {
                OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) authentication;
                //OAuth2AccessToken accessToken = tokenStore.getAccessToken(oAuth2Authentication);

                Authentication userAuthentication = oAuth2Authentication.getUserAuthentication();
                if (userAuthentication.getDetails() instanceof ClientDetails
                        && userAuthentication instanceof AbstractAuthenticationToken) {
                    ClientDetails clientDetails = (ClientDetails) userAuthentication.getDetails();
                    ((AbstractAuthenticationToken) userAuthentication).setDetails(ClientDetails.builder()
                            .of(clientDetails)
                            .locale(locale)
                            .build());

                    //tokenStore.storeAccessToken(accessToken, oAuth2Authentication);

                    return;
                }
            }
        }

        throw new RestAPIException("Could not change user session locale", null, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
