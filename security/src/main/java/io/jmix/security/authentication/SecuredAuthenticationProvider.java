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

package io.jmix.security.authentication;

import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.ClientDetails;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.Role;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.role.RoleRepository;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SecuredAuthenticationProvider extends DaoAuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(SecuredAuthenticationProvider.class);

    protected RoleRepository roleRepository;
    protected RoleAssignmentRepository roleAssignmentRepository;

    public SecuredAuthenticationProvider(RoleRepository roleRepository, RoleAssignmentRepository roleAssignmentRepository) {
        this.roleRepository = roleRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
    }

    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        if (!(user instanceof BaseUser)) {
            throw new IllegalArgumentException("UserDetails must be an instance of " + BaseUser.class.getCanonicalName());
        }
        SecuredAuthenticationToken resultAuthentication = new SecuredAuthenticationToken((BaseUser) user, user.getAuthorities());
        resultAuthentication.setDetails(authentication.getDetails());

        List<ResourcePolicy> resourcePolicies = new ArrayList<>();
        List<RowLevelPolicy> rowLevelPolicies = new ArrayList<>();

        Collection<RoleAssignment> roleAssignments = roleAssignmentRepository.getAssignmentsByUserKey(((BaseUser) user).getKey());
        for (RoleAssignment roleAssignment : roleAssignments) {
            Role role = roleRepository.getRoleByCode(roleAssignment.getRoleCode());
            if (role != null) {
                resourcePolicies.addAll(role.getResourcePolicies());
                rowLevelPolicies.addAll(role.getRowLevelPolicies());
            } else {
                log.error("Role {} not found", roleAssignment.getRoleCode());
            }
        }

        resultAuthentication.setResourcePolicies(resourcePolicies);
        resultAuthentication.setRowLevelPolicies(rowLevelPolicies);

        Object details = authentication.getDetails();
        if (details instanceof ClientDetails) {
            resultAuthentication.setLocale(((ClientDetails) details).getLocale());
        }

        return resultAuthentication;
    }
}
