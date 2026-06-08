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
import org.jspecify.annotations.Nullable;

/**
 * Internal contract for data-component holders that expose observability metadata of their enclosed
 * {@link DataLoader}s as {@code view.id} / {@code fragment.id} tags of {@code jmix.ui.data} metrics.
 */
@Internal
public interface ObservableDataHolder {

    /**
     * @return id of the enclosing view, or {@code null} if there is no enclosing view (e.g. holder
     * owned by a fragment whose host is not yet attached).
     */
    @Nullable
    String getObservableViewId();

    /**
     * @return id of the enclosing fragment, or {@code null} if the holder is not fragment-owned.
     */
    @Nullable
    String getObservableFragmentId();
}
