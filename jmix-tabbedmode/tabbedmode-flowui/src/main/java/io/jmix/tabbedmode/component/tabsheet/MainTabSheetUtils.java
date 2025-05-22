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

public final class MainTabSheetUtils {

    private MainTabSheetUtils() {
    }

    public static void closeTab(JmixViewTab tab) {
        closeTab(tab, true);
    }

    public static void closeTab(JmixViewTab tab, boolean fromClient) {
        tab.closeInternal(fromClient);
    }

    public static ViewContainer asViewContainer(Component component) {
        if (component instanceof ViewContainer viewContainer) {
            return viewContainer;
        } else {
            throw new IllegalStateException("Tab content '%s' is not a %s"
                    .formatted(component, ViewContainer.class.getSimpleName()));
        }
    }

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

    public static View<?> getViewFromContent(Component component) {
        return findViewFromContent(component)
                .orElseThrow(() ->
                        new IllegalStateException("Tab does not contain a %s"
                                .formatted(View.class.getSimpleName())));
    }
}
