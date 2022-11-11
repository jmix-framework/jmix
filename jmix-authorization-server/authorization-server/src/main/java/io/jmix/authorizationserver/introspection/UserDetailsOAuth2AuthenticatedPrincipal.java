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

import io.jmix.security.authentication.JmixUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * Instances of this class are set as authentication principal to the security context when resource server API are
 * accessed
 */
public class UserDetailsOAuth2AuthenticatedPrincipal implements OAuth2AuthenticatedPrincipal, JmixUserDetails {

    protected Map<String, Object> attributes;

    protected Collection<? extends GrantedAuthority> authorities;

    protected String username;

    public UserDetailsOAuth2AuthenticatedPrincipal(String username, Map<String, Object> attributes, Collection<GrantedAuthority> authorities) {
        this.username = username;
        this.attributes = attributes;
        this.authorities = authorities;
    }

    @Override
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    @Nullable
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getName() {
        return this.username;
    }
}
