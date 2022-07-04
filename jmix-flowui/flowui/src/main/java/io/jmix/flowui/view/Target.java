/*
 * Copyright 2019 Haulmont.
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

package io.jmix.flowui.view;


import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.ViewData;

/**
 * {@link Subscribe} and {@link Install} target type.
 */
public enum Target {
    /**
     * UI component if id of component specified in the corresponding annotation.
     * <p>
     * Default option.
     */
    COMPONENT,

    /**
     * UI controller
     */
    CONTROLLER,

    /**
     * Parent UI controller
     */
    PARENT_CONTROLLER,

    /**
     * {@link DataLoader} defined in {@link ViewData}.
     */
    DATA_LOADER,

    /**
     * Data container defined in {@link ViewData}.
     */
    DATA_CONTAINER,

    /**
     * {@code DataContext} provided by {@link ViewData}.
     */
    DATA_CONTEXT
}