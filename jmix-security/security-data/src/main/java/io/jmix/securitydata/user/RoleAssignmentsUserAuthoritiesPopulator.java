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

package io.jmix.securitydata.user;

import io.jmix.core.security.user.AcceptsGrantedAuthorities;
import io.jmix.core.security.user.UserAuthoritiesPopulator;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentRepository;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An implementation of {@link UserAuthoritiesPopulator} that takes user granted authorities information from the
 * {@link RoleAssignmentRepository}.
 */
@Component("sec_RoleAssignmentsUserAuthoritiesPopulator")
public class RoleAssignmentsUserAuthoritiesPopulator implements UserAuthoritiesPopulator<UserDetails> {

    private static final Logger log = LoggerFactory.getLogger(RoleAssignmentsUserAuthoritiesPopulator.class);

    protected RoleAssignmentRepository roleAssignmentRepository;

    protected RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    public RoleAssignmentsUserAuthoritiesPopulator(RoleAssignmentRepository roleAssignmentRepository,
                                                   RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.roleGrantedAuthorityUtils = roleGrantedAuthorityUtils;
    }

    @Override
    public void populateUserAuthorities(UserDetails user) {
        Collection<? extends GrantedAuthority> grantedAuthorities = createAuthorities(user.getUsername());
        if (user instanceof AcceptsGrantedAuthorities acceptsGrantedAuthorities) {
            acceptsGrantedAuthorities.setAuthorities(grantedAuthorities);
        } else {
            log.warn("Cannot set authorities to user {}. User class must implement {}.",
                    user.getUsername(),
                    AcceptsGrantedAuthorities.class.getSimpleName());
        }
    }

    protected Collection<? extends GrantedAuthority> createAuthorities(String username) {
        return roleAssignmentRepository.getAssignmentsByUsername(username).stream()
                .map(this::createAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nullable
    protected GrantedAuthority createAuthority(RoleAssignment roleAssignment) {
        GrantedAuthority authority = null;
        if (RoleAssignmentRoleType.RESOURCE.equals(roleAssignment.getRoleType())) {
            authority = roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(roleAssignment.getRoleCode());
        } else if (RoleAssignmentRoleType.ROW_LEVEL.equals(roleAssignment.getRoleType())) {
            authority = roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(roleAssignment.getRoleCode());
        }
        return authority;
    }
}
