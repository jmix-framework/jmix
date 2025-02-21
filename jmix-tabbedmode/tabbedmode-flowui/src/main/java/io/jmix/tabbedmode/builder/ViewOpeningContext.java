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

import com.google.common.base.Objects;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.view.View;
import io.jmix.tabbedmode.view.ViewOpenMode;

public class ViewOpeningContext {

    protected final View<?> view;
    protected final ViewOpenMode openMode;

    protected RouteParameters routeParameters;
    protected boolean closeSameView = false;

    public ViewOpeningContext(View<?> view, ViewOpenMode openMode) {
        Preconditions.checkNotNullArgument(view);
        Preconditions.checkNotNullArgument(openMode);

        this.view = view;
        this.openMode = openMode;
    }

    public static ViewOpeningContext create(View<?> view, ViewOpenMode openMode) {
        return new ViewOpeningContext(view, openMode);
    }

    public View<?> getView() {
        return view;
    }

    public ViewOpenMode getOpenMode() {
        return openMode;
    }

    public RouteParameters getRouteParameters() {
        return routeParameters != null
                ? routeParameters
                : RouteParameters.empty();
    }

    public boolean isCloseSameView() {
        return closeSameView;
    }

    public ViewOpeningContext withRouteParameters(RouteParameters routeParameters) {
        Preconditions.checkNotNullArgument(routeParameters);

        this.routeParameters = routeParameters;
        return this;
    }

    public ViewOpeningContext withCloseSameView(boolean closeSameView) {
        this.closeSameView = closeSameView;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ViewOpeningContext that = (ViewOpeningContext) o;
        return Objects.equal(view, that.view)
                && openMode == that.openMode
                && Objects.equal(routeParameters, that.routeParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(view, openMode, routeParameters, closeSameView);
    }

    @Override
    public String toString() {
        return "ViewOpeningContext{" +
                "view=" + view +
                ", openMode=" + openMode +
                ", routeParameters=" + routeParameters +
                '}';
    }
}
