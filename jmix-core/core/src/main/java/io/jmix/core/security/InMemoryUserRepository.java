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

package io.jmix.core.security;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryUserRepository implements UserRepository {

    protected UserDetails systemUser;
    protected UserDetails anonymousUser;
    protected List<UserDetails> users = new ArrayList<>();

    public InMemoryUserRepository() {
        initServiceUsers();
    }

    protected void initServiceUsers() {
        systemUser = createSystemUser();
        anonymousUser = createAnonymousUser();
    }

    protected UserDetails createSystemUser() {
        return User.builder()
                .username("system")
                .password("{noop}")
                .authorities(Collections.emptyList())
                .build();
    }

    protected UserDetails createAnonymousUser() {
        return User.builder()
                .username("anonymous")
                .password("{noop}")
                .authorities(Collections.emptyList())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findAny()
                .map(this::copyUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
    }

    @Override
    public UserDetails getSystemUser() {
        return systemUser;
    }

    @Override
    public UserDetails getAnonymousUser() {
        return anonymousUser;
    }

    @Override
    public List<UserDetails> getByUsernameLike(String substring) {
        return users.stream()
                .filter(user -> user.getUsername().contains(substring))
                .collect(Collectors.toList());
    }

    public void addUser(UserDetails user) {
        users.add(user);
    }

    public void removeUser(UserDetails user) {
        users.remove(user);
    }

    protected UserDetails copyUserDetails(UserDetails userDetails) {
        if (userDetails instanceof CredentialsContainer) {
            return User.withUserDetails(userDetails).build();
        } else {
            return userDetails;
        }
    }
}
