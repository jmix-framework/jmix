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
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides a fluent interface to configure navigation parameters and navigate to a {@link View}.
 * <p>
 * An instance of this class should be obtained through {@link ViewNavigators#view(Class)}.
 */
public class ViewClassNavigator<V extends View<?>> extends ViewNavigator
        implements SupportsAfterViewNavigationHandler<V> {

    protected Class<V> viewClass;

    protected Consumer<SupportsAfterViewNavigationHandler.AfterViewNavigationEvent<V>> afterNavigationHandler;

    public ViewClassNavigator(Consumer<? extends ViewNavigator> handler,
                              Class<V> viewClass) {
        super(handler);

        this.viewClass = viewClass;
    }

    protected ViewClassNavigator(ViewNavigator viewNavigator,
                                 Class<V> viewClass) {
        super(viewNavigator);

        this.viewClass = viewClass;
    }

    @Override
    public ViewClassNavigator<V> withViewId(@Nullable String viewId) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " doesn't support 'viewId'");
    }

    @Override
    public ViewClassNavigator<V> withRouteParameters(@Nullable RouteParameters routeParameters) {
        super.withRouteParameters(routeParameters);
        return this;
    }

    @Override
    public ViewClassNavigator<V> withQueryParameters(@Nullable QueryParameters queryParameters) {
        super.withQueryParameters(queryParameters);
        return this;
    }

    @Override
    public ViewClassNavigator<V> withBackwardNavigation(boolean backwardNavigation) {
        super.withBackwardNavigation(backwardNavigation);
        return this;
    }

    /**
     * Adds a handler that will be invoked if navigation to a view actually happened.
     * <p>
     * Note: this handler if in invoked after all lifecycle events of a view.
     *
     * @param handler a handler to set
     * @return this instance for chaining
     */
    public ViewClassNavigator<V> withAfterNavigationHandler(Consumer<AfterViewNavigationEvent<V>> handler) {
        this.afterNavigationHandler = handler;
        return this;
    }

    @Override
    public Optional<Consumer<AfterViewNavigationEvent<V>>> getAfterNavigationHandler() {
        return Optional.ofNullable(afterNavigationHandler);
    }

    @Override
    public Optional<Class<? extends View>> getViewClass() {
        return Optional.of(viewClass);
    }
}
