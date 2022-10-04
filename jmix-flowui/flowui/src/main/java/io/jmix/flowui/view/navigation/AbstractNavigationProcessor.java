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

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.sys.ViewSupport;

public abstract class AbstractNavigationProcessor<N extends ViewNavigator> {

    protected ViewSupport viewSupport;
    protected ViewRegistry viewRegistry;
    protected ViewNavigationSupport navigationSupport;

    protected AbstractNavigationProcessor(ViewSupport viewSupport,
                                          ViewRegistry viewRegistry,
                                          ViewNavigationSupport navigationSupport) {
        this.viewSupport = viewSupport;
        this.viewRegistry = viewRegistry;
        this.navigationSupport = navigationSupport;
    }

    public void processNavigation(N navigator) {
        Class<? extends View> viewClass = getViewClass(navigator);
        RouteParameters routeParameters = getRouteParameters(navigator);
        QueryParameters queryParameters = getQueryParameters(navigator);

        navigator.getBackNavigationTarget().ifPresent(target ->
                viewSupport.registerBackNavigation(viewClass, target));

        navigationSupport.navigate(viewClass, routeParameters, queryParameters);
    }

    protected Class<? extends View> getViewClass(N navigator) {
        if (navigator.getViewId().isPresent()) {
            String viewId = navigator.getViewId().get();
            return viewRegistry.getViewInfo(viewId).getControllerClass();
        } else if (navigator.getViewClass().isPresent()) {
            return navigator.getViewClass().get();
        } else {
            return inferViewClass(navigator);
        }
    }

    protected abstract Class<? extends View> inferViewClass(N navigator);

    protected RouteParameters getRouteParameters(N navigator) {
        return navigator.getRouteParameters().orElse(RouteParameters.empty());
    }

    protected QueryParameters getQueryParameters(N navigator) {
        return navigator.getQueryParameters().orElse(QueryParameters.empty());
    }
}
