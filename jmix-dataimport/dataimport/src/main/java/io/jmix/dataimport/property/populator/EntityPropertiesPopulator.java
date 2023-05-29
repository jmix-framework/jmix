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

package io.jmix.dataimport.property.populator;

import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.configuration.mapping.PropertyMapping;
import io.jmix.dataimport.configuration.mapping.ReferenceMultiFieldPropertyMapping;
import io.jmix.dataimport.extractor.data.ImportedDataItem;
import io.jmix.dataimport.extractor.data.RawValuesSource;

import org.springframework.lang.Nullable;
import java.util.List;
import java.util.Map;

/**
 * API to populate the entity by property values using property mappings and raw values source.
 */
public interface EntityPropertiesPopulator {

    /**
     * Populates the entity properties for which mappings are set in the import configuration by values.
     *
     * @param entity              entity which properties are populated by values
     * @param importConfiguration import configuration
     * @param dataItem            source of raw values of properties
     * @return object that contains entity populated by property values and info about created references
     */
    EntityInfo populateProperties(Object entity, ImportConfiguration importConfiguration, ImportedDataItem dataItem);

    /**
     * Populates the entity properties for which mappings are set in the import configuration by values.
     * For import policies that require loading of existing reference:
     * Many-to-one reference is created if an existing entity does not exist in the database and is not presented in the created references.
     *
     * @param entity              entity which properties are populated by values
     * @param importConfiguration import configuration
     * @param dataItem            source of raw values of properties
     * @param createdReferences   map that contains already created reference entity by property mappings
     * @return object that contains entity populated by property values and info about created references
     */
    EntityInfo populateProperties(Object entity, ImportConfiguration importConfiguration, ImportedDataItem dataItem,
                                  @Nullable Map<PropertyMapping, List<Object>> createdReferences);

    /**
     * Populates the reference entity properties for which mappings are set in the reference mapping by values.
     *
     * @param entity              reference entity which properties are populated by values
     * @param referenceMapping    reference mapping
     * @param importConfiguration import configuration
     * @param rawValuesSource     source of raw values of properties
     * @return reference entity with populated by property values
     */
    Object populateReference(Object entity, ReferenceMultiFieldPropertyMapping referenceMapping, ImportConfiguration importConfiguration, RawValuesSource rawValuesSource);
}
