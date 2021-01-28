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

package io.jmix.securitydata.user;

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.GrantedAuthorityContainer;
import io.jmix.core.security.PasswordNotMatchException;
import io.jmix.core.security.UserManager;
import io.jmix.core.security.UserRepository;
import io.jmix.security.authentication.RoleGrantedAuthority;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentRepository;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.ObjectUtils;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Abstract {@link UserRepository} that loads User entity from the database. A {@link UserRepository} generated in the
 * project may extend this class. It must override the {@link #getUserClass()} method.
 *
 * @param <T>
 */
public abstract class AbstractDatabaseUserRepository<T extends UserDetails> implements UserRepository, UserManager {

    protected T systemUser;
    protected T anonymousUser;

    protected DataManager dataManager;
    protected Metadata metadata;
    protected ResourceRoleRepository resourceRoleRepository;
    protected RowLevelRoleRepository rowLevelRoleRepository;
    protected RoleAssignmentRepository roleAssignmentRepository;

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setRoleAssignmentRepository(RoleAssignmentRepository roleAssignmentRepository) {
        this.roleAssignmentRepository = roleAssignmentRepository;
    }

    @Autowired
    public void setResourceRoleRepository(ResourceRoleRepository resourceRoleRepository) {
        this.resourceRoleRepository = resourceRoleRepository;
    }

    @Autowired
    public void setRowLevelRoleRepository(RowLevelRoleRepository rowLevelRoleRepository) {
        this.rowLevelRoleRepository = rowLevelRoleRepository;
    }

    @PostConstruct
    private void init() {
        systemUser = createSystemUser();
        anonymousUser = createAnonymousUser();
    }

    /**
     * Method returns an actual class of the User entity used in the project
     */
    protected abstract Class<T> getUserClass();

    protected T createSystemUser() {
        T systemUser = metadata.create(getUserClass());
        EntityValues.setValue(systemUser, "username", "system");
        return systemUser;
    }

    protected T createAnonymousUser() {
        T anonymousUser = metadata.create(getUserClass());
        EntityValues.setValue(anonymousUser, "username", "anonymous");
        if (anonymousUser instanceof GrantedAuthorityContainer) {
            ((GrantedAuthorityContainer) anonymousUser).setAuthorities(createAuthorities("anonymous"));
        }
        return anonymousUser;
    }

    @Override
    public T getSystemUser() {
        return systemUser;
    }

    @Override
    public T getAnonymousUser() {
        return anonymousUser;
    }

    @Override
    public List<T> getByUsernameLike(String username) {
        //todo view
        return dataManager.load(getUserClass())
                .query("where e.username like :username")
                .parameter("username", "%" + username + "%")
                .list();
    }

    @Override
    public T loadUserByUsername(String username) throws UsernameNotFoundException {
        //todo view
        List<T> users = dataManager.load(getUserClass())
                .query("where e.username = :username")
                .parameter("username", username)
                .list();
        if (!users.isEmpty()) {
            T user = users.get(0);
            if (user instanceof GrantedAuthorityContainer) {
                ((GrantedAuthorityContainer) user).setAuthorities(createAuthorities(username));
            }
            return user;
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    @Override
    public void changePassword(@Nullable String userName, @Nullable String oldPassword, @Nullable String newPassword) throws PasswordNotMatchException {
        Preconditions.checkNotNullArgument(userName, "Null userName");
        Preconditions.checkNotNullArgument(newPassword, "Null new password hash");
        T userDetails = loadUserByUsername(userName);
        if (!ObjectUtils.isEmpty(oldPassword) && oldPassword.equals(userDetails.getPassword())) {
            throw new PasswordNotMatchException();
        }
        EntityValues.setValue(userDetails, "password", newPassword);
        dataManager.save(userDetails);
    }

    protected Collection<? extends GrantedAuthority> createAuthorities(String username) {
        return roleAssignmentRepository.getAssignmentsByUsername(username).stream()
                .map(this::createAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected GrantedAuthority createAuthority(RoleAssignment roleAssignment) {
        GrantedAuthority authority = null;
        if (RoleAssignmentRoleType.RESOURCE.equals(roleAssignment.getRoleType())) {
            ResourceRole role = resourceRoleRepository.findRoleByCode(roleAssignment.getRoleCode());
            if (role != null) {
                authority = RoleGrantedAuthority.ofResourceRole(role);
            }
        } else if (RoleAssignmentRoleType.ROW_LEVEL.equals(roleAssignment.getRoleType())) {
            RowLevelRole role = rowLevelRoleRepository.findRoleByCode(roleAssignment.getRoleCode());
            if (role != null) {
                authority = RoleGrantedAuthority.ofRowLevelRole(role);
            }
        }
        return authority;
    }
}
