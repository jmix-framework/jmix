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

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.security.PasswordNotMatchException;
import io.jmix.core.security.UserManager;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.event.SingleUserPasswordChangeEvent;
import io.jmix.core.security.event.UserDisabledEvent;
import io.jmix.core.security.event.UserPasswordResetEvent;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.security.authentication.AcceptsGrantedAuthorities;
import io.jmix.security.authentication.RoleGrantedAuthority;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentRepository;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import org.springframework.lang.Nullable;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base implementation of {@link UserRepository} that loads users from the database.
 * The type of entity representing users is specified in the {@link #getUserClass()} method.
 *
 * @param <T> type of entity representing users
 */
public abstract class AbstractDatabaseUserRepository<T extends UserDetails> implements UserRepository, UserManager {

    private T systemUser;
    private T anonymousUser;

    @Autowired
    private UnconstrainedDataManager dataManager;
    @Autowired
    private Metadata metadata;
    @Autowired
    private ResourceRoleRepository resourceRoleRepository;
    @Autowired
    private RowLevelRoleRepository rowLevelRoleRepository;
    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PersistentTokenRepository tokenRepository;
    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    /**
     * Helps create authorities from roles.
     */
    public class GrantedAuthoritiesBuilder {

        private List<GrantedAuthority> authorities = new ArrayList<>();

        /**
         * Adds a resource role by its code.
         */
        public GrantedAuthoritiesBuilder addResourceRole(String code) {
            ResourceRole role = resourceRoleRepository.getRoleByCode(code);
            RoleGrantedAuthority authority = RoleGrantedAuthority.ofResourceRole(role);
            authorities.add(authority);
            return this;
        }

        /**
         * Adds a row-level role by its code.
         */
        public GrantedAuthoritiesBuilder addRowLevelRole(String code) {
            RowLevelRole role = rowLevelRoleRepository.getRoleByCode(code);
            RoleGrantedAuthority authority = RoleGrantedAuthority.ofRowLevelRole(role);
            authorities.add(authority);
            return this;
        }

        /**
         * Builds a collection of authorities.
         */
        public Collection<GrantedAuthority> build() {
            return authorities;
        }
    }

    @PostConstruct
    private void init() {
        systemUser = createSystemUser();
        anonymousUser = createAnonymousUser();
    }

    /**
     * Returns the class of a JPA entity representing users in the application.
     */
    protected abstract Class<T> getUserClass();

    /**
     * Creates the built-in 'system' user.
     */
    protected T createSystemUser() {
        T systemUser = metadata.create(getUserClass());
        EntityValues.setValue(systemUser, "username", "system");
        initSystemUser(systemUser);
        return systemUser;
    }

    /**
     * Initializes the built-in 'system' user.
     * Override in the application to grant authorities or initialize attributes.
     */
    protected void initSystemUser(T systemUser) {
    }

    /**
     * Creates the built-in 'anonymous' user.
     */
    protected T createAnonymousUser() {
        T anonymousUser = metadata.create(getUserClass());
        EntityValues.setValue(anonymousUser, "username", "anonymous");
        initAnonymousUser(anonymousUser);
        return anonymousUser;
    }

    /**
     * Initializes the built-in 'anonymous' user.
     * Override in the application to grant authorities or initialize attributes.
     */
    protected void initAnonymousUser(T anonymousUser) {
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
    public List<T> getByUsernameLike(String substring) {
        return dataManager.load(getUserClass())
                .query("where e.username like :username")
                .parameter("username", "%" + substring + "%")
                .list();
    }

    @Override
    public T loadUserByUsername(String username) throws UsernameNotFoundException {
        List<T> users = dataManager.load(getUserClass())
                .query("where e.username = :username")
                .parameter("username", username)
                .list();
        if (!users.isEmpty()) {
            T user = users.get(0);
            if (user instanceof AcceptsGrantedAuthorities) {
                ((AcceptsGrantedAuthorities) user).setAuthorities(createAuthorities(username));
            }
            return user;
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    @Override
    public void changePassword(String userName, @Nullable String oldPassword, @Nullable String newPassword) throws PasswordNotMatchException {
        Preconditions.checkNotNullArgument(userName, "Null userName");
        Preconditions.checkNotNullArgument(newPassword, "Null new password");
        T userDetails = loadUserByUsername(userName);
        changePassword(userDetails, oldPassword, newPassword);
        eventPublisher.publishEvent(new SingleUserPasswordChangeEvent(userName, newPassword));
    }

    private void changePassword(T userDetails, @Nullable String oldPassword, @Nullable String newPassword) throws PasswordNotMatchException {
        if (!Strings.isNullOrEmpty(userDetails.getPassword()) && passwordEncoder.matches(newPassword, userDetails.getPassword())
                || oldPassword != null && !passwordEncoder.matches(oldPassword, userDetails.getPassword())) {
            throw new PasswordNotMatchException();
        }
        EntityValues.setValue(userDetails, "password", passwordEncoder.encode(newPassword));
        dataManager.save(userDetails);
    }

    @Override
    public Map<UserDetails, String> resetPasswords(Set<UserDetails> users) {
        Map<UserDetails, String> usernamePasswordMap = new LinkedHashMap<>();
        for (UserDetails user : users) {
            String newPassword;
            boolean success = false;
            do {
                newPassword = generateRandomPassword();
                try {
                    changePassword(loadUserByUsername(user.getUsername()), null, newPassword);
                } catch (PasswordNotMatchException e) {
                    continue;
                }
                success = true;
            } while (!success);
            usernamePasswordMap.put(user, newPassword);
        }
        resetRememberMe(users);
        eventPublisher.publishEvent(new UserPasswordResetEvent(usernamePasswordMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getUsername(), Map.Entry::getValue))));
        return usernamePasswordMap;
    }

    public void resetRememberMe(Collection<UserDetails> users) {
        for (UserDetails user : users) {
            tokenRepository.removeUserTokens(user.getUsername());
        }
    }

    private Collection<? extends GrantedAuthority> createAuthorities(String username) {
        return roleAssignmentRepository.getAssignmentsByUsername(username).stream()
                .map(this::createAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private GrantedAuthority createAuthority(RoleAssignment roleAssignment) {
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

    /**
     * Returns a builder that helps create authorities from roles.
     */
    protected GrantedAuthoritiesBuilder getGrantedAuthoritiesBuilder() {
        return new GrantedAuthoritiesBuilder();
    }

    private String generateRandomPassword() {
        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to load SHA1PRNG", e);
        }
        byte[] passwordBytes = new byte[6];
        random.nextBytes(passwordBytes);
        return new String(Base64.getEncoder().encode(passwordBytes), StandardCharsets.UTF_8).replace("=", "");
    }

    @EventListener
    private void onUserChanged(EntityChangedEvent<? extends UserDetails> event) {
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            eventPublisher.publishEvent(new UserRemovedEvent(
                    Objects.requireNonNull(event.getChanges().getOldValue("username"))));
        } else if (event.getType() == EntityChangedEvent.Type.UPDATED) {
            if (Objects.equals(event.getEntityId().getEntityClass(), getUserClass())) {
                if (isUserDisabled(event)) {
                    UserDetails userDetails = dataManager.load(event.getEntityId()).one();
                    if (!userDetails.isEnabled()) {
                        eventPublisher.publishEvent(new UserDisabledEvent(userDetails.getUsername()));
                    }
                }
            }
        }
    }

    protected boolean isUserDisabled(EntityChangedEvent<? extends UserDetails> event) {
        return event.getChanges().isChanged("active")
                && Boolean.TRUE.equals(event.getChanges().getOldValue("active"));
    }
}
