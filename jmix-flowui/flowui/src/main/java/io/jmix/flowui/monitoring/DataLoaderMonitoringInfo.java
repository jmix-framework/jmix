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
import io.jmix.flowui.observation.DataLoaderObservationInfo;
import org.jspecify.annotations.Nullable;

/**
 * Static info about a {@link DataLoader} used as monitoring tags of the deprecated
 * {@link UiMonitoring} Timer.
 *
 * @param viewId   id of the view containing the target data loader; for fragment-owned loaders this
 *                 historically held the fragment id, preserving the {@code view} tag of pre-3.0
 *                 {@code jmix_ui_data} dashboards
 * @param loaderId id of the target data loader
 *
 * @deprecated Replaced by {@link DataLoaderObservationInfo} for the modern Observation path.
 * Lives on for as long as {@link UiMonitoring} does.
 */
@Deprecated(since = "3.0", forRemoval = true)
public record DataLoaderMonitoringInfo(@Nullable String viewId, @Nullable String loaderId) {

    private static final DataLoaderMonitoringInfo EMPTY = new DataLoaderMonitoringInfo(null, null);

    /**
     * Returns stub objects with null values. Monitoring records will not be created based on this info.
     */
    public static DataLoaderMonitoringInfo empty() {
        return EMPTY;
    }
}
