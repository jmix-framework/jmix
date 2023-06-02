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

package io.jmix.dataimport.configuration.mapping;

import org.springframework.lang.Nullable;

/**
 * Property mapping describes how to map a field from input data with entity property.
 * <br>
 * Basically, a property mapping contains the following info:
 * <ul>
 *     <li>Entity property name: name of the entity property that should be populated by the value</li>
 *     <li>Data field name: a name of the field that contains a raw value for entity property.
 *         <br>
 *         If input data is XLSX or CSV, a name of the field - column header.
 *         <br>
 *         If input data is XML, a name of the field - tag name.
 *     </li>
 * </ul>
 *
 * @see SimplePropertyMapping
 * @see CustomPropertyMapping
 * @see ReferencePropertyMapping
 * @see ReferenceMultiFieldPropertyMapping
 */
public interface PropertyMapping {
    /**
     * Gets an entity property name.
     *
     * @return entity property name
     */
    String getEntityPropertyName();

    /**
     * Gets a data field name.
     *
     * @return data field name
     */
    @Nullable
    String getDataFieldName();

}
