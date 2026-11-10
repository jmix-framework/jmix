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

package test_support;

import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.flowui.testassist.UiTestAuthenticator;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import test_support.role.InspectorFullAccessRole;

/**
 * Authenticates UI tests as a user with full access, so that the security constraints registered by the
 * security module do not interfere with the tested views.
 */
public class TestFullAccessUiAuthenticator implements UiTestAuthenticator {

    protected static final String USERNAME = "entity-inspector-full-access";

    @Override
    public void setupAuthentication(ApplicationContext context) {
        RoleGrantedAuthorityUtils roleGrantedAuthorityUtils = context.getBean(RoleGrantedAuthorityUtils.class);
        UserDetails user = User.builder()
                .username(USERNAME)
                .password("{noop}")
                .authorities(roleGrantedAuthorityUtils
                        .createResourceRoleGrantedAuthority(InspectorFullAccessRole.NAME))
                .build();

        InMemoryUserRepository userRepository = context.getBean(InMemoryUserRepository.class);
        if (userRepository.getByUsernameLike(USERNAME).isEmpty()) {
            userRepository.addUser(user);
        }

        SecurityContextHelper.setAuthentication(
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    }

    @Override
    public void removeAuthentication(ApplicationContext context) {
        SecurityContextHelper.setAuthentication(null);
    }
}
