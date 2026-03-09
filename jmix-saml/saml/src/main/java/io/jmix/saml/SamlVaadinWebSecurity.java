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

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import io.jmix.saml.config.SamlHttpSecurityConfigurer;
import io.jmix.saml.converter.SamlResponseAuthenticationConverter;
import io.jmix.saml.mapper.user.SamlUserMapper;
import io.jmix.saml.user.JmixSamlUserDetails;
import io.jmix.security.util.JmixHttpSecurityUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2RelyingPartyInitiatedLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.security.config.Customizer.withDefaults;

public class SamlVaadinWebSecurity extends VaadinWebSecurity {

    private static final Logger log = getLogger(SamlVaadinWebSecurity.class);

    @Autowired
    protected SamlUserMapper<? extends JmixSamlUserDetails> samlUserMapper;
    @Autowired
    protected RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;
    @Autowired
    protected SamlProperties samlProperties;
    @Autowired(required = false)
    protected List<SamlHttpSecurityConfigurer> additionalConfigurers = Collections.emptyList();

    /*@Autowired
    public void setSamlUserMapper(SamlUserMapper<? extends JmixSamlUserDetails> samlUserMapper) {
        this.samlUserMapper = samlUserMapper;
    }

    @Autowired
    public void setRelyingPartyRegistrationRepository(RelyingPartyRegistrationRepository repository) {
        this.relyingPartyRegistrationRepository = repository;
    }*/

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        OpenSaml4AuthenticationProvider authenticationProvider = createAuthenticationProvider(samlUserMapper);

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
    }

    protected OpenSaml4AuthenticationProvider createAuthenticationProvider(SamlUserMapper samlUserMapper) {
        OpenSaml4AuthenticationProvider authenticationProvider = new OpenSaml4AuthenticationProvider();
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

        OpenSaml4LogoutRequestResolver resolver = new OpenSaml4LogoutRequestResolver(effectiveRepository);
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

    protected Converter<OpenSaml4AuthenticationProvider.ResponseToken, ? extends AbstractAuthenticationToken> createSamlAuthConverter(SamlUserMapper samlUserMapper) {
        return new SamlResponseAuthenticationConverter(samlUserMapper);
    }

    /*protected Converter<OpenSaml4AuthenticationProvider.ResponseToken, ? extends AbstractAuthenticationToken> samlAuthConverter3() {
        return responseToken -> {
            // 1. Use default conversion as base
            Saml2Authentication authentication = OpenSaml4AuthenticationProvider
                    .createDefaultResponseAuthenticationConverter()
                    .convert(responseToken);

            Object principal = authentication.getPrincipal();
            String saml2Response = authentication.getSaml2Response();
            String name = authentication.getName();
            Object details = authentication.getDetails();
            Collection<GrantedAuthority> authorities = authentication.getAuthorities();

            // 2. Extract assertions from SAML response
            List<Assertion> assertions = responseToken.getResponse().getAssertions();
            List<EncryptedAssertion> encryptedAssertions = responseToken.getResponse().getEncryptedAssertions();

            if (assertions.isEmpty()) {
                throw new RuntimeException("No assertions in SAML response");
            }

            // 3. Use first assertion (Keycloak sends only one)
            Assertion assertion = assertions.get(0);

            // 4. Extract attributes from AttributeStatement
            Map<String, List<Object>> attributes = assertion.getAttributeStatements()
                    .stream()
                    .flatMap(as -> as.getAttributes().stream())
                    .collect(Collectors.toMap(
                            Attribute::getName,
                            attr -> attr.getAttributeValues().stream()
                                    .map(xmlObject -> xmlObject.getDOM().getTextContent())
                                    .collect(Collectors.toList())
                    ));

            // 5. Create custom principal with extracted attributes
            *//*CustomSamlPrincipal principal = new CustomSamlPrincipal(
                    authentication.getPrincipal().getName(),
                    attributes,
                    assertion.getSubject().getNameID().getValue()
            );*//*

            // 6. Return new authentication with custom principal
            *//*return new Saml2Authentication(
                    principal,
                    authentication.getSaml2Response(),
                    authentication.getAuthorities()
            );*//*
            return authentication;
        };
    }*/
}
