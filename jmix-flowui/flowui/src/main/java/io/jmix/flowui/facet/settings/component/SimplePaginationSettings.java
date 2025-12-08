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

package io.jmix.flowui.facet.settings.component;

import io.jmix.flowui.facet.settings.Settings;
import org.springframework.lang.Nullable;

/**
 * Represents settings for a simple pagination component, allowing configuration of pagination properties
 * such as identifying the settings and defining the number of items per page.
 */
public class SimplePaginationSettings implements Settings {

    protected String id;
    protected Integer itemsPerPageValue;

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(@Nullable String id) {
        this.id = id;
    }

    /**
     * Returns the value that specifies the number of items per page in the pagination settings.
     *
     * @return the number of items per page, or {@code null} if not specified
     */
    @Nullable
    public Integer getItemsPerPageValue() {
        return itemsPerPageValue;
    }

    /**
     * Sets the value for the number of items displayed per page in the pagination settings.
     *
     * @param itemsPerPageValue the number of items to display per page; may be {@code null}
     *                          if this setting is not specified
     */
    public void setItemsPerPageValue(@Nullable Integer itemsPerPageValue) {
        this.itemsPerPageValue = itemsPerPageValue;
    }
}
