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

package io.jmix.autoconfigure.ldap;

import io.jmix.ldap.LdapActiveDirectorySecurityConfiguration;
import io.jmix.ldap.LdapConfiguration;
import io.jmix.ldap.LdapSecurityConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.StandardSecurityConfiguration;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@AutoConfiguration
@Import({SecurityConfiguration.class, LdapConfiguration.class})
public class LdapAutoConfiguration {

    @EnableWebSecurity
    @Conditional(OnDefaultLdapConfigurationCondition.class)
    @ConditionalOnMissingBean({StandardSecurityConfiguration.class, LdapSecurityConfiguration.class})
    public static class DefaultLdapSecurityConfiguration extends LdapSecurityConfiguration {
    }

    @EnableWebSecurity
    @Conditional(OnActiveDirectoryLdapConfigurationCondition.class)
    @ConditionalOnMissingBean({StandardSecurityConfiguration.class, LdapActiveDirectorySecurityConfiguration.class})
    public static class DefaultLdapActiveDirectorySecurityConfiguration extends LdapActiveDirectorySecurityConfiguration {
    }

    private static class OnDefaultLdapConfigurationCondition extends AllNestedConditions {

        OnDefaultLdapConfigurationCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(prefix = "jmix.ldap", name = "enabled", havingValue = "true", matchIfMissing = true)
        static class ldapEnabled {
        }

        @ConditionalOnProperty(prefix = "jmix.ldap", name = "use-active-directory-configuration", havingValue = "false", matchIfMissing = true)
        static class useActiveDirectoryConfiguration {
        }
    }

    private static class OnActiveDirectoryLdapConfigurationCondition extends AllNestedConditions {

        OnActiveDirectoryLdapConfigurationCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(prefix = "jmix.ldap", name = "enabled", havingValue = "true", matchIfMissing = true)
        static class ldapEnabled {
        }

        @ConditionalOnProperty(prefix = "jmix.ldap", name = "use-active-directory-configuration", havingValue = "true")
        static class useActiveDirectoryConfiguration {
        }
    }
}