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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.BeforeEnterEvent;
import io.jmix.core.CoreProperties;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.exception.UiExceptionHandlers;
import io.jmix.flowui.sys.JmixServiceInitListener;
import io.jmix.flowui.sys.LoginViewBeforeEnterHandler;
import io.jmix.flowui.sys.LoginViewRedirectSupport;
import io.jmix.flowui.view.ViewRegistry;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class LoginViewBeforeEnterHandlerExtensionTest {

    @Test
    @DisplayName("Extended login view before enter handler is used")
    void testExtendedLoginViewBeforeEnterHandlerIsUsed() {
        TestLoginViewBeforeEnterHandler loginViewBeforeEnterHandler =
                new TestLoginViewBeforeEnterHandler(new TestLoginViewRedirectSupport());
        TestJmixServiceInitListener serviceInitListener = new TestJmixServiceInitListener(loginViewBeforeEnterHandler);
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);

        serviceInitListener.loginViewBeforeEnter(event);

        Assertions.assertSame(event, loginViewBeforeEnterHandler.handledEvent);
    }

    static class TestJmixServiceInitListener extends JmixServiceInitListener {

        TestJmixServiceInitListener(LoginViewBeforeEnterHandler loginViewBeforeEnterHandler) {
            super(mock(ViewRegistry.class),
                    mock(UiExceptionHandlers.class),
                    mock(CoreProperties.class),
                    loginViewBeforeEnterHandler,
                    mock(JmixModules.class),
                    mock(Resources.class));
        }

        void loginViewBeforeEnter(BeforeEnterEvent event) {
            onLoginViewBeforeEnter(event);
        }
    }

    static class TestLoginViewBeforeEnterHandler extends LoginViewBeforeEnterHandler {

        BeforeEnterEvent handledEvent;

        TestLoginViewBeforeEnterHandler(LoginViewRedirectSupport loginViewRedirectSupport) {
            super(loginViewRedirectSupport);
        }

        @Override
        protected void handleAuthenticatedUser(@Nonnull BeforeEnterEvent event) {
            handledEvent = event;
        }
    }

    static class TestLoginViewRedirectSupport extends LoginViewRedirectSupport {

        TestLoginViewRedirectSupport() {
            super(mock(UiProperties.class), mock(CurrentAuthentication.class), mock(ViewRegistry.class));
        }

        @Override
        public boolean isLoginView(Class<? extends Component> navigationTarget) {
            return true;
        }

        @Override
        public boolean isUserAuthenticated() {
            return true;
        }
    }
}
