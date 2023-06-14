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

package io.jmix.flowui.testassist.navigation;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.navigation.*;

public class TestDetailViewnavigationProcessor extends DetailViewNavigationProcessor {

    protected ViewNavigationDelegate<DetailViewNavigator<?>> navigationDelegate;

    public TestDetailViewnavigationProcessor(ViewSupport viewSupport, ViewRegistry viewRegistry,
                                             ViewNavigationSupport navigationSupport, RouteSupport routeSupport,
                                             Metadata metadata, MetadataTools metadataTools,
                                             ViewNavigationDelegate<DetailViewNavigator<?>> navigationDelegate) {
        super(viewSupport, viewRegistry, navigationSupport, routeSupport, metadata, metadataTools);

        this.navigationDelegate = navigationDelegate;
    }

    @Override
    public void processNavigation(DetailViewNavigator<?> navigator) {
        Class<? extends View<?>> viewClass = (Class<? extends View<?>>) getViewClass(navigator);
        RouteParameters routeParameters = getRouteParameters(navigator);
        QueryParameters queryParameters = getQueryParameters(navigator);

        navigationDelegate.processNavigation(navigator, viewClass, routeParameters, queryParameters,
                view -> fireAfterViewNavigation(navigator, view));
    }
}
