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

package io.jmix.tabbedmode.component.tabsheet;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.view.View;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs;
import io.jmix.tabbedmode.component.viewcontainer.ViewContainer;

import java.util.Optional;

/**
 * Utility class working with {@link JmixMainTabSheet}'s specifics.
 */
public final class MainTabSheetUtils {

    private MainTabSheetUtils() {
    }

    /**
     * Converts the given {@link Component} to a {@link ViewContainer}.
     *
     * @param component the component to be cast to {@link ViewContainer}
     * @return the component cast as {@link ViewContainer} if it is an instance
     * @throws IllegalStateException if the provided component is not a {@link ViewContainer}
     */
    public static ViewContainer asViewContainer(Component component) {
        if (component instanceof ViewContainer viewContainer) {
            return viewContainer;
        } else {
            throw new IllegalStateException("Tab content '%s' is not a %s"
                    .formatted(component, ViewContainer.class.getSimpleName()));
        }
    }

    /**
     * Attempts to find and return a {@link View} associated with the provided tab content.
     *
     * @param component the component from which the view is to be determined
     * @return an {@link Optional} containing the found {@link View}, or an empty {@link Optional}
     * if no view can be determined
     */
    public static Optional<View<?>> findViewFromContent(Component component) {
        ViewContainer viewContainer = MainTabSheetUtils.asViewContainer(component);
        ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
        if (breadcrumbs != null) {
            ViewBreadcrumbs.ViewInfo viewInfo = breadcrumbs.getCurrentViewInfo();
            if (viewInfo != null) {
                return Optional.of(viewInfo.view());
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.ofNullable(viewContainer.getView());
        }
    }

    /**
     * Returns the {@link View} associated with the provided tab content.
     *
     * @param component the component from which the associated {@link View} is to be retrieved
     * @return the {@link View} associated with the provided tab content
     * @throws IllegalStateException if no {@link View} is found within the provided component
     */
    public static View<?> getViewFromContent(Component component) {
        return findViewFromContent(component)
                .orElseThrow(() ->
                        new IllegalStateException("Tab does not contain a %s"
                                .formatted(View.class.getSimpleName())));
    }
}
