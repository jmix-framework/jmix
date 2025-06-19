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

import io.jmix.flowui.kit.meta.StudioIgnore;
import org.springframework.lang.Nullable;

/**
 * An object having a {@link ValueSourceProvider}.
 */
public interface HasValueSourceProvider {

    /**
     * Returns the {@link ValueSourceProvider} associated with this object.
     * The {@link ValueSourceProvider} is responsible for providing instances
     * of {@link ValueSource}, typically for child components or properties.
     *
     * @return the associated {@link ValueSourceProvider}, or {@code null} if none is set
     */
    @Nullable
    ValueSourceProvider getValueSourceProvider();

    /**
     * Sets a {@link ValueSourceProvider} for the implementing object. The {@link ValueSourceProvider} is responsible
     * for supplying {@link ValueSource} instances, often for use with child components or properties.
     *
     * @param provider the {@link ValueSourceProvider} to set, or {@code null} to remove the current provider
     */
    @StudioIgnore
    void setValueSourceProvider(@Nullable ValueSourceProvider provider);
}

