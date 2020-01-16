/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.security;

import io.jmix.core.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserSession implements Authentication {

    private static final long serialVersionUID = -7134190493600163922L;

    protected UUID id = UUID.randomUUID();

    protected User user;

    protected Authentication authentication;

    protected ClientDetails clientDetails = ClientDetails.UNKNOWN;

    protected Locale locale = Locale.getDefault(); // todo user locale

    protected TimeZone timeZone; // todo user timezone

    protected List<String> roles = new ArrayList<>();

    protected Map<String, Serializable> attributes = new ConcurrentHashMap<>();

    public UserSession(Authentication authentication) {
        this.authentication = authentication;
        if (authentication.getPrincipal() instanceof User) {
            user = (User) authentication.getPrincipal();
        } else {
            throw new UnsupportedOperationException("UserSession does not support principal of type "
                    + authentication.getPrincipal().getClass().getName());
        }
    }

    protected UserSession() {
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public ClientDetails getClientDetails() {
        return clientDetails;
    }

    public void setClientDetails(ClientDetails clientDetails) {
        this.clientDetails = clientDetails;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public List<String> getRoles() {
        return roles;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
    }

    public void setAttribute(String name, Serializable value) {
        attributes.put(name, value);
    }

    public Collection<String> getAttributeNames() {
        return new ArrayList<>(attributes.keySet());
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id + "," +
                "user=" + user.getUsername() +
                '}';
    }

    @Override
    public String getName() {
        return authentication.getName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authentication.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return authentication.getCredentials();
    }

    @Override
    public Object getDetails() {
        return authentication.getDetails();
    }

    @Override
    public Object getPrincipal() {
        return authentication.getPrincipal();
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    }

    @Nullable
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * INTERNAL
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
}
