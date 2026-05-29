/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.observation;

import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.Nullable;

/**
 * Info about a {@link DataLoader} used as low-cardinality tags of the {@code jmix.ui.data}
 * Observation metric.
 *
 * @param viewId     id of the enclosing {@link View}, or {@code null} if not resolvable
 * @param loaderId   id of the target data loader
 * @param fragmentId id of the enclosing {@link Fragment} when the loader lives inside a fragment,
 *                   {@code null} for loaders attached directly to a view
 */
public record DataLoaderObservationInfo(@Nullable String viewId,
                                        @Nullable String loaderId,
                                        @Nullable String fragmentId) {

    private static final DataLoaderObservationInfo EMPTY = new DataLoaderObservationInfo(null, null, null);

    /**
     * Returns a stub object with null values. Observation will not be created from this info.
     */
    public static DataLoaderObservationInfo empty() {
        return EMPTY;
    }
}
