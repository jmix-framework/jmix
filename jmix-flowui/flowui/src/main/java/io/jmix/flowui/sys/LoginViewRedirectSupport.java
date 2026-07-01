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
import com.vaadin.flow.component.Component;
import io.jmix.core.annotation.Internal;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

/**
 * Provides shared checks used to redirect an already authenticated user from the configured login view.
 */
@Internal
@org.springframework.stereotype.Component("flowui_LoginViewRedirectSupport")
public class LoginViewRedirectSupport {

    protected UiProperties uiProperties;
    protected CurrentAuthentication currentAuthentication;
    protected ViewRegistry viewRegistry;

    public LoginViewRedirectSupport(UiProperties uiProperties,
                                    CurrentAuthentication currentAuthentication,
                                    ViewRegistry viewRegistry) {
        this.uiProperties = uiProperties;
        this.currentAuthentication = currentAuthentication;
        this.viewRegistry = viewRegistry;
    }

    /**
     * Checks whether the navigation target is the configured login view.
     *
     * @param navigationTarget navigation target class
     * @return {@code true} if the target matches the configured login view
     */
    public boolean isLoginView(Class<? extends Component> navigationTarget) {
        String loginViewId = uiProperties.getLoginViewId();
        if (Strings.isNullOrEmpty(loginViewId)) {
            return false;
        }

        Optional<ViewInfo> loginViewInfo = viewRegistry.findViewInfo(loginViewId);
        return loginViewInfo.isPresent()
                && navigationTarget.equals(loginViewInfo.get().getControllerClass());
    }

    /**
     * Checks whether the current authentication is set and is not anonymous.
     *
     * @return {@code true} if the current user is authenticated
     */
    public boolean isUserAuthenticated() {
        if (!currentAuthentication.isSet()) {
            return false;
        }

        Authentication authentication = currentAuthentication.getAuthentication();
        return !(authentication instanceof AnonymousAuthenticationToken);
    }

    /**
     * Finds the configured main view.
     *
     * @return main view info, or an empty optional if the main view is not configured or not registered
     */
    public Optional<ViewInfo> findMainViewInfo() {
        String mainViewId = uiProperties.getMainViewId();
        if (Strings.isNullOrEmpty(mainViewId)) {
            return Optional.empty();
        }

        return viewRegistry.findViewInfo(mainViewId);
    }
}
