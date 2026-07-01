/*
 * Copyright 2024 Haulmont.
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

package io.jmix.oidc;

import com.vaadin.flow.spring.security.UidlRedirectStrategy;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import io.jmix.securityflowui.security.AbstractFlowuiWebSecurity;
import io.jmix.oidc.userinfo.JmixOidcUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

/**
 * Provides Vaadin security to the project. Configures authentication using the OAuth 2.0 or OpenID Connect provider.
 */
public class OidcVaadinWebSecurity extends AbstractFlowuiWebSecurity {

    private static final String DEFAULT_LOGIN_PAGE_URL = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;

    protected JmixOidcUserService jmixOidcUserService;
    protected OidcProperties oidcProperties;
    protected ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    public void setJmixOidcUserService(JmixOidcUserService jmixOidcUserService) {
        this.jmixOidcUserService = jmixOidcUserService;
    }

    @Autowired
    public void setOidcProperties(OidcProperties oidcProperties) {
        this.oidcProperties = oidcProperties;
    }

    @Autowired
    public void setClientRegistrationRepository(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Override
    protected void configureJmixSpecifics(HttpSecurity http) throws Exception {
        super.configureJmixSpecifics(http);
        http.oauth2Login(oauth2Login ->
                oauth2Login.userInfoEndpoint(userInfoEndpoint ->
                        userInfoEndpoint.oidcUserService(jmixOidcUserService)))
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler()));
    }

    @Override
    protected void configureVaadinSpecifics(HttpSecurity http) {
        // Keep Flow navigation aligned with Spring Security: one client goes directly to
        // the provider authorization endpoint, several clients use the generated /login page.
        http.with(VaadinSecurityConfigurer.vaadin(),
                configurer -> configurer.oauth2LoginPage(getOidcLoginUrl(), oidcProperties.getPostLogoutRedirectUri()));
    }

    /**
     * Mirrors the default Spring Security OAuth2 login behavior so that Vaadin navigation
     * access control points unauthenticated users to the same login URL.
     */
    protected String getOidcLoginUrl() {
        if (clientRegistrationRepository instanceof Iterable<?> clientRegistrations) {
            String loginUrl = null;

            for (Object candidate : clientRegistrations) {
                if (!(candidate instanceof ClientRegistration clientRegistration)
                        || !AuthorizationGrantType.AUTHORIZATION_CODE.equals(clientRegistration.getAuthorizationGrantType())) {
                    continue;
                }

                if (loginUrl != null) {
                    return DEFAULT_LOGIN_PAGE_URL;
                }

                loginUrl = "/oauth2/authorization/" + clientRegistration.getRegistrationId();
            }

            if (loginUrl != null) {
                return loginUrl;
            }
        }

        return DEFAULT_LOGIN_PAGE_URL;
    }

    protected OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler successHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        successHandler.setRedirectStrategy(new UidlRedirectStrategy());
        successHandler.setPostLogoutRedirectUri(oidcProperties.getPostLogoutRedirectUri());
        return successHandler;
    }
}
