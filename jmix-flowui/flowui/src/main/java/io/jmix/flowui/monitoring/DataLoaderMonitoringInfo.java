/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.monitoring;

import io.jmix.flowui.model.DataLoader;
import org.springframework.lang.Nullable;

/**
 * Static info about {@link DataLoader} that will be used as monitoring tags.
 */
public class DataLoaderMonitoringInfo {

    protected String viewId;
    protected String loaderId;

    private static final DataLoaderMonitoringInfo EMPTY = new DataLoaderMonitoringInfo(null, null);

    /**
     *
     * @param viewId   id of the view containing target data loader
     * @param loaderId id of the target data loader
     */
    public DataLoaderMonitoringInfo(@Nullable String viewId, @Nullable String loaderId) {
        viewId = viewId;
        loaderId = loaderId;
    }

    /**
     * Returns stub objects with null values. Monitoring records will not be created based on this info.
     */
    public static DataLoaderMonitoringInfo empty() {
        return EMPTY;
    }

    public String viewId() {
        return viewId;
    }

    public DataLoaderMonitoringInfo setViewId(String viewId) {
        this.viewId = viewId;
        return this;
    }

    public String loaderId() {
        return loaderId;
    }

    public DataLoaderMonitoringInfo setLoaderId(String loaderId) {
        this.loaderId = loaderId;
        return this;
    }
}
