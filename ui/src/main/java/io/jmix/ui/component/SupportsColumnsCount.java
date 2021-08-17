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

package io.jmix.ui.component;

import io.jmix.ui.meta.StudioProperty;

/**
 * Interface to be implemented by a component that supports different values of the number of columns
 * that will be displayed in one row.
 */
public interface SupportsColumnsCount {

    /**
     * Returns the number of columns to be displayed on one row.
     *
     * @return the number of columns to be displayed on one row
     */
    int getColumnsCount();

    /**
     * Sets the number of columns to be displayed on one row.
     *
     * @param columnsCount the number of columns to be displayed on one row
     */
    @StudioProperty(defaultValue = "3")
    void setColumnsCount(int columnsCount);
}
