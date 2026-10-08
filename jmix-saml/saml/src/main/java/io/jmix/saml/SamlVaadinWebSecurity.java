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

import com.google.common.base.Strings;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import io.jmix.securityflowui.security.AbstractFlowuiWebSecurity;
import io.jmix.saml.config.SamlHttpSecurityConfigurer;
import io.jmix.saml.converter.SamlResponseAuthenticationConverter;
import io.jmix.saml.logout.SamlVaadinLogoutSuccessHandler;
import io.jmix.saml.mapper.user.SamlUserMapper;
import io.jmix.saml.user.JmixSamlUserDetails;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml5LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Provides Vaadin security to the project. Configures authentication using the SAML 2.0 provider.
 */
public class SamlVaadinWebSecurity extends AbstractFlowuiWebSecurity {

    private static final Logger log = getLogger(SamlVaadinWebSecurity.class);
    private static final String DEFAULT_LOGIN_PAGE_URL = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;
    private static final String SAML_AUTHENTICATION_REQUEST_URI = "/saml2/authenticate";

    @Autowired
    protected SamlUserMapper<? extends JmixSamlUserDetails> samlUserMapper;
    @Autowired
    protected RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;
    @Autowired
    protected SamlProperties samlProperties;
    @Autowired(required = false)
    protected List<SamlHttpSecurityConfigurer> additionalConfigurers = Collections.emptyList();

    @Override
    protected void configureJmixSpecifics(HttpSecurity http) throws Exception {
        OpenSaml5AuthenticationProvider authenticationProvider = createAuthenticationProvider(samlUserMapper);

        super.configureJmixSpecifics(http);
        // The same resolver configuration is used for both logout paths: the direct 'POST /logout' handled
        // by Saml2RelyingPartyInitiatedLogoutFilter and the Vaadin AuthenticationContext.logout() path
        // handled by the logout success handler, so the logout binding policy stays consistent.
        Saml2LogoutRequestResolver logoutRequestResolver = createSamlLogoutRequestResolver();
        http
                // The authentication manager is scoped to the SAML login filter so that other
                // authentication mechanisms of this chain keep working with the default manager
                .saml2Login(saml2 -> saml2
                        .authenticationManager(new ProviderManager(authenticationProvider))
                )
                .saml2Logout(logout -> logout
                        .logoutRequest(request -> request.logoutRequestResolver(logoutRequestResolver))
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(createSamlLogoutSuccessHandler())
                );

        if (samlProperties.isExposeMetadata()) {
            // Expose the service provider metadata XML used to configure the identity provider
            http.saml2Metadata(Customizer.withDefaults());
        }

        for (SamlHttpSecurityConfigurer configurer : additionalConfigurers) {
            log.debug("Applying additional security configurer: {}", configurer);
            configurer.configure(http);
        }
    }

    @Override
    protected void configureVaadinSpecifics(HttpSecurity http) {
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
        });
        initNavigationAccessControlLoginView(http, getSamlLoginUrl());
    }

    /**
     * Mirrors the default Spring Security SAML login behavior so that Vaadin navigation
     * access control points unauthenticated users to the same login URL.
     */
    protected String getSamlLoginUrl() {
        if (relyingPartyRegistrationRepository instanceof Iterable<?> registrations) {
            String loginUrl = null;

            for (Object candidate : registrations) {
                if (!(candidate instanceof RelyingPartyRegistration registration)) {
                    continue;
                }

                if (loginUrl != null) {
                    return DEFAULT_LOGIN_PAGE_URL;
                }

                loginUrl = SAML_AUTHENTICATION_REQUEST_URI + "?registrationId=" + registration.getRegistrationId();
            }

            if (loginUrl != null) {
                return loginUrl;
            }
        }

        return DEFAULT_LOGIN_PAGE_URL;
    }

    protected OpenSaml5AuthenticationProvider createAuthenticationProvider(SamlUserMapper<?> samlUserMapper) {
        OpenSaml5AuthenticationProvider authenticationProvider = new OpenSaml5AuthenticationProvider();
        authenticationProvider.setResponseAuthenticationConverter(createSamlAuthConverter(samlUserMapper));
        return authenticationProvider;
    }

    /**
     * Creates a resolver that generates SAML LogoutRequests for relying-party-initiated logout. If
     * {@link SamlProperties#isForceRedirectBindingLogout()} is enabled, the logout binding is overridden to
     * REDIRECT because it is the only binding that can be delivered from a Vaadin UI request (the POST binding
     * requires rendering an HTML page which is not possible in a UIDL response).
     */
    protected Saml2LogoutRequestResolver createSamlLogoutRequestResolver() {
        RelyingPartyRegistrationRepository effectiveRepository;
        if (isForceRedirectBindingLogout()) {
            // Wrap repository to override binding to REDIRECT for Vaadin compatibility
            effectiveRepository = createRelyingPartyRegistrationRepositoryWrapperWithRedirectBinding(relyingPartyRegistrationRepository);
        } else {
            effectiveRepository = relyingPartyRegistrationRepository;
        }

        return new OpenSaml5LogoutRequestResolver(effectiveRepository);
    }

    protected LogoutSuccessHandler createSamlLogoutSuccessHandler() {
        SamlVaadinLogoutSuccessHandler handler = new SamlVaadinLogoutSuccessHandler(createSamlLogoutRequestResolver());
        handler.setLogoutSuccessUrl(samlProperties.getLogoutSuccessUrl());
        return handler;
    }

    /**
     * Wraps the repository so that resolved registrations use the REDIRECT binding for single logout. The
     * SingleLogoutService location is kept as is, which is correct for identity providers declaring both
     * bindings on the same URL (e.g. Keycloak). If the identity provider uses a separate URL for the redirect
     * binding, configure the {@code single-logout} URL and binding of the registration explicitly and disable
     * {@code jmix.saml.force-redirect-binding-logout}.
     */
    protected RelyingPartyRegistrationRepository createRelyingPartyRegistrationRepositoryWrapperWithRedirectBinding(
            RelyingPartyRegistrationRepository originalRepository
    ) {
        return new RelyingPartyRegistrationRepository() {
            @Override
            @Nullable
            public RelyingPartyRegistration findByRegistrationId(String registrationId) {
                return overrideLogoutBinding(originalRepository.findByRegistrationId(registrationId));
            }

            @Override
            @Nullable
            public RelyingPartyRegistration findUniqueByAssertingPartyEntityId(String entityId) {
                return overrideLogoutBinding(originalRepository.findUniqueByAssertingPartyEntityId(entityId));
            }

            @Nullable
            private RelyingPartyRegistration overrideLogoutBinding(@Nullable RelyingPartyRegistration original) {
                if (original == null) {
                    return null;
                }

                // Clone registration and override SingleLogoutService binding to REDIRECT
                log.debug("Overriding SAML logout binding to REDIRECT for registration: {}",
                        original.getRegistrationId());

                RelyingPartyRegistration.Builder builder = original.mutate();
                return builder.assertingPartyMetadata(party -> party
                        .singleLogoutServiceBinding(Saml2MessageBinding.REDIRECT)
                ).build();
            }
        };
    }

    protected boolean isForceRedirectBindingLogout() {
        return samlProperties.isForceRedirectBindingLogout();
    }

    protected void initNavigationAccessControlLoginView(HttpSecurity http, String loginView) {
        if (Strings.isNullOrEmpty(loginView)) {
            return;
        }

        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        applicationContext.getBeanProvider(NavigationAccessControl.class)
                .ifAvailable(accessControl -> accessControl.setLoginView(loginView));
    }

    protected Converter<OpenSaml5AuthenticationProvider.ResponseToken, ? extends AbstractAuthenticationToken> createSamlAuthConverter(
            SamlUserMapper<?> samlUserMapper) {
        return new SamlResponseAuthenticationConverter(samlUserMapper);
    }
}
