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
import io.jmix.dataimport.configuration.mapping.CustomPropertyMapping;

import java.util.Map;

/**
 * Input parameter for a function to get a custom value of property.
 *
 * @see CustomPropertyMapping
 */
public class CustomMappingContext {
    protected ImportConfiguration importConfiguration;
    protected Map<String, Object> rawValues;

    public ImportConfiguration getImportConfiguration() {
        return importConfiguration;
    }

    public CustomMappingContext setImportConfiguration(ImportConfiguration importConfiguration) {
        this.importConfiguration = importConfiguration;
        return this;
    }

    public Map<String, Object> getRawValues() {
        return rawValues;
    }

    public CustomMappingContext setRawValues(Map<String, Object> rawValues) {
        this.rawValues = rawValues;
        return this;
    }
}
