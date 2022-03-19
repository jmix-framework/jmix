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

package io.jmix.dataimport;

import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.extractor.data.ImportedData;
import io.jmix.dataimport.result.ImportResult;

import java.io.InputStream;

/**
 * API to import entities from the input data using specified import configuration.
 * Supported formats of the input data:
 * <ol>
 *     <li>XLSX: an Excel file in which one row represents one entity to import</li>
 *     <li>CSV: a CSV file in which one row represents one entity to import </li>
 *     <li>JSON: a JSON file that has an object array as a root node in which one object represents one entity to import</li>
 *     <li>XML: a XML file that has one root tag containing child tags with equal structure. One child tag represents one entity to import.</li>
 * </ol>
 *
 * @see ImportConfiguration
 */
public interface DataImporter {
    /**
     * Creates the entities using data from the given byte array (XLSX, CSV, JSON, XML) and specified import configuration
     * and saves these entities to the database.
     *
     * @param configuration import configuration
     * @param content       source (XLSX, CSV, JSON, XML) that contains data for the entities to import
     * @return object that contains result of import execution
     */
    ImportResult importData(ImportConfiguration configuration, byte[] content);

    /**
     * Creates the entities using data from the given input stream (XLSX, CSV, JSON, XML) and specified import configuration
     * and saves these entities to the database.
     *
     * @param configuration import configuration
     * @param inputStream   source (XLSX, CSV, JSON, XML) that contains data for the entities to import
     * @return object that contains result of import execution
     */
    ImportResult importData(ImportConfiguration configuration, InputStream inputStream);

    /**
     * Creates the entities using data from the given {@link ImportedData} object and specified import configuration
     * and saves these entities to the database.
     *
     * @param configuration import configuration
     * @param importedData  imported data
     * @return object that contains result of import execution
     */
    ImportResult importData(ImportConfiguration configuration, ImportedData importedData);
}
