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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.AbstractViewNavigator;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

public class ViewNavigationDelegate<N extends AbstractViewNavigator> {

    protected static final String CURRENT_NAVIGATION_URL_ATTRIBUTE = "testCurrentNavigationUrl";

    protected ViewNavigationSupport navigationSupport;
    protected ViewSupport viewSupport;

    public ViewNavigationDelegate(ViewNavigationSupport navigationSupport, ViewSupport viewSupport) {
        this.navigationSupport = navigationSupport;
        this.viewSupport = viewSupport;
    }

    public void processNavigation(N navigator, Class<? extends View<?>> viewClass,
                                  RouteParameters routeParameters, QueryParameters queryParameters,
                                  Consumer<View<?>> fireAfterViewNavigation) {
        if (navigator.isBackwardNavigation()) {
            URL url = fetchCurrentUrl();
            Optional<View<?>> view = navigationSupport.navigate(viewClass, routeParameters, queryParameters);
            if (view.isPresent()) {
                viewSupport.registerBackwardNavigation(viewClass, url);
                fireAfterViewNavigation.accept(view.get());
            }
        } else {
            Optional<View<?>> view = navigationSupport.navigate(viewClass, routeParameters, queryParameters);
            view.ifPresent(fireAfterViewNavigation);
        }

        storeCurrentNavigation(viewClass, routeParameters, queryParameters);
    }

    protected URL fetchCurrentUrl() {
        VaadinSession session = UI.getCurrent().getSession();
        URL url = (URL) session.getAttribute(CURRENT_NAVIGATION_URL_ATTRIBUTE);
        return url != null ? url : getHostUrl();
    }

    protected void storeCurrentNavigation(Class<? extends View<?>> viewClass, RouteParameters routeParameters,
                                          QueryParameters queryParameters) {
        String path = getRouteConfiguration().getUrl(viewClass, routeParameters);
        Location location = new Location(path, queryParameters);


        String hostUrl = getProtocol() + getHostAddress() + "/" + location.getPathWithQueryParameters();
        URL url;
        try {
            url = new URL(hostUrl);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Cannot register backward url: " + hostUrl, e);
        }

        VaadinSession session = UI.getCurrent().getSession();
        session.setAttribute(CURRENT_NAVIGATION_URL_ATTRIBUTE, url);
    }

    protected RouteConfiguration getRouteConfiguration() {
        return RouteConfiguration.forSessionScope();
    }

    protected URL getHostUrl() {
        try {
            return new URL(getProtocol() + getHostAddress());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Cannot get host URL", e);
        }
    }

    /**
     * During the processing URL for navigation Vaadin ignores protocol and host.
     * The default value is "http://".
     *
     * @return protocol
     */
    protected String getProtocol() {
        return "http://";
    }

    /**
     * During the processing URL for navigation Vaadin ignores protocol and host.
     * The default value is "localhost".
     *
     * @return host address
     */
    protected String getHostAddress() {
        return "localhost";
    }
}
