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
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public class DetailViewClassNavigator<E, V extends View<?>> extends DetailViewNavigator<E>
        implements SupportsAfterViewNavigationHandler<V> {

    protected Class<V> viewClass;

    protected Consumer<AfterViewNavigationEvent<V>> afterNavigationHandler;

    public DetailViewClassNavigator(Class<E> entityClass, Consumer<? extends DetailViewNavigator<E>> handler,
                                    Class<V> viewClass) {
        super(entityClass, handler);

        this.viewClass = viewClass;
    }

    protected DetailViewClassNavigator(DetailViewNavigator<E> viewNavigator,
                                       Class<V> viewClass) {
        super(viewNavigator);

        this.viewClass = viewClass;
    }

    @Override
    public DetailViewClassNavigator<E, V> newEntity() {
        super.newEntity();
        return this;
    }

    @Override
    public DetailViewClassNavigator<E, V> editEntity(E entity) {
        super.editEntity(entity);
        return this;
    }

    @Override
    public DetailViewClassNavigator<E, V> withViewId(@Nullable String viewId) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " doesn't support 'viewId'");
    }

    @Override
    public DetailViewClassNavigator<E, V> withRouteParameters(@Nullable RouteParameters routeParameters) {
        super.withRouteParameters(routeParameters);
        return this;
    }

    @Override
    public DetailViewClassNavigator<E, V> withQueryParameters(@Nullable QueryParameters queryParameters) {
        super.withQueryParameters(queryParameters);
        return this;
    }

    @Override
    public DetailViewClassNavigator<E, V> withBackwardNavigation(boolean backwardNavigation) {
        super.withBackwardNavigation(backwardNavigation);
        return this;
    }

    @Override
    public DetailViewClassNavigator<E, V> withReadOnly(boolean readOnly) {
        super.withReadOnly(readOnly);
        return this;
    }

    /**
     * Adds a handler that will be invoked if navigation to a view actually happened.
     * <p>
     * Note: this handler is invoked after all lifecycle events of a view.
     * <p>
     * Example of setting a custom parameter to a view:
     * <pre>
     *     viewNavigators.detailView(Foo.class)
     *         .newEntity()
     *         .withViewClass(FooDetailView.class)
     *         .withAfterNavigationHandler(navigationEvent -> {
     *             FooDetailView view = navigationEvent.getView();
     *             view.setBar("bar");
     *         }).navigate();
     * </pre>
     *
     * @param handler a handler to set
     * @return this instance for chaining
     */
    public DetailViewClassNavigator<E, V> withAfterNavigationHandler(Consumer<AfterViewNavigationEvent<V>> handler) {
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
