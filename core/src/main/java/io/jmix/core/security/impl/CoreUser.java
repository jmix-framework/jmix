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

package io.jmix.core.security.impl;

import io.jmix.core.entity.BaseUuidEntity;
import io.jmix.core.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class CoreUser extends BaseUuidEntity implements User {

    private static final long serialVersionUID = 2032149054729862959L;

    private String login;
    private String password;
    private String name;

    public CoreUser(String login, String password, String name) {
        this.login = login;
        this.password = password;
        this.name = name;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String getLoginLowerCase() {
        return login == null ? null : login.toLowerCase();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return getLoginLowerCase();
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
}
