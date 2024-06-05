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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.navigation.SupportsAfterViewNavigationHandler.AfterViewNavigationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;

public abstract class AbstractNavigationProcessor<N extends AbstractViewNavigator> {

    private static final Logger log = LoggerFactory.getLogger(AbstractNavigationProcessor.class);

    protected ViewSupport viewSupport;
    protected ViewRegistry viewRegistry;
    protected ViewNavigationSupport navigationSupport;

    protected Cache<View<?>, Registration> detachRegistrationsCache = CacheBuilder.newBuilder()
            .maximumSize(5)
            .build();

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

        View<?> origin = navigator.getOrigin();
        // If navigation is interrupted and never proceed, e.g. by clicking 'Cancel', sequential
        // navigation attempts from the same view will produce multiple DetachEvent subscriptions,
        // so we need to remember the last subscription for the view and remove it.
        unregisterViewDetachListener(origin);

        if (navigator.isBackwardNavigation()) {
            log.trace("Fetching current URL for backward navigation");
            fetchCurrentURL(url -> {
                log.trace("Fetched URL: {}", url.toString());

                Registration detachRegistration = ViewControllerUtils.addDetachListener(origin, __ -> {
                    Optional<View> view = navigationSupport.findCurrentNavigationTarget(viewClass);
                    if (view.isPresent()) {
                        viewSupport.registerBackwardNavigation(viewClass, url);
                        fireAfterViewNavigation(navigator, view.get());
                    } else {
                        log.warn("Can't find current navigation target: {}. Cannot set backward navigation and fire {}",
                                viewClass.getName(), AfterViewNavigationEvent.class.getSimpleName());
                    }
                    unregisterViewDetachListener(origin);
                });
                detachRegistrationsCache.put(origin, detachRegistration);
                navigationSupport.navigate(viewClass, routeParameters, queryParameters);
            });
        } else {
            Registration detachRegistration = ViewControllerUtils.addDetachListener(origin, __ -> {
                Optional<View> view = navigationSupport.findCurrentNavigationTarget(viewClass);
                if (view.isPresent()) {
                    fireAfterViewNavigation(navigator, view.get());
                } else {
                    log.warn("Can't find current navigation target: {}. Cannot fire {}",
                            viewClass.getName(), AfterViewNavigationEvent.class.getSimpleName());
                }
                unregisterViewDetachListener(origin);
            });
            detachRegistrationsCache.put(origin, detachRegistration);
            navigationSupport.navigate(viewClass, routeParameters, queryParameters);
        }
    }

    protected void unregisterViewDetachListener(View<?> origin) {
        Registration registration = detachRegistrationsCache.getIfPresent(origin);
        if (registration != null) {
            registration.remove();
            detachRegistrationsCache.invalidate(origin);
        }
    }

    protected void fireAfterViewNavigation(N navigator, View<?> view) {
        if (navigator instanceof SupportsAfterViewNavigationHandler) {
            ((SupportsAfterViewNavigationHandler<?>) navigator)
                    .getAfterNavigationHandler().ifPresent(handler ->
                            handler.accept(new AfterViewNavigationEvent(this, view)));
        }
    }

    protected Class<? extends View> getViewClass(N navigator) {
        if (navigator.getViewId().isPresent()) {
            String viewId = navigator.getViewId().get();
            return viewRegistry.getViewInfo(viewId).getControllerClass();
        } else if (navigator.getViewClass().isPresent()) {
            String id = ViewDescriptorUtils.getInferredViewId(navigator.getViewClass().get());
            return viewRegistry.getViewInfo(id).getControllerClass();
        } else {
            return inferViewClass(navigator);
        }
    }

    protected void fetchCurrentURL(SerializableConsumer<URL> callback) {
        UI.getCurrent().getPage().fetchCurrentURL(callback);
    }

    protected abstract Class<? extends View> inferViewClass(N navigator);

    protected RouteParameters getRouteParameters(N navigator) {
        return navigator.getRouteParameters().orElse(RouteParameters.empty());
    }

    protected QueryParameters getQueryParameters(N navigator) {
        return navigator.getQueryParameters().orElse(QueryParameters.empty());
    }
}
