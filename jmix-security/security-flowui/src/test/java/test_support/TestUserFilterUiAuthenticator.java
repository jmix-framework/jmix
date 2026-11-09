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

import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.flowui.testassist.UiTestAuthenticator;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.securityflowui.role.UiFilterRole;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import test_support.role.TestUserFilterRole;

import java.util.Locale;
import java.util.stream.Stream;

/**
 * Authenticates UI tests as a user who may read users and edit their filters: {@link UiFilterRole} and
 * the minimal UI policies, no access to other entities.
 */
public class TestUserFilterUiAuthenticator implements UiTestAuthenticator {

    @Override
    public void setupAuthentication(ApplicationContext context) {
        RoleGrantedAuthorityUtils roleGrantedAuthorityUtils = context.getBean(RoleGrantedAuthorityUtils.class);
        UserDetails user = User.builder()
                .username("admin")
                .password("{noop}")
                .authorities(Stream.of(TestUserFilterRole.CODE, UiFilterRole.CODE)
                        .map(roleGrantedAuthorityUtils::createResourceRoleGrantedAuthority)
                        .toList())
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        authentication.setDetails(ClientDetails.builder().locale(Locale.ENGLISH).build());
        SecurityContextHelper.setAuthentication(authentication);
    }

    @Override
    public void removeAuthentication(ApplicationContext context) {
        SecurityContextHelper.setAuthentication(null);
    }
}
