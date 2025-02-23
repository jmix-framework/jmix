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

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.DialogWindowBuilder;
import io.jmix.flowui.view.navigation.AbstractViewNavigator;

import java.util.function.Consumer;
import java.util.function.Function;

public class ViewBuilderAdapter<V extends View<?>> extends ViewBuilder<V> {

    protected RouteParameters routeParameters = RouteParameters.empty();
    protected QueryParameters queryParameters = QueryParameters.empty();

    public ViewBuilderAdapter(AbstractViewNavigator viewNavigator,
                              Class<V> viewClass,
                              Function<ViewBuilder<V>, V> handler,
                              Consumer<ViewOpeningContext> openHandler) {
        super(viewNavigator.getOrigin(), viewClass, handler, openHandler);

        applyFrom(viewNavigator);
    }

    public ViewBuilderAdapter(DialogWindowBuilder<V> windowBuilder,
                              Class<V> viewClass,
                              Function<ViewBuilder<V>, V> handler,
                              Consumer<ViewOpeningContext> openHandler) {
        super(windowBuilder.getOrigin(), viewClass, handler, openHandler);

        applyFrom(windowBuilder);
    }

    protected void applyFrom(AbstractViewNavigator viewNavigator) {
        ViewBuilderAdapterUtil.apply(this, viewNavigator);

        viewNavigator.getRouteParameters().ifPresent(routeParameters ->
                this.routeParameters = routeParameters);
        viewNavigator.getQueryParameters().ifPresent(queryParameters ->
                this.queryParameters = queryParameters);
    }

    protected void applyFrom(DialogWindowBuilder<V> windowBuilder) {
        ViewBuilderAdapterUtil.apply(this, windowBuilder);
    }

    @Override
    protected ViewOpeningContext createViewOpeningContext() {
        return ViewOpeningContext.create(builtView, openMode)
                .withRouteParameters(routeParameters)
                .withQueryParameters(queryParameters);
    }
}
