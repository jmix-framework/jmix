/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.builder;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.view.ReadOnlyAwareView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.DetailWindowBuilder;
import io.jmix.flowui.view.navigation.DetailViewNavigator;
import io.jmix.tabbedmode.builder.navigation.EnhancedViewNavigator;

import java.util.function.Consumer;
import java.util.function.Function;

public class DetailViewBuilderAdapter<E, V extends View<?>> extends DetailViewBuilder<E, V> {

    protected RouteParameters routeParameters = RouteParameters.empty();
    protected QueryParameters queryParameters = QueryParameters.empty();

    public DetailViewBuilderAdapter(DetailViewNavigator<E> viewNavigator,
                                    Class<V> viewClass,
                                    Function<DetailViewBuilder<E, V>, V> buildHandler,
                                    Consumer<ViewOpeningContext> openHandler) {
        super(viewNavigator.getOrigin(), viewNavigator.getEntityClass(), viewClass, buildHandler, openHandler);

        applyFrom(viewNavigator);
    }

    public DetailViewBuilderAdapter(DetailWindowBuilder<E, V> windowBuilder,
                                    Class<V> viewClass,
                                    Function<DetailViewBuilder<E, V>, V> buildHandler,
                                    Consumer<ViewOpeningContext> openHandler) {
        super(windowBuilder.getOrigin(), windowBuilder.getEntityClass(), viewClass, buildHandler, openHandler);

        applyFrom(windowBuilder);
    }

    @SuppressWarnings({"unchecked"})
    protected void applyFrom(DetailViewNavigator<E> viewNavigator) {
        ViewBuilderAdapterUtil.apply(this, viewNavigator);

        editedEntity = viewNavigator.getEditedEntity().orElse(null);
        mode = viewNavigator.getMode();

        if (viewNavigator.isReadOnly()) {
            // View navigators don't provide the ViewConfigurer handler,
            // so it's safe to set one here
            withViewConfigurer(this::configureReadOnly);
        }

        if (viewNavigator instanceof EnhancedViewNavigator<?> enhancedViewNavigator) {
            listDataComponent = (ListDataComponent<E>) enhancedViewNavigator.getListDataComponent().orElse(null);
            field = (HasValue<?, E>) enhancedViewNavigator.getField().orElse(null);
        }

        viewNavigator.getRouteParameters().ifPresent(routeParameters ->
                this.routeParameters = routeParameters);
        viewNavigator.getQueryParameters().ifPresent(queryParameters ->
                this.queryParameters = queryParameters);
    }

    protected void applyFrom(DetailWindowBuilder<E, V> windowBuilder) {
        ViewBuilderAdapterUtil.apply(this, windowBuilder);

        editedEntity = windowBuilder.getEditedEntity().orElse(null);
        newEntity = windowBuilder.getNewEntity().orElse(null);
        mode = windowBuilder.getMode();

        container = windowBuilder.getContainer().orElse(null);
        field = windowBuilder.getField().orElse(null);
        listDataComponent = windowBuilder.getListDataComponent().orElse(null);

        initializer = windowBuilder.getInitializer().orElse(null);
        transformation = windowBuilder.getTransformation().orElse(null);
        parentDataContext = windowBuilder.getParentDataContext().orElse(null);

        addFirst = windowBuilder.getAddFirst();
    }

    protected void configureReadOnly(V view) {
        if (view instanceof ReadOnlyAwareView) {
            ((ReadOnlyAwareView) view).setReadOnly(true);
        } else {
            throw new IllegalStateException(String.format("%s '%s' does not implement %s: %s",
                    View.class.getSimpleName(), view.getId().orElse(null),
                    ReadOnlyAwareView.class.getSimpleName(), view.getClass()));
        }
    }

    @Override
    protected ViewOpeningContext createViewOpeningContext() {
        return ViewOpeningContext.create(builtView, openMode)
                .withRouteParameters(routeParameters)
                .withQueryParameters(queryParameters);
    }
}
