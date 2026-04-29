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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.VaadinService;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.sys.LoginViewBeforeEnterHandler;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import jakarta.annotation.Nonnull;
import login_redirect.view.LoginRedirectLoginView;
import login_redirect.view.LoginRedirectMainView;
import login_redirect.view.LoginRedirectOtherView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import test_support.FlowuiTestConfiguration;

@UiTest(viewBasePackages = "login_redirect.view", initialView = LoginRedirectMainView.class)
@SpringBootTest(
        classes = {
                FlowuiTestConfiguration.class,
                FlowuiTestAssistConfiguration.class,
                LoginViewBeforeEnterHandlerExtensionTest.TestLoginViewBeforeEnterHandlerConfiguration.class
        },
        properties = {
                "jmix.ui.login-view-id=loginRedirect_LoginView",
                "jmix.ui.main-view-id=loginRedirect_MainView"
        }
)
public class LoginViewBeforeEnterHandlerExtensionTest {

    @Autowired
    ViewNavigationSupport navigationSupport;

    @BeforeEach
    void setUp() {
        VaadinService.getCurrent().fireUIInitListeners(UI.getCurrent());
    }

    @Test
    @DisplayName("Extended login view before enter handler is used")
    void testExtendedLoginViewBeforeEnterHandlerIsUsed() {
        navigationSupport.navigate(LoginRedirectLoginView.class);

        Assertions.assertEquals(LoginRedirectOtherView.class, UiTestUtils.getCurrentView().getClass());
    }

    @TestConfiguration
    static class TestLoginViewBeforeEnterHandlerConfiguration {

        @Bean("test_LoginViewBeforeEnterHandler")
        @Primary
        LoginViewBeforeEnterHandler loginViewBeforeEnterHandler(UiProperties uiProperties,
                                                                CurrentAuthentication currentAuthentication,
                                                                ViewRegistry viewRegistry) {
            return new TestLoginViewBeforeEnterHandler(uiProperties, currentAuthentication, viewRegistry);
        }
    }

    static class TestLoginViewBeforeEnterHandler extends LoginViewBeforeEnterHandler {

        public TestLoginViewBeforeEnterHandler(UiProperties uiProperties,
                                               CurrentAuthentication currentAuthentication,
                                               ViewRegistry viewRegistry) {
            super(uiProperties, currentAuthentication, viewRegistry);
        }

        @Override
        protected void handleAuthenticatedUser(@Nonnull BeforeEnterEvent event) {
            event.rerouteTo(LoginRedirectOtherView.class);
        }
    }
}
