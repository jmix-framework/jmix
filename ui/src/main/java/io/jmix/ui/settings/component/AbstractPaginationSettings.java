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

package io.jmix.ui.settings.component;

import javax.annotation.Nullable;

public abstract class AbstractPaginationSettings implements ComponentSettings {

    protected String id;
    protected Integer itemsPerPageValue;
    protected Boolean isItemsPerPageUnlimitedOption;

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public Integer getItemsPerPageValue() {
        return itemsPerPageValue;
    }

    public void setItemsPerPageValue(@Nullable Integer itemsPerPageValue) {
        this.itemsPerPageValue = itemsPerPageValue;
    }

    /**
     * @return {@code true} if settings contain unlimited (null) option value or not
     */
    @Nullable
    public Boolean getIsItemsPerPageUnlimitedOption() {
        return isItemsPerPageUnlimitedOption;
    }

    /**
     * Sets whether settings contain null option value or not.
     *
     * @param isItemsPerPageUnlimitedOption items per page unlimited option
     */
    public void setIsItemsPerPageUnlimitedOption(@Nullable Boolean isItemsPerPageUnlimitedOption) {
        this.isItemsPerPageUnlimitedOption = isItemsPerPageUnlimitedOption;
    }
}
