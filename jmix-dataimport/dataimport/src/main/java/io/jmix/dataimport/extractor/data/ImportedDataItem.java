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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a source of raw values for properties for one entity to import.
 * <br>
 * Contains the following info:
 * <ul>
 *     <li>Item index:
 *     <ol>
 *         <li>CSV, XLSX - row number;</li>
 *         <li>JSON - index of JSON object in the object array;</li>
 *         <li>XML - sequential number of XML child element in the root element.</li>
 *     </ol>
 *     </li>
 *     <li>Raw values map:
 *     <ol>
 *         <li>CSV, XLSX - string cell values by column names;</li>
 *         <li>JSON - field values by field names. As a field value can be: string (for all simple fields), {@link ImportedObject} or {@link ImportedObjectList};</li>
 *         <li>XML - tag value by tag names. As a tag value can be: string (for tags without child tags), {@link ImportedObject} or {@link ImportedObjectList}.</li>
 *     </ol>
 *     </li>
 * </ul>
 */
public class ImportedDataItem implements RawValuesSource {
    protected Map<String, Object> rawValues = new HashMap<>();
    protected int itemIndex;

    public int getItemIndex() {
        return itemIndex;
    }

    public Map<String, Object> getRawValues() {
        return rawValues;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public void setRawValues(Map<String, Object> rawValues) {
        this.rawValues = rawValues;
    }

    public ImportedDataItem addRawValue(String dataFieldName, @Nullable Object value) {
        this.rawValues.put(dataFieldName, value);
        return this;
    }

    @Nullable
    public Object getRawValue(String dataFieldName) {
        return rawValues.get(dataFieldName);
    }

    @Override
    public String toString() {
        return String.format("Item index: %s, Data: %s", itemIndex,
                rawValues.entrySet().stream()
                        .map(rawValueEntry -> String.format("%s: %s", rawValueEntry.getKey(), rawValueEntry.getValue()))
                        .collect(Collectors.joining(", ")));
    }
}
