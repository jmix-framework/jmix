/*
 * Copyright 2026 Haulmont.
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

package io.jmix.authserver.authentication;

import io.jmix.authserver.event.AsUserAuthenticationSuccessEvent;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;

/**
 * Sets {@link JmixOAuth2AccessTokenResponseHandler} as the access token response handler of the token endpoint, so
 * that {@link AsUserAuthenticationSuccessEvent} is published. Must be applied to the {@link HttpSecurity} that has
 * {@link OAuth2AuthorizationServerConfigurer}.
 */
@NullMarked
public class JmixOAuth2TokenEndpointConfigurer
        extends AbstractHttpConfigurer<JmixOAuth2TokenEndpointConfigurer, HttpSecurity> {

    @Override
    public void init(HttpSecurity http) {
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        JmixOAuth2AccessTokenResponseHandler accessTokenResponseHandler = new JmixOAuth2AccessTokenResponseHandler(
                applicationContext.getBean(OAuth2AuthorizationService.class),
                applicationContext,
                applicationContext.getBeanProvider(IssuedAuthorizationProcessor.class));

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class);
        authorizationServerConfigurer.tokenEndpoint(tokenEndpoint ->
                tokenEndpoint.accessTokenResponseHandler(accessTokenResponseHandler));
    }
}
