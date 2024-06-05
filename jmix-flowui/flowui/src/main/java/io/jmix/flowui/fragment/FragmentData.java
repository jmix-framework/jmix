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

package io.jmix.flowui.fragment;

import io.jmix.flowui.component.HasDataComponents;
import org.springframework.lang.Nullable;

/**
 * Interface defining methods for interacting with data API elements of a {@link Fragment}.
 */
public interface FragmentData extends HasDataComponents {

    /**
     * @return owner fragment id
     */
    @Nullable
    String getFragmentId();

    /**
     * Sets owner fragment id.
     *
     * @param fragmentId if to set
     */
    void setFragmentId(@Nullable String fragmentId);
}
