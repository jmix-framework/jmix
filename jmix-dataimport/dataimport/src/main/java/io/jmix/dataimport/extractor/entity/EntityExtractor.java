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

package io.jmix.dataimport.extractor.entity;

import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.extractor.data.ImportedData;
import io.jmix.dataimport.extractor.data.ImportedDataItem;

import java.util.List;

/**
 * Provides methods to extract entities and populate their properties by values from the imported data
 */
public interface EntityExtractor {
    /**
     * Creates an entity using {@link ImportConfiguration#entityClass} and populates the properties for which mappings are set in import configuration.
     * Values for the properties are got by processing the raw values from {@link ImportedDataItem}.
     *
     * @param importConfiguration import configuration
     * @param dataItem            source of raw values for the entity
     * @return extraction result with entity which properties are populated by values
     */
    EntityExtractionResult extractEntity(ImportConfiguration importConfiguration, ImportedDataItem dataItem);

    /**
     * Creates an entity for each {@link ImportedDataItem} from the specified imported data.
     *
     * @param importConfiguration import configuration
     * @param importedData        imported data
     * @return extraction results for each extracted entity
     */
    List<EntityExtractionResult> extractEntities(ImportConfiguration importConfiguration, ImportedData importedData);

    /**
     * Creates an entity for each {@link ImportedDataItem} from the specified list.
     *
     * @param importConfiguration import configuration
     * @param importedDataItems   imported data items
     * @return extraction results for each extracted entity
     */
    List<EntityExtractionResult> extractEntities(ImportConfiguration importConfiguration, List<ImportedDataItem> importedDataItems);
}
