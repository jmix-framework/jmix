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

package io.jmix.core.security.user;

import io.jmix.core.security.ServiceUserProvider;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A default implementation of {@link ServiceUserProvider}. Extend the class and override the
 * {@link #getSystemUserAuthorities()} and {@link #getAnonymousUserAuthorities()} methods to define service user
 * authorities.
 */
public class DefaultServiceUserProvider implements ServiceUserProvider<UserDetails> {

    //todo MG not UserDetails, just Object
    protected UserDetails systemUser;

    protected UserDetails anonymousUser;

    @PostConstruct
    public void init() {
        this.systemUser = createSystemUser();
        this.anonymousUser = createAnonymousUser();
    }

    protected UserDetails createSystemUser() {
        return User.builder()
                .username("system")
                .password(RandomStringUtils.randomAlphanumeric(10))
                .authorities(getSystemUserAuthorities())
                .build();
    }

    protected Collection<GrantedAuthority> getSystemUserAuthorities() {
        return new ArrayList<>();
    }

    protected UserDetails createAnonymousUser() {
        return User.builder()
                .username("anonymous")
                .password(RandomStringUtils.randomAlphanumeric(10))
                .authorities(getAnonymousUserAuthorities())
                .build();
    }

    protected Collection<GrantedAuthority> getAnonymousUserAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public UserDetails getSystemUser() {
        return systemUser;
    }

    @Override
    public UserDetails getAnonymousUser() {
        return anonymousUser;
    }
}
