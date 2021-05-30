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

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioProperty;

/**
 * Interface for UI components that support filter mode.
 */
public interface HasFilterMode {

    /**
     * @return filter mode for the dropdown list in the field
     */
    FilterMode getFilterMode();

    /**
     * Sets filter mode for the dropdown list in the field.
     *
     * @param filterMode filter mode to set
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "CONTAINS",
            options = {"NO", "STARTS_WITH", "CONTAINS"})
    void setFilterMode(FilterMode filterMode);

    /**
     * Describes filter modes that should be used in the field.
     */
    enum FilterMode {

        /**
         * Field does not use filter.
         */
        NO,

        /**
         * Field shows values which captions starts with entered text.
         */
        STARTS_WITH,

        /**
         * Field shows values which captions contains entered text.
         */
        CONTAINS
    }
}
