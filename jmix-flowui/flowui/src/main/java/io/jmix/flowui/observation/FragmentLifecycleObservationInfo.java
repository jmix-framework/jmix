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
import org.springframework.lang.Nullable;

/**
 * POJO class for information about {@link Fragment} that will be used for observation cardinalities.
 *
 * @param fragmentId    id of the fragment
 * @param fragmentClass FQN of the target fragment class
 */
public record FragmentLifecycleObservationInfo(@Nullable String fragmentId, String fragmentClass) {

    public FragmentLifecycleObservationInfo(Fragment<?> fragment) {
        this(fragment.getId().orElse(null), fragment.getClass().getName());
    }
}
