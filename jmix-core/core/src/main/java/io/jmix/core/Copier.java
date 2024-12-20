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

package io.jmix.core;

/**
 * Creates copies of objects.
 * <p>
 * This interface provides the {@link #copy(Object)} method similar by semantics to {@link MetadataTools#deepCopy(Object)}
 * but different in that it doesn't rely on metadata and copies all object's state using a low-level mechanism
 * like Java serialization.
 */
public interface Copier {

    /**
     * Creates a deep copy of the passed object.
     *
     * @param source source object
     * @return deep copy of the source object
     */
    <T> T copy(T source);
}
