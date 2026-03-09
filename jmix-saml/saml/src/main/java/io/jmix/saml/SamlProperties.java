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

package io.jmix.saml;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.saml")
public class SamlProperties {

    boolean forceRedirectBindingLogout;
    boolean synchronizeRoleAssignments;
    boolean useDefaultConfiguration;
    boolean useDefaultUiConfiguration;

    DefaultSamlAssertionRolesMapperConfig defaultSamlAssertionRolesMapper;

    public SamlProperties(@DefaultValue("true") boolean forceRedirectBindingLogout,
                          @DefaultValue("true") boolean synchronizeRoleAssignments,
                          @DefaultValue("true") boolean useDefaultConfiguration,
                          @DefaultValue("true") boolean useDefaultUiConfiguration,
                          @DefaultValue DefaultSamlAssertionRolesMapperConfig defaultSamlAssertionRolesMapper) {
        this.forceRedirectBindingLogout = forceRedirectBindingLogout;
        this.synchronizeRoleAssignments = synchronizeRoleAssignments;
        this.useDefaultConfiguration = useDefaultConfiguration;
        this.useDefaultUiConfiguration = useDefaultUiConfiguration;

        this.defaultSamlAssertionRolesMapper = defaultSamlAssertionRolesMapper;
    }

    public boolean isForceRedirectBindingLogout() {
        return forceRedirectBindingLogout;
    }

    public boolean isSynchronizeRoleAssignments() {
        return synchronizeRoleAssignments;
    }

    public boolean isUseDefaultConfiguration() {
        return useDefaultConfiguration;
    }

    public boolean isUseDefaultUiConfiguration() {
        return useDefaultUiConfiguration;
    }

    public DefaultSamlAssertionRolesMapperConfig getDefaultSamlAssertionRolesMapper() {
        return defaultSamlAssertionRolesMapper;
    }

    public static class DefaultSamlAssertionRolesMapperConfig {

        String rolesAssertionAttribute;

        public DefaultSamlAssertionRolesMapperConfig(@DefaultValue("Role") String rolesAssertionAttribute) {
            this.rolesAssertionAttribute = rolesAssertionAttribute;
        }

        public String getRolesAssertionAttribute() {
            return rolesAssertionAttribute;
        }
    }
}
