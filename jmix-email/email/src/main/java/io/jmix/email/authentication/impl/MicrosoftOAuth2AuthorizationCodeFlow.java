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

package io.jmix.email.authentication.impl;

import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.authentication.OAuth2AuthorizationCodeFlow;
import io.jmix.email.authentication.OAuth2ClientType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

/**
 * Authorization code flow for Microsoft accounts. MSAL appends the reserved scopes
 * ({@code offline_access} etc.) required to obtain a refresh token.
 */
@NullMarked
public class MicrosoftOAuth2AuthorizationCodeFlow extends AbstractOAuth2Flow implements OAuth2AuthorizationCodeFlow {

    private static final Logger log = LoggerFactory.getLogger(MicrosoftOAuth2AuthorizationCodeFlow.class);

    public MicrosoftOAuth2AuthorizationCodeFlow(EmailerProperties emailerProperties,
                                                EmailRefreshTokenManager refreshTokenManager) {
        super(emailerProperties, refreshTokenManager);
    }

    /**
     * The URL is built manually: the authorization response must come back as a GET redirect so
     * that the callback view can read the query parameters (a POST is also rejected by the CSRF
     * protection), but MSAL silently overrides {@code ResponseMode.QUERY} with {@code form_post}.
     * The Entra endpoint itself supports {@code response_mode=query} as defined by the OAuth spec.
     */
    @Override
    public String buildAuthorizationUrl(String redirectUri, String state) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("client_id", getClientId());
        parameters.put("response_type", "code");
        parameters.put("redirect_uri", redirectUri);
        parameters.put("scope", String.join(" ", getAuthorizationScopes()));
        parameters.put("response_mode", "query");
        parameters.put("state", state);
        return buildAuthorityUrl() + "/oauth2/v2.0/authorize?" + encodeForm(parameters);
    }

    /**
     * Scopes requested on the consent page. Unlike MSAL-built requests, the manually built URL
     * must include the reserved scopes ({@code offline_access} is required to receive a refresh
     * token) explicitly.
     */
    protected Set<String> getAuthorizationScopes() {
        Set<String> scopes = new LinkedHashSet<>(List.of("openid", "profile", "offline_access"));
        scopes.addAll(getScopes());
        return scopes;
    }

    @Override
    public void completeAuthorization(String authorizationCode, String redirectUri) {
        RefreshTokenCapturingCacheAspect cacheAspect = new RefreshTokenCapturingCacheAspect();
        try {
            IAuthenticationResult result = acquireTokenByAuthorizationCode(cacheAspect, authorizationCode, redirectUri);
            log.debug("Authorization code has been exchanged, scopes: {}", result.scopes());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to exchange the authorization code for tokens", e);
        }

        String refreshToken = getCapturedRefreshToken(cacheAspect);
        if (refreshToken == null) {
            throw new IllegalStateException("The token response contains no refresh token."
                    + " Check the OAuth client configuration");
        }
        refreshTokenManager.storeRefreshTokenValue(refreshToken, OAuth2ClientType.CONFIDENTIAL);
        log.info("Mailbox account has been connected using the authorization code flow");
    }

    protected IAuthenticationResult acquireTokenByAuthorizationCode(RefreshTokenCapturingCacheAspect cacheAspect,
                                                                    String authorizationCode,
                                                                    String redirectUri) throws Exception {
        ConfidentialClientApplication application = buildClientApplication(cacheAspect);
        AuthorizationCodeParameters parameters = AuthorizationCodeParameters
                .builder(authorizationCode, new URI(redirectUri))
                .scopes(getScopes())
                .build();
        return application.acquireToken(parameters).get();
    }

    @Nullable
    protected String getCapturedRefreshToken(RefreshTokenCapturingCacheAspect cacheAspect) {
        return cacheAspect.getLatestRefreshToken();
    }

    protected ConfidentialClientApplication buildClientApplication(@Nullable RefreshTokenCapturingCacheAspect cacheAspect) {
        try {
            ConfidentialClientApplication.Builder builder = ConfidentialClientApplication
                    .builder(getClientId(), ClientCredentialFactory.createFromSecret(getSecret()))
                    .authority(buildAuthorityUrl());
            if (cacheAspect != null) {
                builder.setTokenCacheAccessAspect(cacheAspect);
            }
            return builder.build();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to build client application", e);
        }
    }

    protected Set<String> getScopes() {
        return Collections.singleton("https://outlook.office.com/SMTP.Send");
    }

    protected String buildAuthorityUrl() {
        return getBaseAuthorityUrl() + "/" + getTenant();
    }

    protected String getBaseAuthorityUrl() {
        return "https://login.microsoftonline.com";
    }

    protected String getTenant() {
        return emailerProperties.getOAuth2().getTenantId();
    }
}
