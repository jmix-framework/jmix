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
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.stereotype.Component;

import static io.jmix.flowui.view.StandardDetailView.MODE_PARAM;
import static io.jmix.flowui.view.StandardDetailView.MODE_READONLY;
import static java.util.Objects.requireNonNull;

@Internal
@Component("flowui_DetailViewNavigationProcessor")
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
    protected Class<? extends View> inferViewClass(DetailViewNavigator<?> navigator) {
        return viewRegistry.getDetailViewInfo(navigator.getEntityClass()).getControllerClass();
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
            switch (navigator.getMode()) {
                case CREATE:
                    return generateNewEntityRouteParameters(navigator);
                case EDIT:
                    return generateEditEntityRouteParameters(navigator);
                default:
                    throw new IllegalStateException("Unknown detail view mode: " + navigator.getMode());
            }
        });
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
