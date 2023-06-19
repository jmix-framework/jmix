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
import io.jmix.security.authentication.RoleGrantedAuthority;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The class is used for converting a list of role codes specified for the client in properties file into the list of
 * {@link RoleGrantedAuthority}.
 *
 * @see AuthServerProperties
 */
@Component("authsr_TokenIntrospectorRolesHelper")
public class TokenIntrospectorRolesHelper {

    private static final Logger log = LoggerFactory.getLogger(TokenIntrospectorRolesHelper.class);

    private ResourceRoleRepository resourceRoleRepository;

    private RowLevelRoleRepository rowLevelRoleRepository;

    private RegisteredClientRoleAssignmentRepository clientRoleAssignmentRepository;

    public TokenIntrospectorRolesHelper(ResourceRoleRepository resourceRoleRepository,
                                        RowLevelRoleRepository rowLevelRoleRepository,
                                        RegisteredClientRoleAssignmentRepository clientRoleAssignmentRepository) {
        this.resourceRoleRepository = resourceRoleRepository;
        this.rowLevelRoleRepository = rowLevelRoleRepository;
        this.clientRoleAssignmentRepository = clientRoleAssignmentRepository;
    }

    /**
     * Converts a list of roles specified for the client in properties file into the list of
     * {@link RoleGrantedAuthority}.
     *
     * @param clientId a client id
     * @return a list of RoleGrantedAuthority
     */
    public List<RoleGrantedAuthority> getClientGrantedAuthorities(String clientId) {
        Collection<RegisteredClientRoleAssignment> roleAssignments = clientRoleAssignmentRepository.findByClientId(clientId);

        List<String> resourceRoles = roleAssignments.stream()
                .flatMap(roleAssignment -> roleAssignment.resourceRoles().stream())
                .toList();
        List<RoleGrantedAuthority> resourceRoleAuthorities = resourceRoles.stream()
                .map(resourceRoleRepository::getRoleByCode)
                .map(RoleGrantedAuthority::ofResourceRole)
                .toList();

        List<String> rowLevelRoles = roleAssignments.stream()
                .flatMap(roleAssignment -> roleAssignment.rowLevelRoles().stream())
                .toList();
        List<RoleGrantedAuthority> rowLevelRolesAuthorities = rowLevelRoles.stream()
                .map(rowLevelRoleRepository::getRoleByCode)
                .map(RoleGrantedAuthority::ofRowLevelRole)
                .toList();

        List<RoleGrantedAuthority> authorities = new ArrayList<>();
        authorities.addAll(resourceRoleAuthorities);
        authorities.addAll(rowLevelRolesAuthorities);

        return authorities;
    }
}
