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

import io.jmix.flowui.view.View;

import java.util.Optional;
import java.util.function.Consumer;

public class ListViewClassNavigator<E, V extends View<?>> extends ListViewNavigator<E>
        implements SupportsAfterViewNavigationHandler<V> {

    protected Class<V> viewClass;

    protected Consumer<AfterViewNavigationEvent<V>> afterNavigationHandler;

    public ListViewClassNavigator(Class<E> entityClass,
                                  Consumer<? extends ListViewNavigator<E>> handler,
                                  Class<V> viewClass) {
        super(entityClass, handler);

        this.viewClass = viewClass;
    }

    protected ListViewClassNavigator(ListViewNavigator<E> viewNavigator,
                                     Class<V> viewClass) {
        super(viewNavigator);

        this.viewClass = viewClass;
    }

    /**
     * Adds a handler that will be invoked if navigation to a view actually happened.
     * <p>
     * Note: this handler if in invoked after all lifecycle events of a view.
     *
     * @param handler a handler to set
     * @return this instance for chaining
     */
    public ListViewClassNavigator<E, V> withAfterNavigationHandler(Consumer<AfterViewNavigationEvent<V>> handler) {
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
