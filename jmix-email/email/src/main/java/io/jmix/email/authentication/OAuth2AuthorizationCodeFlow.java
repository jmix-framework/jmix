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

package io.jmix.email.authentication;

import org.jspecify.annotations.NullMarked;

/**
 * Interface defining the OAuth2 authorization code flow used to connect a mailbox account:
 * the user is redirected to the provider consent page and returns to the application callback
 * with an authorization code.
 */
@NullMarked
public interface OAuth2AuthorizationCodeFlow {

    /**
     * Builds the provider consent page URL.
     *
     * @param redirectUri application callback URI registered for the OAuth client
     * @param state       opaque value used to protect the round trip against CSRF
     * @return URL to redirect the user to
     */
    String buildAuthorizationUrl(String redirectUri, String state);

    /**
     * Exchanges the authorization code received on the callback for tokens and stores
     * the obtained refresh token via {@link EmailRefreshTokenManager}.
     *
     * @param authorizationCode code received on the redirect URI
     * @param redirectUri       the same redirect URI that was used to build the authorization URL
     * @throws IllegalStateException if the exchange fails or the response contains no refresh token
     */
    void completeAuthorization(String authorizationCode, String redirectUri);
}
