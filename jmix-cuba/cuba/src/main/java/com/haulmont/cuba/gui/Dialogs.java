/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui;

/**
 * @deprecated Use {@link io.jmix.ui.Dialogs} instead
 */
@Deprecated
public interface Dialogs extends io.jmix.ui.Dialogs {

    /**
     * Marker interface for Dialog Builders that have maximized setting.
     *
     * @param <T> return type of fluent API methods
     * @deprecated Use {@link io.jmix.ui.Dialogs.HasWindowMode} instead
     */
    @Deprecated
    interface HasMaximized<T> {
        /**
         * Sets whether dialog should be maximized.
         *
         * @param maximized maximized flag
         * @return builder
         */
        T withMaximized(boolean maximized);

        /**
         * Enables dialog maximized mode.
         *
         * @return builder
         */
        T maximized();

        /**
         * @return true if dialog will be maximized
         */
        boolean isMaximized();
    }
}
