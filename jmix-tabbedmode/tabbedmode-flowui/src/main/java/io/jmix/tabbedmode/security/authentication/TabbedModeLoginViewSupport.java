/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.security.authentication;

import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import io.jmix.flowui.view.View;
import io.jmix.securityflowui.authentication.LoginViewSupport;
import io.jmix.tabbedmode.JmixUI;
import io.jmix.tabbedmode.navigation.RedirectHandler;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("tabmod_TabbedModeLoginViewSupport")
public class TabbedModeLoginViewSupport extends LoginViewSupport {

    @Override
    protected void showInitialView(VaadinServletRequest request, VaadinServletResponse response) {
        Location location = getRedirectLocation(request, response);
        if (location != null
                && !isRedirectToInitialView(location)
                && !isRedirectToDefaultView(location)) {
            JmixUI ui = JmixUI.getCurrent();
            if (ui != null) {
                RedirectHandler redirectHandler = ui.getRedirectHandler();
                redirectHandler.schedule(location);
            }
        }

        navigateToMainView();
    }

    protected boolean isRedirectToDefaultView(Location redirectLocation) {
        String defaultViewId = uiProperties.getDefaultViewId();
        if (defaultViewId == null) {
            return false;
        }

        if (!redirectLocation.getQueryParameters().getParameters().isEmpty()) {
            return false;
        }

        Class<? extends View<?>> defaultViewClass = viewRegistry.getViewInfo(defaultViewId)
                .getControllerClass();

        RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope();
        return routeConfiguration.getRoute(redirectLocation.getPathWithQueryParameters())
                .map(defaultViewClass::isAssignableFrom)
                .orElse(false);
    }
}
