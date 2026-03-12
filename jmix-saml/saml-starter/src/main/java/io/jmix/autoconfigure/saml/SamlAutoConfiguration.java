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

package io.jmix.autoconfigure.saml;

import io.jmix.saml.SamlConfiguration;
import io.jmix.saml.SamlProperties;
import io.jmix.saml.SamlVaadinWebSecurity;
import io.jmix.saml.filter.SamlVaadinSecurityFilterChainCustomizer;
import io.jmix.saml.mapper.role.DefaultSamlAssertionRolesMapper;
import io.jmix.saml.mapper.role.SamlAssertionRolesMapper;
import io.jmix.saml.mapper.user.DefaultSamlUserMapper;
import io.jmix.saml.mapper.user.SamlUserMapper;
import io.jmix.security.configurer.SecurityFilterChainCustomizer;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.security.util.ClientDetailsSourceSupport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@AutoConfiguration
@Import({SamlConfiguration.class})
@ConditionalOnProperty(name = "jmix.saml.use-default-configuration", matchIfMissing = true)
public class SamlAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SamlAssertionRolesMapper.class)
    @ConditionalOnBean(ResourceRoleRepository.class)
    public SamlAssertionRolesMapper claimsRoleMapper(ResourceRoleRepository resourceRoleRepository,
                                                     RowLevelRoleRepository rowLevelRoleRepository,
                                                     SamlProperties samlProperties,
                                                     RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        DefaultSamlAssertionRolesMapper mapper = new DefaultSamlAssertionRolesMapper(
                resourceRoleRepository, rowLevelRoleRepository, roleGrantedAuthorityUtils
        );
        mapper.setRolesAttributeName(samlProperties.getDefaultSamlAssertionRolesMapper().getRolesAssertionAttribute());
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean(SamlUserMapper.class)
    public SamlUserMapper userMapper(SamlAssertionRolesMapper assertionRolesMapper) {
        return new DefaultSamlUserMapper(assertionRolesMapper);
    }

    @EnableWebSecurity
    @ConditionalOnProperty(value = "jmix.saml.use-default-ui-configuration", havingValue = "true", matchIfMissing = true)
    public static class DefaulSamlVaadinWebSecurity extends SamlVaadinWebSecurity {

        @Bean("saml_SamlVaadinSecurityFilterChainCustomizer")
        public SecurityFilterChainCustomizer samlVaadinSecurityFilterChainCustomizer(ClientDetailsSourceSupport clientDetailsSourceSupport,
                                                                                     SamlProperties samlProperties) {
            return new SamlVaadinSecurityFilterChainCustomizer(clientDetailsSourceSupport, samlProperties);
        }
    }
}
