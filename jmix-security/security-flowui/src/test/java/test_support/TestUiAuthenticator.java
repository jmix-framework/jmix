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
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import test_support.role.TestFullAccessRole;

import java.util.Locale;

/**
 * Authenticates UI tests as an English user with full access. German is the default locale of the tests, so the
 * texts the user sees are told apart from the texts the roles keep.
 */
public class TestUiAuthenticator implements UiTestAuthenticator {

    private static final String USERNAME = "admin";

    @Override
    public void setupAuthentication(ApplicationContext context) {
        RoleGrantedAuthorityUtils roleGrantedAuthorityUtils = context.getBean(RoleGrantedAuthorityUtils.class);
        UserDetails user = User.builder()
                .username(USERNAME)
                .password("{noop}")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(getRoleCode()))
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

    /**
     * Returns the code of the resource role granted to the user.
     */
    protected String getRoleCode() {
        return TestFullAccessRole.CODE;
    }
}
