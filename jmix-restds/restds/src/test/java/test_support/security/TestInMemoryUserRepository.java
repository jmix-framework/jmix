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

package test_support.security;

import io.jmix.core.security.InMemoryUserRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestInMemoryUserRepository extends InMemoryUserRepository {

    public TestInMemoryUserRepository() {
        super();
        addUser(createUserWithRoles("customerReadOnly",
                List.of(CustomerReadOnlyRole.CODE), null));
        addUser(createUserWithRoles("preferredContactOnly",
                List.of(FullAccessRole.CODE), List.of(PreferredContactOnlyRowLevelRole.CODE)));
    }

    private UserDetails createUserWithRoles(String username, @Nullable Collection<String> resourceRoles, @Nullable Collection<String> rowLevelRoles) {
        return User.builder()
                .username(username)
                .password("{noop}" + username)
                .authorities(createAuthorities(resourceRoles, rowLevelRoles))
                .build();
    }

    private Collection<? extends GrantedAuthority> createAuthorities(@Nullable Collection<String> resourceRoles, @Nullable Collection<String> rowLevelRoles) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (resourceRoles != null) {
            authorities.addAll(resourceRoles.stream().map(this::resourceRole).toList());
        }
        if (rowLevelRoles != null) {
            authorities.addAll(rowLevelRoles.stream().map(this::rowLevelRole).toList());
        }
        return authorities;
    }

    @Override
    protected UserDetails createSystemUser() {
        return User.builder()
                .username("system")
                .password("{noop}system")
                .authorities(resourceRole(FullAccessRole.CODE))
                .build();
    }

    private GrantedAuthority resourceRole(String roleCode) {
        return new SimpleGrantedAuthority("ROLE_" + roleCode);
    }

    private GrantedAuthority rowLevelRole(String roleCode) {
        return new SimpleGrantedAuthority("ROW_LEVEL_ROLE_" + roleCode);
    }
}