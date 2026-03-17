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

package io.jmix.flowui.model;

import io.jmix.flowui.component.HasDataComponents;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.Nullable;

/**
 * Interface defining methods for interacting with data API elements of a {@link View}.
 */
public interface ViewData extends HasDataComponents {

    /**
     * Returns the identifier of the {@link View}.
     *
     * @return the {@link View} identifier, or {@code null} if it is not set.
     */
    @Nullable
    String getViewId();

    /**
     * Sets the identifier of the {@link View}.
     *
     * @param viewId the identifier of the {@link View} to set, or {@code null} to remove the identifier
     */
    void setViewId(@Nullable String viewId);
}
