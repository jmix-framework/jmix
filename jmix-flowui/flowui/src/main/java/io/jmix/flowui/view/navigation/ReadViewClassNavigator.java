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
import io.jmix.flowui.view.View;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Facilitates navigation to a read view of a specific type with additional configuration options.
 * This class extends {@link ReadViewNavigator} to provide the ability to specify a particular view class
 * and to handle a callback after successful navigation to a view.
 *
 * @param <E> the type of the entity shown by the view
 * @param <V> the type of the view being navigated to
 */
@NullMarked
public class ReadViewClassNavigator<E, V extends View<?>> extends ReadViewNavigator<E>
        implements SupportsAfterViewNavigationHandler<V> {

    protected Class<V> viewClass;

    @Nullable
    protected Consumer<AfterViewNavigationEvent<V>> afterNavigationHandler;

    public ReadViewClassNavigator(View<?> origin,
                                  Class<E> entityClass,
                                  Consumer<? extends ReadViewNavigator<E>> handler,
                                  Class<V> viewClass) {
        super(origin, entityClass, handler);

        this.viewClass = viewClass;
    }

    protected ReadViewClassNavigator(ReadViewNavigator<E> viewNavigator,
                                     Class<V> viewClass) {
        super(viewNavigator);

        this.viewClass = viewClass;
    }

    @Override
    public ReadViewClassNavigator<E, V> readEntity(E entity) {
        super.readEntity(entity);
        return this;
    }

    @Override
    public ReadViewClassNavigator<E, V> withViewId(@Nullable String viewId) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " doesn't support 'viewId'");
    }

    @Override
    public ReadViewClassNavigator<E, V> withRouteParameters(@Nullable RouteParameters routeParameters) {
        super.withRouteParameters(routeParameters);
        return this;
    }

    @Override
    public ReadViewClassNavigator<E, V> withQueryParameters(@Nullable QueryParameters queryParameters) {
        super.withQueryParameters(queryParameters);
        return this;
    }

    @Override
    public ReadViewClassNavigator<E, V> withBackwardNavigation(boolean backwardNavigation) {
        super.withBackwardNavigation(backwardNavigation);
        return this;
    }

    /**
     * Adds a handler that will be invoked if navigation to a view actually happened.
     * <p>
     * Note: this handler is invoked after all lifecycle events of a view.
     * <pre>{@code
     *     viewNavigators.readView(Foo.class)
     *         .readEntity(someFoo)
     *         .withViewClass(FooReadView.class)
     *         .withAfterNavigationHandler(navigationEvent -> {
     *             FooReadView view = navigationEvent.getView();
     *             view.setBar("bar");
     *         }).navigate();
     * }</pre>
     *
     * @param handler a handler to set
     * @return this instance for chaining
     */
    public ReadViewClassNavigator<E, V> withAfterNavigationHandler(
            Consumer<AfterViewNavigationEvent<V>> handler) {
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
