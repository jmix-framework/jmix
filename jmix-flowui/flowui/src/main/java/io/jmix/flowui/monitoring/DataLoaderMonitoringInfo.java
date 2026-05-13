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

import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.Nullable;

/**
 * Info about {@link DataLoader} that will be used as monitoring tags.
 *
 * @param viewId     id of the enclosing {@link View}, or {@code null} if not resolvable
 * @param loaderId   id of the target data loader
 * @param fragmentId id of the enclosing {@link Fragment} when the loader lives inside a fragment,
 *                   {@code null} for loaders attached directly to a view
 */
public record DataLoaderMonitoringInfo(@Nullable String viewId,
                                       @Nullable String loaderId,
                                       @Nullable String fragmentId) {

    private static final DataLoaderMonitoringInfo EMPTY = new DataLoaderMonitoringInfo(null, null, null);

    /**
     * Returns stub objects with null values. Monitoring records will not be created based on this info.
     */
    public static DataLoaderMonitoringInfo empty() {
        return EMPTY;
    }
}
