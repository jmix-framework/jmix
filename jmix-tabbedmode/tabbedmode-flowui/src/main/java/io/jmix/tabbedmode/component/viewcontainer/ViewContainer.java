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

package io.jmix.tabbedmode.component.viewcontainer;

import io.jmix.flowui.view.View;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs;
import org.springframework.lang.Nullable;

/**
 * Represents a container that can hold {@link View views} and {@link ViewBreadcrumbs breadcrumbs},
 * providing functionality to interact with and manipulate them dynamically.
 */
public interface ViewContainer {

    /**
     * Returns the {@link ViewBreadcrumbs} associated with this container.
     *
     * @return the {@link ViewBreadcrumbs} instance if set,
     * or {@code null} if no breadcrumbs are associated
     */
    @Nullable
    ViewBreadcrumbs getBreadcrumbs();

    /**
     * Sets the {@link ViewBreadcrumbs} for this container.
     *
     * @param breadcrumbs the {@link ViewBreadcrumbs} instance to associate with this container,
     *                    or {@code null} to remove any currently associated breadcrumbs
     */
    void setBreadcrumbs(@Nullable ViewBreadcrumbs breadcrumbs);

    /**
     * Removes the breadcrumbs from this container.
     */
    void removeBreadcrumbs();

    /**
     * Returns the {@link View} contained in this container.
     *
     * @return the current {@link View} if present, or null if no view is set
     */
    @Nullable
    View<?> getView();

    /**
     * Sets the specified {@link View} in this container.
     *
     * @param view the {@link View} to set in this container,
     *             or {@code null} to remove
     */
    void setView(@Nullable View<?> view);

    /**
     * Removes the {@link View} contained in this container.
     */
    void removeView();
}
