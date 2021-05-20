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

package io.jmix.multitenancy;

import io.jmix.multitenancy.security.role.DefaultTenantRole;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.multitenancy")
@ConstructorBinding
public class MultitenancyProperties {

    private final String adminUser;
    private final String anonymousUser;
    private final boolean loginByTenantParamEnabled;
    private final String defaultTenantRoleCode;

    public MultitenancyProperties(String adminUser,
                                  String anonymousUser,
                                  boolean loginByTenantParamEnabled,
                                  @DefaultValue(DefaultTenantRole.CODE) String defaultTenantRoleCode) {
        this.adminUser = adminUser;
        this.anonymousUser = anonymousUser;
        this.loginByTenantParamEnabled = loginByTenantParamEnabled;
        this.defaultTenantRoleCode = defaultTenantRoleCode;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public String getAnonymousUser() {
        return anonymousUser;
    }

    public boolean isLoginByTenantParamEnabled() {
        return loginByTenantParamEnabled;
    }

    public String getDefaultTenantRoleCode() {
        return defaultTenantRoleCode;
    }
}
