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

package io.jmix.saml.mapper.role;

import io.jmix.saml.SamlProperties;
import io.jmix.saml.util.SamlAssertionUtils;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.RowLevelRoleRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Default implementation of {@link SamlAssertionRolesMapper} that takes role names from SAML assertion attribute and
 * transforms them to resource and row-level roles using role name prefixes.
 * <p>
 * Roles names are taken from a special assertion attribute. Attribute name is taken from the {@link #rolesAttributeName} property.
 * The default value is taken from the {@link SamlProperties.DefaultSamlAssertionRolesMapperConfig#getRolesAssertionAttribute()}
 * <p>
 * Role names from the assertion attribute are mapped to the resource and row-level roles using {@link #resourceRolePrefix}
 * and {@link #rowLevelRolePrefix} prefixes, e.g., if the {@code resourceRolePrefix} is "resource$" then SAML role with
 * the name "resource$system-full-access" will be mapped to Jmix role with the "system-full-access" code.
 * By default the prefixes are empty.
 */
public class DefaultSamlAssertionRolesMapper extends BaseSamlAssertionRolesMapper {

    private static final Logger log = getLogger(DefaultSamlAssertionRolesMapper.class);

    protected String rolesAttributeName = "Role";

    protected String resourceRolePrefix = "";
    protected String rowLevelRolePrefix = "";

    public DefaultSamlAssertionRolesMapper(ResourceRoleRepository resourceRoleRepository,
                                           RowLevelRoleRepository rowLevelRoleRepository,
                                           RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        super(rowLevelRoleRepository, resourceRoleRepository, roleGrantedAuthorityUtils);
    }

    @Override
    protected Collection<String> getResourceRolesCodes(Assertion assertion) {
        return getRolesCodes(assertion, resourceRolePrefix);
    }

    @Override
    protected Collection<String> getRowLevelRoleCodes(Assertion assertion) {
        return getRolesCodes(assertion, rowLevelRolePrefix);
    }

    protected Collection<String> getRolesCodes(Assertion assertion, String roleNamePrefix) {
        Map<String, List<Object>> assertionAttributes = SamlAssertionUtils.getAssertionAttributes(assertion);
        List<Object> rolesAssertionAttributes = assertionAttributes.get(getRolesAttributeName());
        if (CollectionUtils.isEmpty(rolesAssertionAttributes)) {
            return Collections.emptySet();
        } else {
            return rolesAssertionAttributes.stream()
                    .map(Object::toString)
                    .filter(roleName -> roleName.startsWith(roleNamePrefix))
                    .map(roleName -> roleName.substring(roleNamePrefix.length()))
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    public String getRolesAttributeName() {
        return rolesAttributeName;
    }

    public void setRolesAttributeName(String rolesAttributeName) {
        this.rolesAttributeName = rolesAttributeName;
    }

    public String getResourceRolePrefix() {
        return resourceRolePrefix;
    }

    public void setResourceRolePrefix(String resourceRolePrefix) {
        this.resourceRolePrefix = resourceRolePrefix;
    }

    public String getRowLevelRolePrefix() {
        return rowLevelRolePrefix;
    }

    public void setRowLevelRolePrefix(String rowLevelRolePrefix) {
        this.rowLevelRolePrefix = rowLevelRolePrefix;
    }
}
