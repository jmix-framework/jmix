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

package reportsrest.security;

import io.jmix.core.AccessManager;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.reportsrest.security.event.AbstractReportBeforeInvocationEventListener;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.ReportsRestTestConfiguration;
import test_support.role.FullAccessRole;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks the "reports.rest.enabled" gate shared by the AS and OIDC report listeners: the policy must be applied to
 * report REST requests regardless of the servlet context path, and only to them.
 */
@ContextConfiguration(classes = ReportsRestTestConfiguration.class)
@ExtendWith(SpringExtension.class)
public class ReportBeforeInvocationEventListenerTest {

    @Autowired
    AccessManager accessManager;
    @Autowired
    SystemAuthenticator systemAuthenticator;
    @Autowired
    RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    TestListener listener;
    Authentication withoutPolicy;
    Authentication withPolicy;

    @BeforeEach
    void setUp() {
        listener = new TestListener(accessManager);
        withoutPolicy = authenticationWith(List.of());
        withPolicy = authenticationWith(
                List.of(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(FullAccessRole.NAME)));
    }

    @Test
    void checkAccess_contextPath_deniesUserWithoutPolicy() {
        HttpServletRequest request = request("/app", "/app/rest/reports/run/1");

        assertFalse(listener.check(request, withoutPolicy));
    }

    @Test
    void checkAccess_noContextPath_deniesUserWithoutPolicy() {
        HttpServletRequest request = request("", "/rest/reports/run/1");

        assertFalse(listener.check(request, withoutPolicy));
    }

    @Test
    void checkAccess_contextPath_permitsUserWithPolicy() {
        HttpServletRequest request = request("/app", "/app/rest/reports/run/1");

        assertTrue(listener.check(request, withPolicy));
    }

    @Test
    void checkAccess_nonReportUrl_skipsPolicy() {
        HttpServletRequest request = request("/app", "/app/rest/entities/User");

        assertTrue(listener.check(request, withoutPolicy));
    }

    @Test
    void checkAccess_restoresPreviousAuthentication() {
        Authentication previous = systemAuthenticator.begin();
        try {
            listener.check(request("/app", "/app/rest/reports/run/1"), withoutPolicy);

            assertSame(previous, SecurityContextHelper.getAuthentication());
        } finally {
            systemAuthenticator.end();
        }
    }

    static HttpServletRequest request(String contextPath, String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath(contextPath);
        request.setRequestURI(requestUri);
        request.setServletPath(requestUri.substring(contextPath.length()));
        return request;
    }

    static Authentication authenticationWith(List<GrantedAuthority> authorities) {
        UserDetails user = User.builder()
                .username("report-rest-user")
                .password("{noop}")
                .authorities(authorities)
                .build();
        return UsernamePasswordAuthenticationToken.authenticated(user, null, authorities);
    }

    /**
     * Exposes the protected check of the abstract listener; the concrete AS and OIDC listeners need the respective
     * add-ons on the classpath, which the module's tests do not have.
     */
    static class TestListener extends AbstractReportBeforeInvocationEventListener {

        TestListener(AccessManager accessManager) {
            this.accessManager = accessManager;
        }

        boolean check(HttpServletRequest request, Authentication authentication) {
            return checkAccess(request, authentication);
        }
    }
}
