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

package io.jmix.flowui.kit.component;


import io.jmix.flowui.kit.component.formatter.Formatter;

import jakarta.annotation.Nullable;

/**
 * Interface to be implemented by UI components supporting value formatting.
 */
public interface SupportsFormatter<V> {

    /**
     * @return a formatter or {@code null} if not set
     */
    @Nullable
    Formatter<V> getFormatter();

    /**
     * Sets a formatter that is used to produce the strings representation of the value.
     *
     * @param formatter a formatter to set or {@code null} to remove
     */
    void setFormatter(@Nullable Formatter<? super V> formatter);
}
