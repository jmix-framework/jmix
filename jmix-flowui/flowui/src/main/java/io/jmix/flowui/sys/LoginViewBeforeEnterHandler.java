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

package io.jmix.flowui.sys;

import com.vaadin.flow.router.BeforeEnterEvent;
import io.jmix.core.annotation.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles {@link BeforeEnterEvent} for the configured login view.
 * <p>
 * The default implementation forwards an already authenticated user from the login view
 * to the configured main view.
 */
@Internal
@Component("flowui_LoginViewBeforeEnterHandler")
public class LoginViewBeforeEnterHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginViewBeforeEnterHandler.class);

    protected LoginViewRedirectSupport loginViewRedirectSupport;

    public LoginViewBeforeEnterHandler(LoginViewRedirectSupport loginViewRedirectSupport) {
        this.loginViewRedirectSupport = loginViewRedirectSupport;
    }

    /**
     * Handles navigation before entering a view.
     *
     * @param event navigation event to handle
     */
    public void handle(BeforeEnterEvent event) {
        if (isLoginView(event) && isUserAuthenticated()) {
            handleAuthenticatedUser(event);
        }
    }

    protected boolean isLoginView(BeforeEnterEvent event) {
        return loginViewRedirectSupport.isLoginView(event.getNavigationTarget());
    }

    protected boolean isUserAuthenticated() {
        return loginViewRedirectSupport.isUserAuthenticated();
    }

    protected void handleAuthenticatedUser(BeforeEnterEvent event) {
        loginViewRedirectSupport.findMainViewInfo()
                .ifPresent(mainViewInfo -> {
                    log.debug("Forwarding already logged-in user to the main view");
                    event.forwardTo(mainViewInfo.getControllerClass());
                });
    }
}
