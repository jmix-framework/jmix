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

import io.jmix.flowui.view.View;
import io.jmix.tabbedmode.Views;
import io.jmix.tabbedmode.action.tabsheet.CloseAllTabsAction;
import io.jmix.tabbedmode.action.tabsheet.CloseOtherTabsAction;
import io.jmix.tabbedmode.action.tabsheet.CloseThisTabAction;

/**
 * Represents available properties of {@link View Views}.
 */
public class TabbedModeViewProperties {

    protected boolean closeable;
    protected boolean defaultView;
    protected boolean forceDialog;

    public TabbedModeViewProperties() {
    }

    /**
     * Returns whether a view can be closed by standard operations like a tab close button,
     * close tab actions, {@link Views.OpenedViews} and {@link Views.ViewStack} methods.
     *
     * @return {@code true} if the view is closeable, {@code false} otherwise
     * @see CloseThisTabAction
     * @see CloseOtherTabsAction
     * @see CloseAllTabsAction
     * @see Views.OpenedViews#closeAll()
     * @see Views.ViewStack#close()
     */
    public boolean isCloseable() {
        return closeable;
    }

    /**
     * Sets whether a view can be closed by standard operations like a tab close button,
     * close tab actions, {@link Views.OpenedViews} and {@link Views.ViewStack} methods.
     *
     * @param closeable {@code true} if the view is closeable, {@code false} otherwise
     * @see CloseThisTabAction
     * @see CloseOtherTabsAction
     * @see CloseAllTabsAction
     * @see Views.OpenedViews#closeAll()
     * @see Views.ViewStack#close()
     */
    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }

    /**
     * Returns whether a view is a default, which means that it's opened after successful log in.
     *
     * @return {@code true} if the view is a default, {@code false} otherwise
     */
    public boolean isDefaultView() {
        return defaultView;
    }

    /**
     * Sets whether a view is a default, which means that it's opened after successful log in.
     *
     * @param defaultView {@code true} if the view is a default, {@code false} otherwise
     */
    public void setDefaultView(boolean defaultView) {
        this.defaultView = defaultView;
    }

    /**
     * Returns whether a view must be opened in a dialog window.
     *
     * @return {@code true} if the view must be opened in a dialog
     * window, {@code false} otherwise
     */
    public boolean isForceDialog() {
        return forceDialog;
    }

    /**
     * Sets whether a view must be opened in a dialog window.
     *
     * @param forceDialog {@code true} if the view must be opened
     *                    in a dialog window, {@code false} otherwise
     */
    public void setForceDialog(boolean forceDialog) {
        this.forceDialog = forceDialog;
    }


}
