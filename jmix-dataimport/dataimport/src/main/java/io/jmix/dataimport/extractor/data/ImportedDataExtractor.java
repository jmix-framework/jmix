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

package io.jmix.dataimport.extractor.data;


import io.jmix.dataimport.configuration.ImportConfiguration;

import java.io.InputStream;

/**
 * Parses the data from source and returns an instance of {@link ImportedData} containing list of {@link ImportedDataItem} with raw values
 */
public interface ImportedDataExtractor {

    /**
     * Parses input data specified as input stream and creates an instance of {@link ImportedData}.
     *
     * @param importConfiguration import configuration
     * @param inputStream input data
     * @return an instance of {@link ImportedData} containing list of {@link ImportedDataItem} with raw values for entity properties
     */
    ImportedData extract(ImportConfiguration importConfiguration, InputStream inputStream);

    /**
     * Parses input data specified as a byte array and creates an instance of {@link ImportedData}.
     *
     * @param importConfiguration import configuration
     * @param inputData input data
     * @return an instance of {@link ImportedData} containing list of {@link ImportedDataItem} with raw values for entity properties
     */
    ImportedData extract(ImportConfiguration importConfiguration, byte[] inputData);

    /**
     * @return supported format of input data
     */
    String getSupportedDataFormat();
}
