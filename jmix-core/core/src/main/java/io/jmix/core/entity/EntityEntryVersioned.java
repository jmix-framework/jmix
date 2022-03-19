/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.entity;

import io.jmix.core.annotation.Internal;

/**
 * Interface to be implemented by optimistically locked entities.
 */
@Internal
public interface EntityEntryVersioned {

    Object getVersion();

    /**
     * Do not set version if you are not sure - it must be null for a new entity or loaded from the database
     * for a persistent one.
     */
    void setVersion(Object version);
}
