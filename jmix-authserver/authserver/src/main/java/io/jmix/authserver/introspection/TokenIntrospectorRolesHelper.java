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

package io.jmix.authserver.introspection;

import io.jmix.authserver.AuthServerProperties;
import io.jmix.authserver.roleassignment.RegisteredClientRoleAssignment;
import io.jmix.authserver.roleassignment.RegisteredClientRoleAssignmentRepository;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The class is used for converting a list of role codes specified for the client in properties file into the list of
 * {@link GrantedAuthority}.
 *
 * @see AuthServerProperties
 */
@Component("authsr_TokenIntrospectorRolesHelper")
public class TokenIntrospectorRolesHelper {

    private static final Logger log = LoggerFactory.getLogger(TokenIntrospectorRolesHelper.class);

    private RegisteredClientRoleAssignmentRepository clientRoleAssignmentRepository;

    private RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    public TokenIntrospectorRolesHelper(RegisteredClientRoleAssignmentRepository clientRoleAssignmentRepository,
                                        RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        this.clientRoleAssignmentRepository = clientRoleAssignmentRepository;
        this.roleGrantedAuthorityUtils = roleGrantedAuthorityUtils;
    }

    /**
     * Converts a list of roles specified for the client in properties file into the list of
     * {@link GrantedAuthority}.
     *
     * @param clientId a client id
     * @return a list of RoleGrantedAuthority
     */
    public List<GrantedAuthority> getClientGrantedAuthorities(String clientId) {
        Collection<RegisteredClientRoleAssignment> roleAssignments = clientRoleAssignmentRepository.findByClientId(clientId);

        List<GrantedAuthority> resourceRoleAuthorities = roleAssignments.stream()
                .flatMap(roleAssignment -> roleAssignment.resourceRoles().stream())
                .map(roleCode -> roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(roleCode))
                .toList();

        List<GrantedAuthority> rowLevelRolesAuthorities = roleAssignments.stream()
                .flatMap(roleAssignment -> roleAssignment.rowLevelRoles().stream())
                .map(roleCode -> roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(roleCode))
                .toList();

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.addAll(resourceRoleAuthorities);
        authorities.addAll(rowLevelRolesAuthorities);

        return authorities;
    }
}
