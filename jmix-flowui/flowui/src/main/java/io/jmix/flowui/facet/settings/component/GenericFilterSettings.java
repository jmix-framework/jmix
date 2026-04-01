/*
 * Copyright 2023 Haulmont.
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

import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.facet.settings.Settings;
import org.jspecify.annotations.Nullable;

/**
 * Represents settings for a {@link GenericFilter} component.
 */
public class GenericFilterSettings implements Settings {

    protected String id;

    protected Boolean opened;

    protected String defaultConfigurationId;

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
     * Returns the opened state of the associated component.
     *
     * @return {@code true} if the component is opened, {@code false} if it is closed,
     * or {@code null} if the opened state is not explicitly set
     */
    @Nullable
    public Boolean getOpened() {
        return opened;
    }

    /**
     * Sets the opened state of the associated component.
     *
     * @param opened the new opened state of the component; {@code true} if the component should be opened,
     *               {@code false} if it should be closed, or {@code null} if the opened state is not explicitly set
     */
    public void setOpened(@Nullable Boolean opened) {
        this.opened = opened;
    }

    /**
     * Returns the default configuration ID associated with the settings.
     *
     * @return the default configuration ID, or {@code null} if not set
     */
    @Nullable
    public String getDefaultConfigurationId() {
        return defaultConfigurationId;
    }

    /**
     * Sets the default configuration ID for the settings.
     *
     * @param defaultConfigurationId the default configuration ID to set, or {@code null}
     *                                if no default configuration ID is specified
     */
    public void setDefaultConfigurationId(@Nullable String defaultConfigurationId) {
        this.defaultConfigurationId = defaultConfigurationId;
    }
}
