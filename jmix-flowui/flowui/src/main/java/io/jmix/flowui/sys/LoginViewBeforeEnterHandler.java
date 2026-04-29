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

import com.google.common.base.Strings;
import com.vaadin.flow.router.BeforeEnterEvent;
import io.jmix.core.annotation.Internal;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;


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

    protected UiProperties uiProperties;
    protected CurrentAuthentication currentAuthentication;
    protected ViewRegistry viewRegistry;

    public LoginViewBeforeEnterHandler(UiProperties uiProperties,
                                       CurrentAuthentication currentAuthentication,
                                       ViewRegistry viewRegistry) {
        this.uiProperties = uiProperties;
        this.currentAuthentication = currentAuthentication;
        this.viewRegistry = viewRegistry;
    }

    /**
     * Handles navigation before entering a view.
     *
     * @param event navigation event to handle
     */
    public void handle(BeforeEnterEvent event) {
        if (!isLoginView(event) || !isUserAuthenticated()) {
            return;
        }

        handleAuthenticatedUser(event);
    }

    protected boolean isLoginView(BeforeEnterEvent event) {
        String loginViewId = uiProperties.getLoginViewId();
        if (Strings.isNullOrEmpty(loginViewId)) {
            return false;
        }

        Optional<ViewInfo> loginViewInfo = viewRegistry.findViewInfo(loginViewId);
        return loginViewInfo.isPresent()
                && event.getNavigationTarget().equals(loginViewInfo.get().getControllerClass());
    }

    protected boolean isUserAuthenticated() {
        if (!currentAuthentication.isSet()) {
            return false;
        }

        Authentication authentication = currentAuthentication.getAuthentication();
        return !(authentication instanceof AnonymousAuthenticationToken);
    }

    protected void handleAuthenticatedUser(BeforeEnterEvent event) {
        String mainViewId = uiProperties.getMainViewId();
        if (Strings.isNullOrEmpty(mainViewId)) {
            return;
        }

        viewRegistry.findViewInfo(mainViewId)
                .ifPresent(mainViewInfo -> {
                    log.debug("Forwarding already logged-in user to the main view");
                    event.forwardTo(mainViewInfo.getControllerClass());
                });
    }
}
