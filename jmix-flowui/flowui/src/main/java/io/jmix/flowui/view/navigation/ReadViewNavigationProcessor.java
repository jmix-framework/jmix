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

package io.jmix.flowui.view.navigation;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.ReadView;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.StandardReadView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;

import static java.util.Objects.requireNonNull;

/**
 * The navigation processor implementation that is responsible for processing navigation
 * to read views using a {@link ReadViewNavigator} instance.
 */
public class ReadViewNavigationProcessor extends AbstractNavigationProcessor<ReadViewNavigator<?>> {

    protected RouteSupport routeSupport;

    public ReadViewNavigationProcessor(ViewSupport viewSupport,
                                       ViewRegistry viewRegistry,
                                       ViewNavigationSupport navigationSupport,
                                       RouteSupport routeSupport) {
        super(viewSupport, viewRegistry, navigationSupport);

        this.routeSupport = routeSupport;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Class<? extends View> inferViewClass(ReadViewNavigator<?> navigator) {
        Class<?> entityClass = navigator.getEntity()
                .map(Object::getClass)
                .orElse((Class) navigator.getEntityClass());

        return viewRegistry.getReadViewInfo(entityClass).getControllerClass();
    }

    @Override
    protected RouteParameters getRouteParameters(ReadViewNavigator<?> navigator) {
        return navigator.getRouteParameters()
                .orElseGet(() -> {
                    Object entity = navigator.getEntity().orElseThrow(() -> new IllegalStateException(
                            String.format("Read view of %s cannot be opened, entity is not set",
                                    navigator.getEntityClass())));
                    Object id = requireNonNull(EntityValues.getId(entity));

                    return routeSupport.createRouteParameters(StandardReadView.DEFAULT_ROUTE_PARAM, id);
                });
    }

    @Override
    protected QueryParameters getQueryParameters(ReadViewNavigator<?> navigator) {
        QueryParameters queryParameters = super.getQueryParameters(navigator);

        if (!ReadView.class.isAssignableFrom(getViewClass(navigator))) {
            // View resolution fell back to a detail view, so it must be opened in the read-only mode.
            return routeSupport.addQueryParameter(queryParameters,
                    StandardDetailView.MODE_PARAM, StandardDetailView.MODE_READONLY);
        }

        return queryParameters;
    }

    @Override
    protected void fireAfterViewNavigation(ReadViewNavigator<?> navigator, View<?> view) {
        if (navigator instanceof SupportsAfterViewNavigationHandler<?> handlerSupport
                && handlerSupport.getAfterNavigationHandler().isPresent()) {
            super.fireAfterViewNavigation(navigator, view);
        }
    }
}
