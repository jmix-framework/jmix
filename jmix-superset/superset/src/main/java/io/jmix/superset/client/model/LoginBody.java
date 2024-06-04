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

package io.jmix.superset.client.model;

import org.springframework.lang.Nullable;

import java.io.Serializable;

public class LoginBody implements Serializable {

    private String username;
    private String password;
    private String provider;
    private Boolean refresh;

    @Nullable
    public String getUsername() {
        return username;
    }

    public void setUsername(@Nullable String username) {
        this.username = username;
    }

    public LoginBody withUsername(@Nullable String username) {
        setUsername(username);
        return this;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    public LoginBody withPassword(@Nullable String password) {
        setPassword(password);
        return this;
    }

    @Nullable
    public String getProvider() {
        return provider;
    }

    public void setProvider(@Nullable String provider) {
        this.provider = provider;
    }

    public LoginBody withProvider(@Nullable String provider) {
        setProvider(provider);
        return this;
    }

    @Nullable
    public Boolean getRefresh() {
        return refresh;
    }

    public void setRefresh(@Nullable Boolean refresh) {
        this.refresh = refresh;
    }

    public LoginBody withRefresh(@Nullable Boolean refresh) {
        setRefresh(refresh);
        return this;
    }
}
