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

package io.jmix.ui.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.ui.login")
@ConstructorBinding
public class UiLoginProperties {

    boolean rememberMeEnabled;
    String defaultUser;
    String defaultPassword;
    boolean poweredByLinkVisible;

    public UiLoginProperties(
            @DefaultValue("true") boolean rememberMeEnabled,
            @DefaultValue("admin") String defaultUser,
            @DefaultValue("admin") String defaultPassword,
            @DefaultValue("true") boolean poweredByLinkVisible
    ) {
        this.rememberMeEnabled = rememberMeEnabled;
        this.defaultUser = defaultUser;
        this.defaultPassword = defaultPassword;
        this.poweredByLinkVisible = poweredByLinkVisible;
    }

    public boolean isRememberMeEnabled() {
        return rememberMeEnabled;
    }

    public String getDefaultUser() {
        return defaultUser;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public boolean isPoweredByLinkVisible() {
        return poweredByLinkVisible;
    }
}
