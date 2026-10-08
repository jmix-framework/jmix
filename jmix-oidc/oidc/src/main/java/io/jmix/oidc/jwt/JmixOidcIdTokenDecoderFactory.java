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

package io.jmix.oidc.jwt;

import io.jmix.core.common.util.Preconditions;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link JwtDecoderFactory} used by OpenID Connect login for verifying ID token signatures with the keys
 * from the provider JWK Set endpoint.
 * <p>
 * Reproduces the behavior of the standard {@link OidcIdTokenDecoderFactory} with the default
 * {@link SignatureAlgorithm#RS256 RS256} algorithm, except that the JWK Set is loaded using the given
 * {@link RestOperations}, which allows configuring HTTP timeouts instead of the 500 ms defaults applied
 * by Spring Security. Created decoders are cached per client registration id, so the JWK Set cache inside
 * the decoder is reused between logins.
 */
public class JmixOidcIdTokenDecoderFactory implements JwtDecoderFactory<ClientRegistration> {

    protected static final String MISSING_SIGNATURE_VERIFIER_ERROR_CODE = "missing_signature_verifier";

    protected final RestOperations restOperations;

    protected final Map<String, JwtDecoder> jwtDecoders = new ConcurrentHashMap<>();

    public JmixOidcIdTokenDecoderFactory(RestOperations restOperations) {
        Preconditions.checkNotNullArgument(restOperations, "restOperations cannot be null");
        this.restOperations = restOperations;
    }

    @Override
    public JwtDecoder createDecoder(ClientRegistration clientRegistration) {
        Preconditions.checkNotNullArgument(clientRegistration, "clientRegistration cannot be null");
        return jwtDecoders.computeIfAbsent(clientRegistration.getRegistrationId(),
                key -> buildDecoder(clientRegistration));
    }

    protected JwtDecoder buildDecoder(ClientRegistration clientRegistration) {
        String jwkSetUri = clientRegistration.getProviderDetails().getJwkSetUri();
        if (!StringUtils.hasText(jwkSetUri)) {
            OAuth2Error oauth2Error = new OAuth2Error(MISSING_SIGNATURE_VERIFIER_ERROR_CODE,
                    "Failed to find a Signature Verifier for Client Registration: '"
                            + clientRegistration.getRegistrationId()
                            + "'. Check to ensure you have configured the JwkSet URI.",
                    null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .restOperations(restOperations)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(
                List.of(new OidcIdTokenValidator(clientRegistration))));
        decoder.setClaimSetConverter(OidcIdTokenDecoderFactory.createDefaultClaimTypeConverter());
        return decoder;
    }
}
