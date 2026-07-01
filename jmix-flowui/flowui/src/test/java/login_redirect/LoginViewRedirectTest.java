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

package login_redirect;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import login_redirect.view.LoginRedirectLoginView;
import login_redirect.view.LoginRedirectMainView;
import login_redirect.view.LoginRedirectOtherView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import test_support.FlowuiTestConfiguration;

@UiTest(viewBasePackages = "login_redirect.view", initialView = LoginRedirectOtherView.class)
@SpringBootTest(
        classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class},
        properties = {
                "jmix.ui.login-view-id=loginRedirect_LoginView",
                "jmix.ui.main-view-id=loginRedirect_MainView"
        }
)
public class LoginViewRedirectTest {

    @Autowired
    ViewNavigationSupport navigationSupport;

    @BeforeEach
    void setUp() {
        VaadinService.getCurrent().fireUIInitListeners(UI.getCurrent());
    }

    @Test
    @DisplayName("Authenticated user is rerouted from login view to configured main view")
    void testAuthenticatedUserReroutedFromLoginViewToMainView() {
        navigationSupport.navigate(LoginRedirectLoginView.class);

        assertCurrentView(LoginRedirectMainView.class);
    }

    @Test
    @DisplayName("Anonymous user can navigate to login view")
    void testAnonymousUserCanNavigateToLoginView() {
        SecurityContextHelper.setAuthentication(new AnonymousAuthenticationToken(
                "test-key",
                "anonymous",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        ));

        navigationSupport.navigate(LoginRedirectLoginView.class);

        assertCurrentView(LoginRedirectLoginView.class);
    }

    @Test
    @DisplayName("Authenticated user navigation to other view is untouched")
    void testAuthenticatedUserNavigationToOtherViewIsUntouched() {
        navigationSupport.navigate(LoginRedirectOtherView.class);

        assertCurrentView(LoginRedirectOtherView.class);
    }

    protected void assertCurrentView(Class<? extends View<?>> expectedView) {
        View<?> currentView = UiTestUtils.getCurrentView();
        Assertions.assertEquals(expectedView, currentView.getClass());
    }
}
