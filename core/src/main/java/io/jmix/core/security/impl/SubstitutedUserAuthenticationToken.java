/*
 * Copyright 2021 Haulmont.
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

package io.jmix.core.security.impl;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Objects;

/**
 * Authentication object used by the user substitution mechanism.
 */
public class SubstitutedUserAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 8888961947349593368L;

    private final Object principal;

    private final Object credentials = "";
    private final Object substitutedPrincipal;

    /**
     * This constructor can be safely used by any code that wishes to create a
     * <code>SubstitutedUserAuthenticationToken</code>, as the {@link #isAuthenticated()}
     * will return <code>false</code>.
     *
     * @param originalToken        token of current user
     * @param substitutedPrincipal {@code userName} of user that should be substituted
     */
    public SubstitutedUserAuthenticationToken(Authentication originalToken, Object substitutedPrincipal) {
        this(originalToken, substitutedPrincipal, originalToken.getAuthorities());
        setAuthenticated(false);
    }


    /**
     * This constructor should only be used by <code>AuthenticationManager</code> or
     * <code>AuthenticationProvider</code> implementations that are satisfied with
     * producing a trusted (i.e. {@link #isAuthenticated()} = <code>true</code>)
     * authentication token.
     *
     * @param originalToken        to take current principal and details from
     * @param substitutedPrincipal principal whose authorities are used
     * @param authorities          authorities
     */
    public SubstitutedUserAuthenticationToken(Authentication originalToken,
                                              Object substitutedPrincipal,
                                              Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = originalToken.getPrincipal();
        this.substitutedPrincipal = substitutedPrincipal;
        super.setAuthenticated(true);
        setDetails(originalToken.getDetails());
    }


    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }
        super.setAuthenticated(false);
    }

    public Object getSubstitutedPrincipal() {
        return substitutedPrincipal;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && Objects.equals(this.substitutedPrincipal, ((SubstitutedUserAuthenticationToken) obj).substitutedPrincipal);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.getSubstitutedPrincipal().hashCode();
    }
}
