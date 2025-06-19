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

package io.jmix.flowui.data;

import org.springframework.lang.Nullable;

/**
 * Interface defining a component or object that can be associated with a {@link ValueSource}.
 * A {@link ValueSource} provides a mechanism for binding the component's value to a data source.
 *
 * @param <V> the type of the value managed by the value source
 */
public interface SupportsValueSource<V> {

    /**
     * Returns the current {@link ValueSource} associated with this component.
     *
     * @return the associated {@link ValueSource}, or {@code null} if no value source is set
     */
    @Nullable
    ValueSource<V> getValueSource();

    /**
     * Sets a {@link ValueSource} for this component, allowing it to bind its value to a specified data source.
     *
     * @param valueSource the {@link ValueSource} to be associated with this component, or {@code null} to remove
     *                    the current association
     */
    void setValueSource(@Nullable ValueSource<V> valueSource);
}
