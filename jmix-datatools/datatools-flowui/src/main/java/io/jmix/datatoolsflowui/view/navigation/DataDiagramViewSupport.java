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

package io.jmix.datatoolsflowui.view.navigation;

import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.LocationUtil;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import com.vaadin.flow.spring.security.VaadinDefaultRequestCache;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.Nullable;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.SavedRequest;

public class DataDiagramViewSupport {

    protected UiProperties uiProperties;
    protected ViewNavigators viewNavigators;
    protected VaadinDefaultRequestCache requestCache;
    protected ViewRegistry viewRegistry;

    protected void showInitialView(VaadinServletRequest request, VaadinServletResponse response) {
        Location location = getRedirectLocation(request, response);
        if (location == null || isRedirectToInitialView(location)) {
            navigateToInitialView();
        } else {
            UI.getCurrent().navigate(location.getPath(), location.getQueryParameters());
        }
    }

    protected void navigateToDefaultView(String defaultViewId) {
        ViewInfo viewInfo = viewRegistry.getViewInfo(defaultViewId);
        if (DetailView.class.isAssignableFrom(viewInfo.getControllerClass())) {
            viewNavigators.detailView(UiComponentUtils.getCurrentView(), getEntityClass(viewInfo))
                    .withBackwardNavigation(false)
                    .navigate();
        } else {
            viewNavigators.view(UiComponentUtils.getCurrentView(), defaultViewId)
                    .navigate();
        }
    }

    protected Class<?> getEntityClass(ViewInfo viewInfo) {
        return DetailViewTypeExtractor.extractEntityClass(viewInfo)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Failed to determine entity type for detail view '%s'", viewInfo.getId())));
    }

    protected void navigateToInitialView() {
        String defaultViewId = uiProperties.getDefaultViewId();
        if (Strings.isNullOrEmpty(defaultViewId)) {
            navigateToMainView();
        } else {
            navigateToDefaultView(defaultViewId);
        }
    }

    @Nullable
    protected Location getRedirectLocation(VaadinServletRequest request, VaadinServletResponse response) {
        HttpServletRequest httpServletRequest = request.getHttpServletRequest();
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            return null;
        }

        String redirectTarget = (String) session.getAttribute(NavigationAccessControl.SESSION_STORED_REDIRECT);
        if (redirectTarget != null) {
            return new Location(redirectTarget);
        }

        SavedRequest savedRequest = requestCache.getRequest(httpServletRequest, response);
        if (savedRequest != null) {
            if (savedRequest instanceof DefaultSavedRequest defaultSavedRequest) {
                //build location by servlet path and query params only (without host, port etc.)
                //because later we need to check if it is main view location
                //and RouteConfiguration.getRoute(String) doesn't support full URLs
                //like one returned from savedRequest.getRedirectUrl()
                QueryParameters queryParameters = QueryParameters.fromString(defaultSavedRequest.getQueryString());
                if (isPathAvailable(defaultSavedRequest.getServletPath())) {
                    return new Location(defaultSavedRequest.getServletPath(), queryParameters);
                }
                return null;
            } else {
                return new Location(savedRequest.getRedirectUrl());
            }
        }

        return null;
    }

    protected void navigateToMainView() {
        String mainViewId = uiProperties.getMainViewId();
        viewNavigators.view(UiComponentUtils.getCurrentView(), mainViewId)
                .navigate();
    }

    protected boolean isPathAvailable(@Nullable String path) {
        String normalizedPath = LocationUtil.ensureRelativeNonNull(path);

        RouteRegistry handledRegistry = getRouteConfiguration().getHandledRegistry();

        return handledRegistry.getNavigationRouteTarget(normalizedPath).hasTarget();
    }

    protected RouteConfiguration getRouteConfiguration() {
        return viewRegistry.getRouteConfiguration();
    }

    protected boolean isRedirectToInitialView(Location redirectLocation) {
        if (!redirectLocation.getQueryParameters().getParameters().isEmpty()) {
            return false;
        }

        String mainViewId = uiProperties.getMainViewId();
        Class<? extends View<?>> mainViewClass = viewRegistry.getViewInfo(mainViewId)
                .getControllerClass();

        RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope();
        return routeConfiguration.getRoute(redirectLocation.getPathWithQueryParameters())
                .map(mainViewClass::isAssignableFrom)
                .orElse(false);
    }
}
