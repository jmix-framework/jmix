/*
 * Copyright 2022 Haulmont.
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

package io.jmix.authorizationserver.introspection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Token introspector that queries the backing store of tokens (authorization service) and fills the authenticated
 * principal authorities with proper roles depending on authorization grant type:
 *
 * <ul>
 *     <li>For AUTHORIZATION_CODE grant type roles of authenticated user are used</li>
 *     <li>For CLIENT_CREDENTIALS grant type roles of the user with the name equal to the client id are used</li>
 * </ul>
 *
 *
 */
public class AuthorizationServiceOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private OAuth2AuthorizationService authorizationService;

    private UserDetailsService userDetailsService;

    public AuthorizationServiceOpaqueTokenIntrospector(OAuth2AuthorizationService authorizationService, UserDetailsService userDetailsService) {
        this.authorizationService = authorizationService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        OAuth2Authorization authorization = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
        if (authorization == null) {
            throw new BadOpaqueTokenException("Authorization for provided access token not found");
        }
        String principalName = authorization.getPrincipalName();
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if  (authorization.getAuthorizationGrantType() == AuthorizationGrantType.AUTHORIZATION_CODE) {
            Object principal = authorization.getAttribute(Principal.class.getCanonicalName());
            if  (principal instanceof Authentication) {
                principalName = ((Authentication) principal).getName();
                authorities.addAll(((Authentication) principal).getAuthorities());
            }
        } else if  (authorization.getAuthorizationGrantType() == AuthorizationGrantType.CLIENT_CREDENTIALS) {
            principalName = authorization.getPrincipalName();
            try {
                UserDetails user = userDetailsService.loadUserByUsername(principalName);
                authorities.addAll(user.getAuthorities());
            } catch (UsernameNotFoundException e) {
                throw new BadOpaqueTokenException("User " + principalName + " not found");
            }
        }
        return new UserDetailsOAuth2AuthenticatedPrincipal(principalName, authorization.getAttributes(), authorities);
    }
}
