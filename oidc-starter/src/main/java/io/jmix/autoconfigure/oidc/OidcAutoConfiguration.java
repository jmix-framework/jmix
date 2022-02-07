/*
 * Copyright 2020 Haulmont.
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

package io.jmix.autoconfigure.oidc;

import io.jmix.core.JmixOrder;
import io.jmix.oidc.OidcConfiguration;
import io.jmix.oidc.OidcProperties;
import io.jmix.oidc.claimsmapper.DefaultOidcClaimsMapper;
import io.jmix.oidc.claimsmapper.DefaultJmixOidcUserService;
import io.jmix.oidc.claimsmapper.JmixOidcUserService;
import io.jmix.oidc.claimsmapper.OidcClaimsMapper;
import io.jmix.oidc.user.OidcUserDetails;
import io.jmix.oidc.usermapper.OidcSimpleUserMapper;
import io.jmix.oidc.usermapper.OidcUserMapper;
import io.jmix.security.SecurityConfigurers;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.*;

@Configuration
@Import({OidcConfiguration.class})
@ConditionalOnProperty(name = "jmix.oidc.use-default-configuration", matchIfMissing = true)
public class OidcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JmixOidcUserService.class)
    public JmixOidcUserService oidcUserService(OidcUserMapper oidcUserMapper) {
        return new DefaultJmixOidcUserService(oidcUserMapper);
    }

    @Bean
    @ConditionalOnMissingBean(OidcClaimsMapper.class)
    @ConditionalOnBean(ResourceRoleRepository.class)
    public OidcClaimsMapper claimsToGrantedAuthoritiesMapper(ResourceRoleRepository resourceRoleRepository,
                                                             RowLevelRoleRepository rowLevelRoleRepository,
                                                             OidcProperties oidcProperties) {
        return new DefaultOidcClaimsMapper(resourceRoleRepository, rowLevelRoleRepository, oidcProperties);
    }

    @Bean
    @ConditionalOnMissingBean(OidcUserMapper.class)
    public OidcUserMapper userMapper(OidcClaimsMapper claimsToGrantedAuthoritiesMapper) {
        return new OidcSimpleUserMapper(claimsToGrantedAuthoritiesMapper);
    }

    /**
     * Configures UI endpoint protection
     */
    @EnableWebSecurity
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
    public static class OAuth2LoginSecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Autowired
        private JmixOidcUserService jmixOidcUserService;

        @Autowired
        private ClientRegistrationRepository clientRegistrationRepository;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .oauth2Login()
                    .userInfoEndpoint()
                    .oidcUserService(jmixOidcUserService)
                    .and()
                    //todo build UserDetails that contains locale
//                    .authenticationDetailsSource()
                    .and()
                    .logout()
                    .logoutSuccessHandler(oidcLogoutSuccessHandler());

            http.csrf().disable();
        }

        protected OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
            OidcClientInitiatedLogoutSuccessHandler successHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
            successHandler.setPostLogoutRedirectUri("{baseUrl}");
            return successHandler;
        }
    }

    /**
     * Configures API endpoints (REST, GraphQL, etc.) protection. Invocations to these resources require a bearer token
     * in the request header.
     */
    @EnableWebSecurity
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 90)
    public static class OAuth2ResourceServerConfiguration extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.apply(SecurityConfigurers.apiSecurity())
                    .and()
                    .oauth2ResourceServer()
                    .jwt();
        }
    }

    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationConverter.class)
    public JwtAuthenticationConverter jwtAuthenticationConverter(OidcClaimsMapper grantedAuthoritiesMapper) {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<? extends GrantedAuthority> mappedAuthorities = grantedAuthoritiesMapper.toGrantedAuthorities(jwt.getClaims());
            return new ArrayList<>(mappedAuthorities);
        });
        return jwtAuthenticationConverter;
    }
}