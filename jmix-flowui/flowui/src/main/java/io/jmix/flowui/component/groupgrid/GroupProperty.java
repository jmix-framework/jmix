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

public interface GroupProperty {

    Object get();

    default boolean is(Object property) {
        Object value = get();

        if (property instanceof String) {
            if (value instanceof MetaPropertyPath mpp) {
                return Objects.equals(mpp.toPathString(), property);
            }
            return Objects.equals(value.toString(), property);
        }
        if (property instanceof MetaPropertyPath mpp) {
            if (value instanceof String) {
                return Objects.equals(value, mpp.toPathString());
            }
            return Objects.equals(value, mpp);
        }

        return Objects.equals(value, property);
    }
}
