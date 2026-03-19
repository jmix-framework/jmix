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

import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.RowLevelRoleRepository;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * {@link SamlAssertionRolesMapper} that can be used as super-class for your own {@link SamlAssertionRolesMapper}.
 * The child classes must override the {@link #getResourceRolesCodes(Assertion)} and {@link #getRowLevelRoleCodes(Assertion)} methods.
 * The behavior for finding roles with given codes and transforming them into a collection of {@link GrantedAuthority}
 * is already implemented in the current class.
 */
public abstract class BaseSamlAssertionRolesMapper implements SamlAssertionRolesMapper {

    private static final Logger log = getLogger(BaseSamlAssertionRolesMapper.class);

    //todo [IVGA] field injection
    protected final RowLevelRoleRepository rowLevelRoleRepository;
    protected final ResourceRoleRepository resourceRoleRepository;
    protected final RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    public BaseSamlAssertionRolesMapper(RowLevelRoleRepository rowLevelRoleRepository,
                                        ResourceRoleRepository resourceRoleRepository,
                                        RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        this.rowLevelRoleRepository = rowLevelRoleRepository;
        this.resourceRoleRepository = resourceRoleRepository;
        this.roleGrantedAuthorityUtils = roleGrantedAuthorityUtils;
    }

    @Override
    public Collection<ResourceRole> toResourceRoles(Assertion assertion) {
        Collection<ResourceRole> roles = new ArrayList<>();
        Collection<String> jmixRoleCodes = getResourceRolesCodes(assertion);
        for (String jmixRoleCode : jmixRoleCodes) {
            ResourceRole jmixRole = resourceRoleRepository.findRoleByCode(jmixRoleCode);
            if (jmixRole != null) {
                roles.add(jmixRole);
            } else {
                log.debug("Resource role {} not found", jmixRoleCode);
            }
        }
        return roles;
    }

    @Override
    public Collection<RowLevelRole> toRowLevelRoles(Assertion assertion) {
        Collection<RowLevelRole> roles = new ArrayList<>();
        Collection<String> jmixRoleCodes = getRowLevelRoleCodes(assertion);
        for (String jmixRoleCode : jmixRoleCodes) {
            RowLevelRole jmixRole = rowLevelRoleRepository.findRoleByCode(jmixRoleCode);
            if (jmixRole != null) {
                roles.add(jmixRole);
            } else {
                log.debug("Row-level role {} not found", jmixRoleCode);
            }
        }
        return roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> toGrantedAuthorities(Assertion assertion) {
        Stream<GrantedAuthority> resourceRoleAuthoritiesStream = toResourceRoles(assertion).stream()
                .map(roleGrantedAuthorityUtils::createResourceRoleGrantedAuthority);
        Stream<GrantedAuthority> rowLevelRoleAuthoritiesStream = toRowLevelRoles(assertion).stream()
                .map(roleGrantedAuthorityUtils::createRowLevelRoleGrantedAuthority);
        return Stream.concat(resourceRoleAuthoritiesStream, rowLevelRoleAuthoritiesStream).toList();
    }

    protected abstract Collection<String> getResourceRolesCodes(Assertion assertion);

    protected abstract Collection<String> getRowLevelRoleCodes(Assertion assertion);
}
