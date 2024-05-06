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

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import io.jmix.oidc.userinfo.JmixOidcUserService;
import io.jmix.security.configurer.AnonymousConfigurer;
import io.jmix.security.configurer.SessionManagementConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Provides Vaadin security to the project. Configures authentication using the OAuth 2.0 or OpenID Connect provider.
 */
public class OidcVaadinWebSecurity extends VaadinWebSecurity {

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
    protected void configure(HttpSecurity http) throws Exception {
        //apply Jmix configuration
        configureJmixSpecifics(http);

        //apply Vaadin configuration
        super.configure(http);
    }

    protected void configureJmixSpecifics(HttpSecurity http) throws Exception {
        http.with(new AnonymousConfigurer(), Customizer.withDefaults());
        http.with(new SessionManagementConfigurer(), Customizer.withDefaults());

        http.oauth2Login(oauth2Login -> {
                    oauth2Login.userInfoEndpoint(userInfoEndpoint -> {
                        userInfoEndpoint.oidcUserService(jmixOidcUserService);
                    });
                })
                .logout(logout -> {
                    logout.logoutSuccessHandler(oidcLogoutSuccessHandler());
                });
    }

    protected OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler successHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        successHandler.setPostLogoutRedirectUri(oidcProperties.getPostLogoutRedirectUri());
        return successHandler;
    }

    /**
     * Temporary workaround until https://github.com/vaadin/flow/issues/19075 is fixed
     */
    @Override
    protected void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.ignoring().requestMatchers(new AntPathRequestMatcher("/VAADIN/push/**"));
    }
}
