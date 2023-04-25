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

import jakarta.annotation.Nullable;
import java.util.Map;

/**
 * Interface for source of extracted raw values
 */
public interface RawValuesSource {
    /**
     * @return map that contains raw values by data field names
     */
    Map<String, Object> getRawValues();


    /**
     * Sets a map containing raw values of data fields.
     *
     * @param rawValues map with raw values
     */
    void setRawValues( Map<String, Object> rawValues);

    /**
     * Adds a raw value of specified data field
     *
     * @param dataFieldName data field name
     * @param rawValue raw value
     * @return current instance of raw values source
     */
    RawValuesSource addRawValue(String dataFieldName, @Nullable Object rawValue);

    /**
     * Gets a raw value of the specified data field
     *
     * @param dataFieldName data field name
     * @return extracted raw value
     */
    @Nullable
    Object getRawValue(String dataFieldName);
}
