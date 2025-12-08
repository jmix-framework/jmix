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

package io.jmix.flowui.accesscontext;

import io.jmix.core.accesscontext.AccessContext;
import io.jmix.flowui.view.View;
import org.springframework.lang.Nullable;

/**
 * Defines an authorization point for showing {@link View}.
 * Determines whether a specific UI view can be displayed based on access permissions.
 */
public class UiShowViewContext implements AccessContext {

    protected final String viewId;

    protected boolean permitted = true;

    public UiShowViewContext(String viewId) {
        this.viewId = viewId;
    }

    /**
     * Retrieves the identifier of the {@link View} associated with this context.
     *
     * @return the view identifier
     */
    public String getViewId() {
        return viewId;
    }

    /**
     * Denies access for the associated {@link View} by setting the permission state to false.
     * This method is used to explicitly restrict access within the current context.
     */
    public void setDenied() {
        permitted = false;
    }

    /**
     * Checks whether access is permitted within the current context.
     *
     * @return {@code true} if access is permitted, {@code false} otherwise
     */
    public boolean isPermitted() {
        return permitted;
    }

    @Nullable
    @Override
    public String explainConstraints() {
        return !permitted ? "view: " + viewId : null;
    }
}
