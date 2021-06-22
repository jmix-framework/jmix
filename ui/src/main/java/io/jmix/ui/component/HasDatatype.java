/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component;

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;

public interface HasDatatype<V> {
    /**
     * Sets the given <code>datatype</code> to the component. Its value will be formatted according to this
     * datatype.
     *
     * @param datatype {@link Datatype} instance
     */
    @StudioProperty(type = PropertyType.DATATYPE_ID, typeParameter = "V")
    void setDatatype(@Nullable Datatype<V> datatype);

    /**
     * @return a datatype that is used by this component
     */
    @Nullable
    Datatype<V> getDatatype();
}