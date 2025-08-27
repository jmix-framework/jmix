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

package io.jmix.email.sandbox;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;

import java.util.Collections;
import java.util.Set;

//todo remove?
public class MicrosoftTokenProvider implements OAuth2TokenProvider {

    private final ConfidentialClientApplication clientApplication;
    private final Set<String> scopes = Collections.singleton("https://outlook.office365.com/.default offline_access");

    public MicrosoftTokenProvider(
            String clientId,
            String clientSecret,
            String tenantId
    ) throws Exception {
        this.clientApplication = ConfidentialClientApplication.builder(
                        clientId,
                        ClientCredentialFactory.createFromSecret(clientSecret))
                .authority("https://login.microsoftonline.com/" + tenantId + "/")
                .build();
    }

    @Override
    public String getAccessToken() {
        try {
            ClientCredentialParameters parameters = ClientCredentialParameters.builder(scopes).build();

            IAuthenticationResult result = clientApplication.acquireToken(parameters).get();
            return result.accessToken();
        } catch (Exception e) {
            throw new RuntimeException("MSAL token acquisition failed", e);
        }
    }
}
