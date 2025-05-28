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
import io.jmix.tabbedmode.Views;
import io.jmix.tabbedmode.action.tabsheet.CloseAllTabsAction;
import io.jmix.tabbedmode.action.tabsheet.CloseOtherTabsAction;
import io.jmix.tabbedmode.action.tabsheet.CloseThisTabAction;
import io.jmix.tabbedmode.component.viewcontainer.ViewContainer;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * Utility class working with Tabbed Mode view's specifics.
 */
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

    /**
     * Returns whether the passed view can be closed by standard operations like a tab close button,
     * close tab actions, {@link Views.OpenedViews} and {@link Views.ViewStack} methods.
     *
     * @param component a view to check
     * @return {@code true} if the view is closeable, {@code false} otherwise
     * @see CloseThisTabAction
     * @see CloseOtherTabsAction
     * @see CloseAllTabsAction
     * @see Views.OpenedViews#closeAll()
     * @see Views.ViewStack#close()
     */
    public static boolean isCloseable(Component component) {
        TabbedModeViewProperties viewProperties = getViewProperties(component);
        return viewProperties == null || viewProperties.isCloseable();
    }

    /**
     * Sets whether the passed view can be closed by standard operations like a tab close button,
     * close tab actions, {@link Views.OpenedViews} and {@link Views.ViewStack} methods.
     *
     * @param component a view to set the closeable flag for
     * @param closeable {@code true} if the view is closeable, {@code false} otherwise
     * @see CloseThisTabAction
     * @see CloseOtherTabsAction
     * @see CloseAllTabsAction
     * @see Views.OpenedViews#closeAll()
     * @see Views.ViewStack#close()
     */
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

    /**
     * Returns whether the passed view is a default, which means that it's opened
     * after successful log in.
     *
     * @param component a view to check
     * @return {@code true} if the view is a default, {@code false} otherwise
     */
    public static boolean isDefaultView(Component component) {
        TabbedModeViewProperties viewProperties = getViewProperties(component);
        return viewProperties != null && viewProperties.isDefaultView();
    }

    /**
     * Sets whether the passed view is a default, which means that it's opened
     * after successful log in.
     *
     * @param component   a view to set the default flag for
     * @param defaultView {@code true} if the view is a default, {@code false} otherwise
     */
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

    /**
     * Returns whether the passed view must be opened in a dialog window.
     *
     * @param component a view to check
     * @return {@code true} if the view must be opened in a dialog
     * window, {@code false} otherwise
     */
    public static boolean isForceDialog(Component component) {
        TabbedModeViewProperties viewProperties = getViewProperties(component);
        return viewProperties != null && viewProperties.isForceDialog();
    }

    /**
     * Sets whether the passed view must be opened in a dialog window.
     *
     * @param component   a view to set the force dialog flag for
     * @param forceDialog {@code true} if the view must be opened
     *                    in a dialog window, {@code false} otherwise
     */
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

    /**
     * Finds the parent {@link ViewContainer} for the specified {@link View}, if it exists.
     *
     * @param view the view whose container is to be found
     * @return an {@link Optional} containing the parent {@link ViewContainer} if found,
     * or an empty {@link Optional} if no container exists
     */
    public static Optional<ViewContainer> findViewContainer(View<?> view) {
        return view.getParent()
                .filter(parent -> parent instanceof ViewContainer)
                .map(parent -> ((ViewContainer) parent));
    }

    /**
     * Returns the parent {@link ViewContainer} for the specified {@link View}.
     *
     * @param view the view whose container is to be found
     * @return the parent {@link ViewContainer}
     * @throws IllegalStateException if the specified {@link View} is not attached to a {@link ViewContainer}
     */
    public static ViewContainer getViewContainer(View<?> view) {
        return findViewContainer(view)
                .orElseThrow(() -> new IllegalStateException("%s is not attached to a %s"
                        .formatted(View.class.getSimpleName(), ViewContainer.class.getSimpleName())));
    }
}
