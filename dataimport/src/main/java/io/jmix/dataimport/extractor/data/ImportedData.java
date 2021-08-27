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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of input data processing that contains:
 * <ul>
 *     <li>List of {@link ImportedDataItem}s: one imported data item contains the raw values of properties for one entity to import.</li>
 *     <li>Data field names: names of the fields from input data (XLSX, CSV - column names specified in the first row,
 *     JSON - field names from JSON object, XML - tag names).</li>
 * </ul>
 */
public class ImportedData {
    protected List<ImportedDataItem> items = new ArrayList<>();
    protected List<String> dataFieldNames = new ArrayList<>(); //names to map the fields from file to entity properties

    public List<ImportedDataItem> getItems() {
        return items;
    }

    public void setItems(List<ImportedDataItem> items) {
        this.items = items;
    }

    public List<String> getDataFieldNames() {
        return dataFieldNames;
    }

    public void setDataFieldNames(List<String> dataFieldNames) {
        this.dataFieldNames = dataFieldNames;
    }

    public void addItem(ImportedDataItem item) {
        this.items.add(item);
    }

    public void addDataFieldName(String dataFieldName) {
        this.dataFieldNames.add(dataFieldName);
    }
}
