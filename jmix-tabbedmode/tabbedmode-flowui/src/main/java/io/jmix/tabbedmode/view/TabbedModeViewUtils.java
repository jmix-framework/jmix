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

package io.jmix.tabbedmode.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import io.jmix.flowui.view.View;
import io.jmix.tabbedmode.component.viewcontainer.ViewContainer;
import org.springframework.lang.Nullable;

import java.util.Optional;

public final class TabbedModeViewUtils {

    private TabbedModeViewUtils() {
    }

    @Nullable
    public static TabbedModeViewProperties getViewProperties(Component component) {
        return ComponentUtil.getData(component, TabbedModeViewProperties.class);
    }

    public static void setViewProperties(Component component,
                                         @Nullable TabbedModeViewProperties viewProperties) {
        ComponentUtil.setData(component, TabbedModeViewProperties.class, viewProperties);
    }

    public static boolean isCloseable(Component component) {
        TabbedModeViewProperties viewProperties = getViewProperties(component);
        return viewProperties == null || viewProperties.isCloseable();
    }

    public static void setCloseable(Component component, boolean closeable) {
        TabbedModeViewProperties viewProperties = getViewProperties(component);
        if (viewProperties != null) {
            viewProperties.setCloseable(closeable);
        } else {
            viewProperties = new TabbedModeViewProperties();
            viewProperties.setCloseable(closeable);
            setViewProperties(component, viewProperties);
        }
    }

    public static boolean isDefaultView(Component component) {
        TabbedModeViewProperties viewProperties = getViewProperties(component);
        return viewProperties != null && viewProperties.isDefaultView();
    }

    public static void setDefaultView(Component component, boolean defaultView) {
        TabbedModeViewProperties viewProperties = getViewProperties(component);
        if (viewProperties != null) {
            viewProperties.setDefaultView(defaultView);
        } else {
            viewProperties = new TabbedModeViewProperties();
            viewProperties.setCloseable(defaultView);
            setViewProperties(component, viewProperties);
        }
    }

    public static boolean isForceDialog(Component component) {
        TabbedModeViewProperties viewProperties = getViewProperties(component);
        return viewProperties != null && viewProperties.isForceDialog();
    }

    public static void setForceDialog(Component component, boolean forceDialog) {
        TabbedModeViewProperties viewProperties = getViewProperties(component);
        if (viewProperties != null) {
            viewProperties.setForceDialog(forceDialog);
        } else {
            viewProperties = new TabbedModeViewProperties();
            viewProperties.setForceDialog(forceDialog);
            setViewProperties(component, viewProperties);
        }
    }

    public static Optional<ViewContainer> findViewContainer(View<?> view) {
        return view.getParent()
                .filter(parent -> parent instanceof ViewContainer)
                .map(parent -> ((ViewContainer) parent));
    }

    public static ViewContainer getViewContainer(View<?> view) {
        return findViewContainer(view)
                .orElseThrow(() -> new IllegalStateException("%s is not attached to a %s"
                        .formatted(View.class.getSimpleName(), ViewContainer.class.getSimpleName())));
    }
}
