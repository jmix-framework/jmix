/*
 * Copyright 2025 Haulmont.
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

import com.microsoft.aad.msal4j.*;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

public class MicrosoftOAuth2TokenProvider extends AbstractOAuth2TokenProvider {

    private static final Logger log = getLogger(MicrosoftOAuth2TokenProvider.class);

    public MicrosoftOAuth2TokenProvider(EmailerProperties emailerProperties, EmailRefreshTokenManager refreshTokenManager) {
        super(emailerProperties, refreshTokenManager);
    }

    @Override
    public String getAccessToken() {
        try {
            log.debug("Try to get access token");
            IClientCredential credential = createCredential();
            ConfidentialClientApplication app = buildClientApplication(credential);
            Set<String> scopes = getScopes();

            RefreshTokenParameters params = RefreshTokenParameters.builder(scopes, getRefreshToken()).build();

            CompletableFuture<IAuthenticationResult> future = app.acquireToken(params);
            IAuthenticationResult result = future.get();

            log.debug("Access token has been acquired with scopes: {} (expiration date = {})",
                    result.scopes(), result.expiresOnDate());
            return result.accessToken();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to acquire Microsoft SMTP access token", e);
        }
    }

    protected IClientCredential createCredential() {
        return ClientCredentialFactory.createFromSecret(getSecret());
    }

    protected ConfidentialClientApplication buildClientApplication(IClientCredential credential) {
        try {
            return ConfidentialClientApplication
                    .builder(getClientId(), credential)
                    .authority(buildAuthorityUrl())
                    .build();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to build client application", e);
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
