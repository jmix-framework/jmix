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

package ${project_rootPackage}.security;

import ${project_rootPackage}.entity.User;
import io.jmix.core.security.user.UserAuthoritiesPopulator;
import io.jmix.localusermanagementflowui.security.UserManagementRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The class is used by the {@link io.jmix.core.security.user.DatabaseUserRepository} to populate granted authorities
 * of the loaded user.
 */
@Component("${normalizedPrefix_underscore}AppUserAuthoritiesPopulator")
public class AppUserAuthoritiesPopulator implements UserAuthoritiesPopulator<User> {
    private static final String ROLE_PREFIX = "ROLE_";

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";
    private static final String EDIT_FILTER_ROLE = "flowui-filter";

    @Override
    public void populateUserAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(createRoleAuthority(USER_ROLE));
        authorities.add(createRoleAuthority(EDIT_FILTER_ROLE));
        if ("admin".equals(user.getUsername())) {
            authorities.add(createRoleAuthority(ADMIN_ROLE));
            authorities.add(createRoleAuthority(UserManagementRole.CODE));
        }
        user.setAuthorities(authorities);
    }

    private GrantedAuthority createRoleAuthority(String roleName) {
        return new SimpleGrantedAuthority(ROLE_PREFIX + roleName);
    }
}