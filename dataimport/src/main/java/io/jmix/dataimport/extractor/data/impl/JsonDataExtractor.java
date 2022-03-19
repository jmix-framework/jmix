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

package io.jmix.dataimport.extractor.data.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.dataimport.InputDataFormat;
import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.exception.ImportException;
import io.jmix.dataimport.extractor.data.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component("datimp_JsonDataExtractor")
public class JsonDataExtractor implements ImportedDataExtractor {

    @Override
    public ImportedData extract(ImportConfiguration importConfiguration, InputStream inputStream) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(inputStream);
            return getImportedData(rootNode);
        } catch (JsonProcessingException e) {
            throw new ImportException(e, "Error while parsing JSON: " + e.getMessage());
        } catch (IOException e) {
            throw new ImportException(e, "I/O error: " + e.getMessage());
        }
    }

    @Override
    public ImportedData extract(ImportConfiguration importConfiguration, byte[] inputData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(inputData);
            return getImportedData(rootNode);
        } catch (JsonProcessingException e) {
            throw new ImportException(e, "Error while parsing JSON: " + e.getMessage());
        } catch (IOException e) {
            throw new ImportException(e, "I/O error: " + e.getMessage());
        }
    }

    @Override
    public String getSupportedDataFormat() {
        return InputDataFormat.JSON;
    }

    protected ImportedData getImportedData(JsonNode rootNode) {
        ImportedData importedData = new ImportedData();
        if (rootNode.isArray()) {
            Iterator<JsonNode> iterator = rootNode.elements();
            int itemIndex = 1;
            List<String> dataFieldNames = new ArrayList<>();

            while (iterator.hasNext()) {
                JsonNode entityJsonNode = iterator.next();
                ImportedDataItem importedDataItem = createImportedDataItem(entityJsonNode, itemIndex);
                importedData.addItem(importedDataItem);
                itemIndex++;

                Iterator<String> dataFieldNamesIterator = entityJsonNode.fieldNames();
                while (dataFieldNamesIterator.hasNext()) {
                    String dataFieldName = dataFieldNamesIterator.next();
                    if (!dataFieldNames.contains(dataFieldName)) {
                        dataFieldNames.add(dataFieldName);
                    }
                }
            }
            importedData.setDataFieldNames(dataFieldNames);
        } else if (rootNode.isObject()) {
            Iterator<String> dataFieldNamesIterator = rootNode.fieldNames();
            while (dataFieldNamesIterator.hasNext()) {
                importedData.addDataFieldName(dataFieldNamesIterator.next());
            }
            ImportedDataItem importedDataItem = createImportedDataItem(rootNode, 1);
            importedData.addItem(importedDataItem);

        }
        return importedData;
    }

    protected ImportedDataItem createImportedDataItem(JsonNode jsonNode, int itemIndex) {
        ImportedDataItem item = new ImportedDataItem();
        item.setItemIndex(itemIndex);
        readRawValues(jsonNode, item);
        return item;
    }

    protected ImportedObject createImportedObject(JsonNode objectNode) {
        ImportedObject importedObject = new ImportedObject();
        readRawValues(objectNode, importedObject);
        return importedObject;
    }

    protected void readRawValues(JsonNode objectNode, RawValuesSource rawValuesSource) {
        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            Object rawValue;
            JsonNode childNode = field.getValue();
            if (childNode.isNull()) {
                rawValue = null;
            } else {
                if (childNode.isObject()) {
                    ImportedObject importedObject = createImportedObject(childNode);
                    importedObject.setDataFieldName(field.getKey());
                    rawValue = importedObject;
                } else if (childNode.isArray()) {
                    ImportedObjectList importedObjectList = createImportedObjectList(childNode);
                    importedObjectList.setDataFieldName(field.getKey());
                    rawValue = importedObjectList;
                } else {
                    rawValue = childNode.asText();
                }
            }
            rawValuesSource.addRawValue(field.getKey(), rawValue);
        }
    }

    protected ImportedObjectList createImportedObjectList(JsonNode rootNode) {
        ImportedObjectList listObject = new ImportedObjectList();
        Iterator<JsonNode> children = rootNode.elements();
        while (children.hasNext()) {
            JsonNode childNode = children.next();
            listObject.addImportedObject(createImportedObject(childNode));
        }
        return listObject;
    }


}
