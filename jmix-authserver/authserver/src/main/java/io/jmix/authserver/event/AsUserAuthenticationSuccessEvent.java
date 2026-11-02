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

package io.jmix.authserver.event;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

/**
 * Event published when the authorization server has issued tokens to a user: the REST counterpart of the
 * {@link org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent} published on
 * login to UI.
 * <p>
 * The event is published by the token endpoint for the {@code password} and {@code authorization_code} grants,
 * after the tokens are saved to the {@link OAuth2Authorization} and before the token response is written. It is not
 * published for the {@code refresh_token} and {@code client_credentials} grants. The event means that the server
 * issued the tokens; what the client does with them afterwards is not tracked.
 * <p>
 * Listeners are invoked synchronously with the user's authentication in the security context. A listener can reject
 * the authentication by throwing an {@link org.springframework.security.core.AuthenticationException}: the issued
 * authorization is removed and the client receives the {@code invalid_grant} error. Any other exception thrown by a
 * listener is logged, and the tokens are issued.
 */
@NullMarked
public class AsUserAuthenticationSuccessEvent extends AbstractAuthenticationEvent {

    private static final long serialVersionUID = -2398407735420736520L;

    private final RegisteredClient registeredClient;
    private final OAuth2Authorization authorization;
    private final transient HttpServletRequest request;

    public AsUserAuthenticationSuccessEvent(Authentication authentication,
                                        RegisteredClient registeredClient,
                                        OAuth2Authorization authorization,
                                        HttpServletRequest request) {
        super(authentication);
        this.registeredClient = registeredClient;
        this.authorization = authorization;
        this.request = request;
    }

    /**
     * @return the client that requested the tokens
     */
    public RegisteredClient getRegisteredClient() {
        return registeredClient;
    }

    /**
     * @return the saved authorization holding the issued tokens
     */
    public OAuth2Authorization getAuthorization() {
        return authorization;
    }

    /**
     * @return the grant type the tokens were issued for
     */
    public AuthorizationGrantType getAuthorizationGrantType() {
        return authorization.getAuthorizationGrantType();
    }

    /**
     * @return the token request
     */
    public HttpServletRequest getRequest() {
        return request;
    }
}
