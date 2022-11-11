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

package io.jmix.core.entity;

import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;

/**
 * Interface to be implemented by entities that are not included in static metadata, but can provide a {@code MetaClass}
 * specifically for each instance.
 */
@Internal
public interface HasInstanceMetaClass {

    /**
     * @return metaclass of this entity instance.
     * @throws IllegalStateException if the instance has no metaclass specified
     */
    MetaClass getInstanceMetaClass();

    /**
     * Sets the instance metaclass
     * @param metaClass
     */
    void setInstanceMetaClass(MetaClass metaClass);

    /**
     * @return true if the instance has a metaclass and call of {@link #getInstanceMetaClass()} is safe
     */
    boolean hasInstanceMetaClass();
}
