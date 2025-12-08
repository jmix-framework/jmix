/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.component.groupgrid;

import io.jmix.core.metamodel.model.MetaPropertyPath;

import java.util.Objects;

/**
 * Represents a property that can be used for grouping.
 */
public interface GroupProperty {

    /**
     * Returns the property. It contains one of the following types:
     * <ul>
     *    <li>
     *        {@link MetaPropertyPath} if the grouping column is based on an entity property.
     *    </li>
     *    <li>
     *        {@link String} (column key) if the grouping is based on grouping column not bound to an
     *        entity property.
     *    </li>
     * </ul>
     *
     * @return the property
     */
    Object get();

    /**
     * Checks whether the given property value matches this group property's value.
     * <p>
     * The method compares the provided {@code property} value with the value obtained from the {@link #get} method.
     * <ul>
     *   <li>
     *       If the provided {@code property} is a {@link String} and group property is a {@link MetaPropertyPath}.
     *       It will compare the provided value with the string representation of the property path.
     *   </li>
     *   <li>
     *       For any other cases, the equality will be determined using {@link Objects#equals(Object, Object)}.
     *   </li>
     * </ul>
     *
     * @param property the property to check
     * @return {@code true} if the property matches the current property value
     */
    default boolean is(Object property) {
        Object currentProperty = get();

        if (property instanceof String
                && currentProperty instanceof MetaPropertyPath mpp) {
            return Objects.equals(mpp.toPathString(), property);
        }

        return Objects.equals(currentProperty, property);
    }
}
