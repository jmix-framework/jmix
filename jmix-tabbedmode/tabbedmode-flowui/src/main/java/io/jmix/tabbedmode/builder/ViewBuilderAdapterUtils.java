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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.DialogWindowBuilder;
import io.jmix.flowui.view.navigation.AbstractViewNavigator;
import io.jmix.flowui.view.navigation.SupportsAfterViewNavigationHandler;
import io.jmix.tabbedmode.builder.navigation.AfterNavigationViewReadyListenerAdapter;
import io.jmix.tabbedmode.view.ViewOpenMode;

/**
 * Utility class working with the navigation compatibility specifics.
 */
public final class ViewBuilderAdapterUtils {

    private ViewBuilderAdapterUtils() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void apply(AbstractViewBuilder<?, ?> builder, AbstractViewNavigator viewNavigator) {
        if (viewNavigator instanceof SupportsAfterViewNavigationHandler<?> navigator) {
            navigator.getAfterNavigationHandler().ifPresent(handler ->
                    builder.withReadyListener(new AfterNavigationViewReadyListenerAdapter(handler)));
        }

        builder.getViewClass().ifPresent(viewClass ->
                builder.withOpenMode(inferOpenMode(viewNavigator, viewClass)));
    }

    public static <V extends View<?>> void apply(AbstractViewBuilder<V, ?> builder,
                                                 DialogWindowBuilder<V> windowBuilder) {
        builder.withOpenMode(ViewOpenMode.DIALOG);

        windowBuilder.getViewConfigurer().ifPresent(builder::withViewConfigurer);
    }

    public static ViewOpenMode inferOpenMode(AbstractViewNavigator viewNavigator,
                                             Class<? extends View<?>> viewClass) {
        Route route = viewClass.getAnnotation(Route.class);
        if (route == null) {
            return ViewOpenMode.NEW_TAB;
        }

        if (route.layout().equals(UI.class)) {
            return ViewOpenMode.ROOT;
        }

        return viewNavigator.isBackwardNavigation() ? ViewOpenMode.THIS_TAB : ViewOpenMode.NEW_TAB;
    }
}
