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

package io.jmix.saml.converter;

import io.jmix.saml.mapper.user.SamlUserMapper;
import io.jmix.saml.user.JmixSamlUserDetails;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class SamlResponseAuthenticationConverter implements Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> {

    private static final Logger log = getLogger(SamlResponseAuthenticationConverter.class);

    protected final SamlUserMapper samlUserMapper;

    public SamlResponseAuthenticationConverter(SamlUserMapper samlUserMapper) {
        this.samlUserMapper = samlUserMapper;
    }

    @Override
    public Saml2Authentication convert(OpenSaml4AuthenticationProvider.ResponseToken responseToken) {
        log.info("SAML response conversion started");

        try {
            Saml2AuthenticationToken token = responseToken.getToken();
            Response response = responseToken.getResponse();

            // Check for encrypted assertions
            if (!response.getEncryptedAssertions().isEmpty()) {
                log.debug("Response contains {} encrypted assertions", response.getEncryptedAssertions().size());
                // Note: Decryption is handled by OpenSaml4AuthenticationProvider if decryption credentials are configured
            }

            Assertion assertion = getAssertion(responseToken);
            if (assertion == null) {
                throw new IllegalStateException("SAML response doesn't contain assertions");
            }

            log.debug("Processing assertion for subject: {}", Optional.ofNullable(assertion.getSubject())
                    .map(s -> s.getNameID())
                    .map(name -> name.getValue())
                    .orElse("unknown")
            );

            JmixSamlUserDetails principal = samlUserMapper.toJmixUser(assertion, responseToken);
            log.info("Successfully converted SAML assertion to Jmix user: {}", principal.getUsername());

            Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
            log.debug("User granted {} authorities", authorities.size());

            return new Saml2Authentication(principal, token.getSaml2Response(), authorities);
        } catch (Exception e) {
            throw new Saml2Exception("Failed to convert SAML response", e);
        }
    }

    /**
     * Extracts assertion from SAML response, handling both plain and encrypted assertions.
     */
    protected Assertion getAssertion(OpenSaml4AuthenticationProvider.ResponseToken responseToken) {
        Response response = responseToken.getResponse();

        // First try to get plain assertions
        Assertion assertion = CollectionUtils.firstElement(response.getAssertions());

        if (assertion == null && !response.getEncryptedAssertions().isEmpty()) {
            log.warn("Only encrypted assertions found. Ensure decryption credentials are properly configured.");
            // OpenSaml4AuthenticationProvider should have already decrypted if credentials are configured
            throw new IllegalStateException(
                    "Response contains only encrypted assertions but they were not decrypted. " +
                            "Please configure decryption credentials in application.properties"
            );
        }

        return assertion;
    }
}
