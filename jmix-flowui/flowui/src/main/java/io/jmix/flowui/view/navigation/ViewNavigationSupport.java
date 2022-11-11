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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;

@org.springframework.stereotype.Component("flowui_ViewNavigationSupport")
public class ViewNavigationSupport {

    protected ViewRegistry viewRegistry;

    public ViewNavigationSupport(ViewRegistry viewRegistry) {
        this.viewRegistry = viewRegistry;
    }

    public void navigate(Class<? extends Component> navigationTarget) {
        navigate(navigationTarget, RouteParameters.empty(), QueryParameters.empty());
    }

    public void navigate(Class<? extends Component> navigationTarget,
                         RouteParameters routeParameters) {
        navigate(navigationTarget, routeParameters, QueryParameters.empty());
    }

    public void navigate(Class<? extends Component> navigationTarget,
                         RouteParameters routeParameters,
                         QueryParameters queryParameters) {

        String url = getRouteConfiguration().getUrl(navigationTarget, routeParameters);
        UI.getCurrent().navigate(url, queryParameters);
    }

    public void navigate(String viewId) {
        navigate(viewId, RouteParameters.empty(), QueryParameters.empty());
    }

    public void navigate(String viewId, RouteParameters routeParameters) {
        navigate(viewId, routeParameters, QueryParameters.empty());
    }

    public void navigate(String viewId,
                         RouteParameters routeParameters,
                         QueryParameters queryParameters) {

        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
        navigate(viewInfo.getControllerClass(), routeParameters, queryParameters);
    }

    public <T, C extends Component & HasUrlParameter<T>> void navigate(Class<? extends C> navigationTarget,
                                                                       T parameter) {

        navigate(navigationTarget, parameter, QueryParameters.empty());
    }

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
