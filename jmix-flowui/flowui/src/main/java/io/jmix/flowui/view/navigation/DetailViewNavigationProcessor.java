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
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.DetailViewMode;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;

import static io.jmix.flowui.view.StandardDetailView.MODE_PARAM;
import static io.jmix.flowui.view.StandardDetailView.MODE_READONLY;
import static java.util.Objects.requireNonNull;

/**
 * The navigation processor implementation that is responsible for processing navigation
 * to specific views using a {@link DetailViewNavigator} instance.
 */
public class DetailViewNavigationProcessor extends AbstractNavigationProcessor<DetailViewNavigator<?>> {

    protected RouteSupport routeSupport;

    public DetailViewNavigationProcessor(ViewSupport viewSupport,
                                         ViewRegistry viewRegistry,
                                         ViewNavigationSupport navigationSupport,
                                         RouteSupport routeSupport) {
        super(viewSupport, viewRegistry, navigationSupport);

        this.routeSupport = routeSupport;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Class<? extends View> inferViewClass(DetailViewNavigator<?> navigator) {
        Class<?> entityClass;
        if (navigator.getMode() == DetailViewMode.CREATE) {
            entityClass = navigator.getEntityClass();
        } else {
            entityClass = navigator.getEditedEntity()
                    .map(Object::getClass)
                    .orElse((Class) navigator.getEntityClass());
        }
        return viewRegistry.getDetailViewInfo(entityClass).getControllerClass();
    }

    @Override
    protected QueryParameters getQueryParameters(DetailViewNavigator<?> navigator) {
        if (navigator.isReadOnly()) {
            QueryParameters queryParameters = navigator.getQueryParameters()
                    .orElse(QueryParameters.empty());
            return routeSupport.addQueryParameter(queryParameters, MODE_PARAM, MODE_READONLY);
        } else {
            return super.getQueryParameters(navigator);
        }
    }

    @Override
    protected RouteParameters getRouteParameters(DetailViewNavigator<?> navigator) {
        return navigator.getRouteParameters().orElseGet(() -> {
            return switch (navigator.getMode()) {
                case CREATE -> generateNewEntityRouteParameters(navigator);
                case EDIT -> generateEditEntityRouteParameters(navigator);
            };
        });
    }

    @Override
    protected void fireAfterViewNavigation(DetailViewNavigator<?> navigator, View<?> view) {
        if (navigator instanceof SupportsAfterViewNavigationHandler<?>
                && ((SupportsAfterViewNavigationHandler<?>) navigator).getAfterNavigationHandler().isPresent()) {
            super.fireAfterViewNavigation(navigator, view);
        }
    }

    protected RouteParameters generateNewEntityRouteParameters(DetailViewNavigator<?> navigator) {
        return routeSupport.createRouteParameters("id", StandardDetailView.NEW_ENTITY_ID);
    }

    protected RouteParameters generateEditEntityRouteParameters(DetailViewNavigator<?> navigator) {
        Object entity = navigator.getEditedEntity().orElseThrow(() -> new IllegalStateException(
                String.format("Detail View of %s cannot be open with mode EDIT, entity is not set",
                        navigator.getEntityClass())));

        Object id = requireNonNull(EntityValues.getId(entity));
        return routeSupport.createRouteParameters("id", id);
    }
}
