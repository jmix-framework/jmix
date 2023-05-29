/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.facet;

import io.jmix.flowui.view.View;

import org.springframework.lang.Nullable;

/**
 * Non-visual component of a {@link View}.
 */
public interface Facet {

    @Nullable
    String getId();

    void setId(@Nullable String id);

    /**
     * @return a view containing this facet
     */
    @Nullable
    View<?> getOwner();

    void setOwner(@Nullable View<?> owner);
}
