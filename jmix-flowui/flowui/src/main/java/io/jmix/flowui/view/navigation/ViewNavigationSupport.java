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

package io.jmix.flowui.view.navigation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;

import java.util.List;
import java.util.Optional;

/**
 * Utility class that provides support for navigating between views.
 */
@org.springframework.stereotype.Component("flowui_ViewNavigationSupport")
public class ViewNavigationSupport {

    protected ViewRegistry viewRegistry;

    public ViewNavigationSupport(ViewRegistry viewRegistry) {
        this.viewRegistry = viewRegistry;
    }

    /**
     * Navigates to the specified navigation target with empty route and query parameters.
     *
     * @param <T>              the type of the navigation target
     * @param navigationTarget the class of the component to navigate to
     * @return an {@link Optional} containing the view instance, if navigation actually happened,
     * otherwise an empty {@link Optional}
     */
    public <T extends Component> Optional<T> navigate(Class<? extends T> navigationTarget) {
        return navigate(navigationTarget, RouteParameters.empty(), QueryParameters.empty());
    }

    /**
     * Navigates to the specified navigation target using the provided route parameters.
     *
     * @param <T>              the type of the navigation target
     * @param navigationTarget the class of the component to navigate to
     * @param routeParameters  the route parameters to be used during navigation
     * @return an {@link Optional} containing the view instance, if navigation actually happened,
     * otherwise an empty {@link Optional}
     */
    public <T extends Component> Optional<T> navigate(Class<? extends T> navigationTarget,
                                                      RouteParameters routeParameters) {
        return navigate(navigationTarget, routeParameters, QueryParameters.empty());
    }

    /**
     * Navigates to the specified navigation target using the provided route and query parameters.
     *
     * @param <T>              the type of the navigation target
     * @param navigationTarget the class of the component to navigate to
     * @param routeParameters  the route parameters to be used during navigation
     * @param queryParameters  the query parameters to be used during navigation
     * @return an {@link Optional} containing the view instance, if navigation actually happened,
     * otherwise an empty {@link Optional}
     */
    public <T extends Component> Optional<T> navigate(Class<? extends T> navigationTarget,
                                                      RouteParameters routeParameters,
                                                      QueryParameters queryParameters) {

        String url = getRouteConfiguration().getUrl(navigationTarget, routeParameters);
        UI.getCurrent().navigate(url, queryParameters);
        return findCurrentNavigationTarget(navigationTarget);
    }

    /**
     * Finds the current navigation target of the specified type in the current UI's
     * active router target chain.
     *
     * @param <T>              the type of the navigation target
     * @param navigationTarget the class of the component representing the navigation target
     * @return an {@link Optional} containing the found navigation target if available,
     * otherwise an empty {@link Optional}
     */
    public <T extends Component> Optional<T> findCurrentNavigationTarget(Class<? extends T> navigationTarget) {
        return findCurrentNavigationTarget(UI.getCurrent(), navigationTarget);
    }

    /**
     * Finds the current navigation target of the specified type in the provided UI's
     * active router target chain.
     *
     * @param <T>              the type of the navigation target
     * @param ui               the UI instance in which the search is performed
     * @param navigationTarget the class of the component representing the navigation target
     * @return an {@link Optional} containing the found navigation target if available,
     * otherwise an empty {@link Optional}
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> Optional<T> findCurrentNavigationTarget(UI ui, Class<? extends T> navigationTarget) {
        // CAUTION: copied from com.vaadin.flow.component.UI.findCurrentNavigationTarget [last update Vaadin 24.7.3]
        List<HasElement> activeRouterTargetsChain = ui.getInternals().getActiveRouterTargetsChain();
        for (HasElement element : activeRouterTargetsChain) {
            if (navigationTarget.isAssignableFrom(element.getClass())) {
                return Optional.of((T) element);
            }
        }

        return Optional.empty();
    }

    /**
     * Navigates to the specified view identified by its ID
     * with empty route and query parameters.
     *
     * @param viewId the identifier of the view to navigate to
     */
    public void navigate(String viewId) {
        navigate(viewId, RouteParameters.empty(), QueryParameters.empty());
    }

    /**
     * Navigates to the specified view identified by its ID using the provided route parameters
     * and empty query parameters.
     *
     * @param viewId          the identifier of the view to navigate to
     * @param routeParameters the route parameters to be used during navigation
     */
    public void navigate(String viewId, RouteParameters routeParameters) {
        navigate(viewId, routeParameters, QueryParameters.empty());
    }

    /**
     * Navigates to the specified view identified by its ID using the provided route
     * and query parameters.
     *
     * @param viewId          the identifier of the view to navigate to
     * @param routeParameters the route parameters to be used during navigation
     * @param queryParameters the query parameters to be used during navigation
     */
    public void navigate(String viewId, RouteParameters routeParameters, QueryParameters queryParameters) {
        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
        navigate(viewInfo.getControllerClass(), routeParameters, queryParameters);
    }

    /**
     * Navigates to the specified navigation target using the given parameter.
     *
     * @param navigationTarget the class of the component to navigate to
     * @param parameter        the parameter to pass to the navigation target
     * @param <T>              the type of the parameter used for navigation
     * @param <C>              the type of the navigation target
     */
    public <T, C extends Component & HasUrlParameter<T>> void navigate(Class<? extends C> navigationTarget,
                                                                       T parameter) {

        navigate(navigationTarget, parameter, QueryParameters.empty());
    }

    /**
     * Navigates to the specified navigation target using the provided parameter
     * and query parameters.
     *
     * @param navigationTarget the class of the component to navigate to
     * @param parameter        the parameter to pass to the navigation target
     * @param queryParameters  the query parameters to be used during navigation
     * @param <T>              the type of the parameter used for navigation
     * @param <C>              the type of the navigation target
     */
    public <T, C extends Component & HasUrlParameter<T>> void navigate(Class<? extends C> navigationTarget,
                                                                       T parameter,
                                                                       QueryParameters queryParameters) {
        String url = getRouteConfiguration().getUrl(navigationTarget, parameter);
        UI.getCurrent().navigate(url, queryParameters);
    }

    protected RouteConfiguration getRouteConfiguration() {
        return RouteConfiguration.forSessionScope();
    }
}
