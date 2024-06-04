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

package io.jmix.authserver.principal;

import io.jmix.authserver.introspection.UserDetailsOAuth2AuthenticatedPrincipal;
import io.jmix.core.security.AuthenticationPrincipalResolver;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * A {@link AuthenticationPrincipalResolver} that makes the {@link CurrentAuthentication#getUser()} method return an
 * instance of the actual user class (usually a JPA entity defined in the application) if authenticated using access
 * token.
 *
 * @see CurrentAuthentication#getUser()
 */
public class AuthServerAuthenticationPrincipalResolver implements AuthenticationPrincipalResolver {

    @Override
    public boolean supports(Authentication authentication) {
        if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
            return bearerTokenAuthentication.getPrincipal() instanceof UserDetailsOAuth2AuthenticatedPrincipal;
        }
        return false;
    }

    @Override
    public Object resolveAuthenticationPrincipal(Authentication authentication) {
        if (authentication instanceof BearerTokenAuthentication auth) {
            if (auth.getPrincipal() instanceof UserDetailsOAuth2AuthenticatedPrincipal principal) {
                Object nestedPrincipal = principal.getAttribute(Principal.class.getName());
                if (nestedPrincipal instanceof UsernamePasswordAuthenticationToken token) {
                    return token.getPrincipal();
                }
            }
        }
        return authentication.getPrincipal();
    }
}
