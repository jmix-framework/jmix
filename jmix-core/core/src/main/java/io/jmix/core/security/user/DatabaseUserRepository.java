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

package io.jmix.core.security.user;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base implementation of {@link UserRepository} that loads users from the database.
 * <p>
 * Authorities are evaluated with {@link UserAuthoritiesPopulator}.
 * <p>
 * The type of entity representing users is resolved by the {@link UserClassResolver}.
 *
 * @param <T> type of entity representing users
 */
public class DatabaseUserRepository<T extends UserDetails> implements UserRepository, UserManager {

    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected PersistentTokenRepository tokenRepository;
    @Autowired
    protected ApplicationEventPublisher eventPublisher;
    @Autowired
    protected UserAuthoritiesPopulator<T> userAuthoritiesPopulator;

    @Autowired
    protected UserClassResolver<T> userClassResolver;

    @Override
    public List<T> getByUsernameLike(String substring) {
        return dataManager.load(getUserClass())
                .query("where e.username like :username")
                .parameter("username", "%" + substring + "%")
                .list();
    }

    @Override
    public T loadUserByUsername(String username) throws UsernameNotFoundException {
        List<T> users = loadUsersByUsernameFromDatabase(username);
        if (!users.isEmpty()) {
            T user = users.get(0);
            populateUserAuthorities(user);
            return user;
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    protected void populateUserAuthorities(T user) {
        userAuthoritiesPopulator.populateUserAuthorities(user);
    }

    protected List<T> loadUsersByUsernameFromDatabase(String username) {
        return dataManager.load(getUserClass())
                .query("where e.username = :username")
                .parameter("username", username)
                .list();
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

    protected Class<T> getUserClass() {
        return userClassResolver.getUserClass();
    }
}
