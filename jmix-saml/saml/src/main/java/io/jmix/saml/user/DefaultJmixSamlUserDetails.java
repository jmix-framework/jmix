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

package io.jmix.saml.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@link JmixSamlUserDetails} that delegates some method invocations to the
 * wrapped {@code Saml2AuthenticatedPrincipal}.
 */
public class DefaultJmixSamlUserDetails implements JmixSamlUserDetails, HasSamlPrincipalDelegate {

    private Collection<? extends GrantedAuthority> authorities;

    private Saml2AuthenticatedPrincipal delegate;

    @Override
    public String getName() {
        return delegate.getName();
    }

    public Saml2AuthenticatedPrincipal getDelegate() {
        return delegate;
    }

    public void setDelegate(Saml2AuthenticatedPrincipal delegate) {
        this.delegate = delegate;
    }

    @Override
    public Map<String, List<Object>> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return getName();
    }

    @Override
    public String getRelyingPartyRegistrationId() {
        return delegate.getRelyingPartyRegistrationId();
    }

    @Override
    public List<String> getSessionIndexes() {
        return delegate.getSessionIndexes();
    }

    /**
     * Equality is based on the username, like in the Spring Security {@code User} class. It is required for
     * {@code SessionRegistryImpl} to group sessions of the same user, otherwise the maximum sessions per user
     * limit is not enforced.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof DefaultJmixSamlUserDetails other
                && Objects.equals(getUsername(), other.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUsername());
    }
}
