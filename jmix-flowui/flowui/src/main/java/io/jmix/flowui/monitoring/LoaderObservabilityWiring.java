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

package io.jmix.flowui.monitoring;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.observation.DataLoaderObservationInfo;

/**
 * Wires the {@link DataLoaderObservationInfo} provider on a {@link DataLoader} from an
 * {@link ObservableDataHolder}.
 */
@Internal
public final class LoaderObservabilityWiring {

    private LoaderObservabilityWiring() {
    }

    /**
     * Resolution is lazy because for fragment-owned loaders the fragment is not yet attached to its
     * host at registration time — the enclosing view is only reachable later, on each monitoring event.
     */
    public static void install(ObservableDataHolder holder, DataLoader loader, String loaderId) {
        loader.setObservationInfoProvider(dl ->
                new DataLoaderObservationInfo(
                        holder.getObservableViewId(), loaderId, holder.getObservableFragmentId()));
    }
}
