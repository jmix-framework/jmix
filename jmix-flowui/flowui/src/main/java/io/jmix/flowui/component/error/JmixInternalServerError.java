/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.error;

import com.vaadin.flow.router.*;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.exception.UiExceptionHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

public class JmixInternalServerError extends InternalServerError {

    private static final Logger log = LoggerFactory.getLogger(JmixInternalServerError.class);

    protected UiExceptionHandlers uiExceptionHandlers;

    public JmixInternalServerError(UiExceptionHandlers uiExceptionHandlers) {
        this.uiExceptionHandlers = uiExceptionHandlers;
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<Exception> parameter) {
        forwardToPreviousView(event);

        handleException(new ErrorContext(event, parameter));

        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    protected void forwardToPreviousView(BeforeEnterEvent event) {
        Location location = event.getLocation();
        if (location.getSegments().size() > 1) {
            event.forwardTo(location.getFirstSegment());
        } else {
            RouteConfiguration.forSessionScope().getRoute(location.getPath())
                    .ifPresentOrElse(
                            viewClass -> navigateToParentLayout(viewClass, event),
                            () -> log.info("Cannot navigate to the parent layout"));
        }
    }

    protected void navigateToParentLayout(Class<?> viewClass, BeforeEnterEvent event) {
        RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope();
        List<RouteData> routes = routeConfiguration.getAvailableRoutes();

        RouteData parentRouteData = findRouteData(viewClass, routes)
                .flatMap(routeData -> findRouteData(routeData.getParentLayout(), routes))
                .orElse(null);

        if (parentRouteData != null) {
            String redirectUrl = routeConfiguration.getUrl(parentRouteData.getNavigationTarget());
            event.forwardTo(redirectUrl);
            return;
        }

        log.info("Cannot navigate to the parent layout {}", viewClass.getName());
    }

    protected Optional<RouteData> findRouteData(@Nullable Class<?> target, List<RouteData> routes) {
        if (target == null) {
            return Optional.empty();
        }
        return routes.stream()
                .filter(routeData -> target.equals(routeData.getNavigationTarget()))
                .findFirst();
    }

    protected void handleException(ErrorContext errorContext) {
        errorContext.handleExceptionAfterNavigation();
    }

    protected class ErrorContext {

        protected BeforeEnterEvent beforeEnterEvent;
        protected ErrorParameter<Exception> errorParameter;
        protected Registration uiAfterNagigationRegistration;

        public ErrorContext(BeforeEnterEvent beforeEnterEvent, ErrorParameter<Exception> errorParameter) {
            this.beforeEnterEvent = beforeEnterEvent;
            this.errorParameter = errorParameter;
        }

        public void handleExceptionAfterNavigation() {
            removeUiAfterNavigationRegistration();
            uiAfterNagigationRegistration = beforeEnterEvent.getUI().addAfterNavigationListener(event1 -> {
                uiExceptionHandlers.error(new ErrorEvent(errorParameter.getException()));
                removeUiAfterNavigationRegistration();
            });
        }

        protected void removeUiAfterNavigationRegistration() {
            if (uiAfterNagigationRegistration != null) {
                uiAfterNagigationRegistration.remove();
                uiAfterNagigationRegistration = null;
            }
        }
    }
}
