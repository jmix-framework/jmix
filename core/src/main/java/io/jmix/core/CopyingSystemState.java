/*
 * Copyright 2021 Haulmont.
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
 * Interface to be implemented by entities having a state which is not reflected in metadata. It's usually
 * a non-persistent state which resides in fields with types not supported by metadata, like {@code Object}.
 * <p>
 * Some framework mechanisms copy entity instances by copying the state of all properties known to metadata (let's
 * call it "main" state). If you want the "system" state to be copied along with the main state, implement this
 * interface and copy the state in the {@link #copyFrom(Object)} method.
 *
 * @param <T> entity type
 */
public interface CopyingSystemState<T> {

    /**
     * Invoked by the framework when copying the entity instance.
     * @param source source entity
     */
    void copyFrom(T source);
}
