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

package io.jmix.core;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;

import java.util.Set;

/**
 * Interface to provide additional properties of MetaClass, e.g. dynamic attributes.
 */
public interface MetadataExtension {

    /**
     * Returns set additional properties of the given MetaClass.
     *
     * @param metaClass MetaClass instance
     * @return Set of MetaProperties
     */
    Set<MetaProperty> getAdditionalProperties(MetaClass metaClass);

    boolean isAdditionalProperty(MetaClass metaClass, String propertyName);
}
