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

package io.jmix.flowui.component;

import io.jmix.core.metamodel.datatype.Datatype;
import org.springframework.lang.Nullable;

/**
 * Represents a contract for components or classes that support specifying a datatype.
 *
 * @param <V> the type of value the datatype describes
 */
public interface SupportsDatatype<V> {

    /**
     * Returns the datatype of this component.
     *
     * @return the datatype, or {@code null} if no datatype is set
     */
    @Nullable
    Datatype<V> getDatatype();

    /**
     * Sets the datatype of this component.
     *
     * @param datatype the datatype to be set, or {@code null} to unset the current datatype
     */
    void setDatatype(@Nullable Datatype<V> datatype);
}
