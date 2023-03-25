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
import java.util.function.Consumer;

/**
 * Provides a fluent interface to configure navigation parameters and navigate to a {@link View}.
 * <p>
 * An instance of this class should be obtained through {@link ViewNavigators#view(String)}.
 */
public class ViewNavigator extends AbstractViewNavigator {

    public ViewNavigator(Consumer<? extends ViewNavigator> handler) {
        super(handler);
    }

    protected ViewNavigator(ViewNavigator viewNavigator) {
        super(viewNavigator);
    }

    @Override
    public ViewNavigator withViewId(@Nullable String viewId) {
        super.withViewId(viewId);
        return this;
    }

    /**
     * Sets the opened view by its class.
     *
     * @param viewClass view class
     * @return this instance for chaining
     */
    public <V extends View<?>> ViewClassNavigator<V> withViewClass(Class<V> viewClass) {
        return new ViewClassNavigator<>(this, viewClass);
    }

    @Override
    public ViewNavigator withRouteParameters(@Nullable RouteParameters routeParameters) {
        super.withRouteParameters(routeParameters);
        return this;
    }

    @Override
    public ViewNavigator withQueryParameters(@Nullable QueryParameters queryParameters) {
        super.withQueryParameters(queryParameters);
        return this;
    }

    @Override
    public ViewNavigator withBackwardNavigation(boolean backwardNavigation) {
        super.withBackwardNavigation(backwardNavigation);
        return this;
    }
}
