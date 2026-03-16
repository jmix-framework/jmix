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

package io.jmix.saml;

import io.jmix.saml.config.SamlHttpSecurityConfigurer;
import io.jmix.saml.converter.SamlResponseAuthenticationConverter;
import io.jmix.saml.mapper.user.SamlUserMapper;
import io.jmix.saml.user.JmixSamlUserDetails;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml5LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2RelyingPartyInitiatedLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class SamlVaadinWebSecurity {

    private static final Logger log = getLogger(SamlVaadinWebSecurity.class);

    @Autowired
    protected SamlUserMapper<? extends JmixSamlUserDetails> samlUserMapper;
    @Autowired
    protected RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;
    @Autowired
    protected SamlProperties samlProperties;
    @Autowired(required = false)
    protected List<SamlHttpSecurityConfigurer> additionalConfigurers = Collections.emptyList();

    /*@Override
    protected void configure(HttpSecurity http) throws Exception {
        OpenSaml5AuthenticationProvider authenticationProvider = createAuthenticationProvider(samlUserMapper);

        JmixHttpSecurityUtils.configureAnonymous(http);
        JmixHttpSecurityUtils.configureSessionManagement(http);
        JmixHttpSecurityUtils.configureFrameOptions(http);

        http
                .saml2Login(withDefaults())
                .saml2Logout(withDefaults())
                .logout(logout -> logout
                        .logoutSuccessHandler(createSamlLogoutSuccessHandler())
                )
                .authenticationManager(new ProviderManager(authenticationProvider));

        for (SamlHttpSecurityConfigurer configurer : additionalConfigurers) {
            log.debug("Applying additional security configurer: {}", configurer);
            configurer.configure(http);
        }

        super.configure(http);
    }*/

    protected OpenSaml5AuthenticationProvider createAuthenticationProvider(SamlUserMapper samlUserMapper) {
        OpenSaml5AuthenticationProvider authenticationProvider = new OpenSaml5AuthenticationProvider();
        authenticationProvider.setResponseAuthenticationConverter(createSamlAuthConverter(samlUserMapper));
        return authenticationProvider;
    }

    protected LogoutSuccessHandler createSamlLogoutSuccessHandler() {
        if (relyingPartyRegistrationRepository == null) {
            throw new Saml2Exception("RelyingPartyRegistrationRepository is not available");
        }

        RelyingPartyRegistrationRepository effectiveRepository;
        if (isForceRedirectBindingLogout()) {
            // Wrap repository to override binding to REDIRECT for Vaadin compatibility
            effectiveRepository = createRelyingPartyRegistrationRepositoryWrapperWithRedirectBinding(relyingPartyRegistrationRepository);
        } else {
            effectiveRepository = relyingPartyRegistrationRepository;
        }

        OpenSaml5LogoutRequestResolver resolver = new OpenSaml5LogoutRequestResolver(effectiveRepository);
        return new Saml2RelyingPartyInitiatedLogoutSuccessHandler(resolver);
    }

    protected RelyingPartyRegistrationRepository createRelyingPartyRegistrationRepositoryWrapperWithRedirectBinding(
            RelyingPartyRegistrationRepository originalRepository
    ) {
        return new RelyingPartyRegistrationRepository() {
            @Override
            public RelyingPartyRegistration findByRegistrationId(String registrationId) {
                RelyingPartyRegistration original = originalRepository.findByRegistrationId(registrationId);
                if (original == null) {
                    return null;
                }

                // Clone registration and override SingleLogoutService binding to REDIRECT
                log.debug("Overriding SAML logout binding to REDIRECT for registration: {}", registrationId);

                RelyingPartyRegistration.Builder builder = original.mutate();
                RelyingPartyRegistration result = builder.assertingPartyMetadata(party -> party
                        .singleLogoutServiceBinding(Saml2MessageBinding.REDIRECT)
                ).build();
                return result;
            }

            @Override
            public RelyingPartyRegistration findUniqueByAssertingPartyEntityId(String entityId) {
                return originalRepository.findUniqueByAssertingPartyEntityId(entityId);
            }
        };
    }

    protected boolean isForceRedirectBindingLogout() {
        return samlProperties.isForceRedirectBindingLogout();
    }

    protected Converter<OpenSaml5AuthenticationProvider.ResponseToken, ? extends AbstractAuthenticationToken> createSamlAuthConverter(SamlUserMapper samlUserMapper) {
        return new SamlResponseAuthenticationConverter(samlUserMapper);
    }
}
