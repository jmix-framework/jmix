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
import io.jmix.oidc.claimsmapper.ClaimsRolesMapper;
import io.jmix.oidc.claimsmapper.DefaultClaimsRolesMapper;
import io.jmix.oidc.jwt.JmixJwtAuthenticationConverter;
import io.jmix.oidc.resourceserver.OidcResourceServerLastSecurityFilter;
import io.jmix.oidc.userinfo.DefaultJmixOidcUserService;
import io.jmix.oidc.userinfo.JmixOidcUserService;
import io.jmix.oidc.usermapper.DefaultOidcUserMapper;
import io.jmix.oidc.usermapper.OidcUserMapper;
import io.jmix.security.SecurityConfigurers;
import io.jmix.security.configurer.SessionManagementConfigurer;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

@AutoConfiguration
@Import({OidcConfiguration.class})
@ConditionalOnProperty(name = "jmix.oidc.use-default-configuration", matchIfMissing = true)
public class OidcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JmixOidcUserService.class)
    public JmixOidcUserService oidcUserService(OidcUserMapper oidcUserMapper) {
        return new DefaultJmixOidcUserService(oidcUserMapper);
    }

    @Bean
    @ConditionalOnMissingBean(ClaimsRolesMapper.class)
    @ConditionalOnBean(ResourceRoleRepository.class)
    public ClaimsRolesMapper claimsRoleMapper(ResourceRoleRepository resourceRoleRepository,
                                              RowLevelRoleRepository rowLevelRoleRepository,
                                              OidcProperties oidcProperties) {
        DefaultClaimsRolesMapper mapper = new DefaultClaimsRolesMapper(resourceRoleRepository, rowLevelRoleRepository);
        mapper.setRolesClaimName(oidcProperties.getDefaultClaimsRolesMapper().getRolesClaimName());
        mapper.setResourceRolePrefix(oidcProperties.getDefaultClaimsRolesMapper().getResourceRolePrefix());
        mapper.setRowLevelRolePrefix(oidcProperties.getDefaultClaimsRolesMapper().getRowLevelRolePrefix());
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean(OidcUserMapper.class)
    public OidcUserMapper userMapper(ClaimsRolesMapper claimsRolesMapper) {
        return new DefaultOidcUserMapper(claimsRolesMapper);
    }

    /**
     * Configures UI endpoint protection
     */
    @EnableWebSecurity
    @ConditionalOnProperty(value = "jmix.oidc.use-default-ui-configuration", havingValue = "true", matchIfMissing = true)
    public static class OAuth2LoginSecurityConfiguration {

        public static final String SECURITY_CONFIGURER_QUALIFIER = "oidc-login";

        @Bean("oidc_OAuthLoginSecurityFilterChain")
        @Order(JmixOrder.HIGHEST_PRECEDENCE + 200)
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                       JmixOidcUserService jmixOidcUserService,
                                                       ClientRegistrationRepository clientRegistrationRepository) throws Exception {
            http.authorizeHttpRequests(authorize -> {
                        authorize
                                //if we don't allow /vaadinServlet/PUSH URL the Session Expired toolbox won't
                                //be shown in the web browser
                                .antMatchers("/vaadinServlet/PUSH/**").permitAll()
                                .anyRequest().authenticated();
                    })
                    .oauth2Login(oauth2Login -> {
                        oauth2Login.userInfoEndpoint(userInfoEndpoint -> {
                            userInfoEndpoint.oidcUserService(jmixOidcUserService);
                        });
                    })
                    .logout(logout -> {
                        logout.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository));
                    })
                    .csrf(csrf -> {
                        csrf.disable();
                    })
                    .headers(headers -> {
                        headers.frameOptions(frameOptions -> {
                            frameOptions.sameOrigin();
                        });
                    });
            http.apply(new SessionManagementConfigurer());
            SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
            return http.build();
        }

        protected OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
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
    @ConditionalOnProperty(value = "jmix.oidc.use-default-jwt-configuration", havingValue = "true", matchIfMissing = true)
    public static class OAuth2ResourceServerConfiguration {

        public static final String SECURITY_CONFIGURER_QUALIFIER = "oidc-resource-server";

        @Bean("oidc_JwtSecurityFilterChain")
        @Order(JmixOrder.HIGHEST_PRECEDENCE + 150)
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                       JmixJwtAuthenticationConverter jmixJwtAuthenticationConverter,
                                                       ApplicationEventPublisher applicationEventPublisher) throws Exception {
            http.oauth2ResourceServer(resourceServer -> {
                resourceServer.jwt(jwt -> {
                    jwt.jwtAuthenticationConverter(jmixJwtAuthenticationConverter);
                });
            });
            http.apply(SecurityConfigurers.apiSecurity());

            OidcResourceServerLastSecurityFilter lastSecurityFilter = new OidcResourceServerLastSecurityFilter(applicationEventPublisher);
            http.addFilterAfter(lastSecurityFilter, FilterSecurityInterceptor.class);
            SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
            return http.build();
        }

        @Bean
        @ConditionalOnMissingBean(JmixJwtAuthenticationConverter.class)
        public JmixJwtAuthenticationConverter jmixJwtAuthenticationConverter(OidcUserMapper oidcUserMapper, OidcProperties oidcProperties) {
            return new JmixJwtAuthenticationConverter(oidcUserMapper, oidcProperties);
        }
    }
}