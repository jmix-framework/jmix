/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.settings.component;

import io.jmix.ui.settings.component.ComponentSettings;

import javax.annotation.Nullable;

public class CubaFilterSettings implements ComponentSettings {

    protected String id;

    protected String defaultFilterId;

    protected Boolean applyDefault;

    protected Boolean groupBoxExpanded;

    protected Integer maxResults;

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
    public String getDefaultFilterId() {
        return defaultFilterId;
    }

    public void setDefaultFilterId(@Nullable String defaultFilterId) {
        this.defaultFilterId = defaultFilterId;
    }

    @Nullable
    public Boolean getApplyDefault() {
        return applyDefault;
    }

    public void setApplyDefault(@Nullable Boolean applyDefault) {
        this.applyDefault = applyDefault;
    }

    @Nullable
    public Boolean getGroupBoxExpanded() {
        return groupBoxExpanded;
    }

    public void setGroupBoxExpanded(@Nullable Boolean groupBoxExpanded) {
        this.groupBoxExpanded = groupBoxExpanded;
    }

    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(@Nullable Integer maxResults) {
        this.maxResults = maxResults;
    }
}
