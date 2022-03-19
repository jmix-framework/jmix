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

import io.jmix.core.common.util.Dom4j;
import io.jmix.dataimport.InputDataFormat;
import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.extractor.data.*;
import org.apache.commons.collections4.CollectionUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("datimp_XmlDataExtractor")
public class XmlDataExtractor implements ImportedDataExtractor {

    @Override
    public ImportedData extract(ImportConfiguration importConfiguration, InputStream inputStream) {
        Document document = Dom4j.readDocument(inputStream);
        return getImportedData(document);
    }

    @Override
    public ImportedData extract(ImportConfiguration importConfiguration, byte[] inputData) {
        Document document = Dom4j.readDocument(new ByteArrayInputStream(inputData));
        return getImportedData(document);
    }

    @Override
    public String getSupportedDataFormat() {
        return InputDataFormat.XML;
    }

    protected ImportedData getImportedData(Document document) {
        Element rootElement = document.getRootElement();
        boolean containsSimpleValues = containsSimpleValues(rootElement);
        ImportedData importedData = new ImportedData();
        if (!containsSimpleValues) {
            List<Element> elements = rootElement.elements();
            if (CollectionUtils.isNotEmpty(elements)) {
                int itemIndex = 1;
                List<String> dataFieldNames = new ArrayList<>();
                for (Element element : elements) {
                    ImportedDataItem importedDataItem = createImportedDataItem(element, itemIndex);
                    importedData.addItem(importedDataItem);
                    itemIndex++;

                    element.elements().forEach(child -> {
                        String name = child.getName();
                        if (!dataFieldNames.contains(name)) {
                            dataFieldNames.add(name);
                        }
                    });
                }
                importedData.setDataFieldNames(dataFieldNames);
            }
        } else {
            ImportedDataItem importedDataItem = createImportedDataItem(rootElement, 1);
            importedData.addItem(importedDataItem);
            importedData.setDataFieldNames(rootElement.elements()
                    .stream().map(Node::getName)
                    .distinct()
                    .collect(Collectors.toList()));
        }
        return importedData;
    }

    protected ImportedDataItem createImportedDataItem(Element parentElement, int itemIndex) {
        ImportedDataItem item = new ImportedDataItem();
        item.setItemIndex(itemIndex);
        readRawValues(parentElement, item);
        return item;
    }

    protected ImportedObject createImportedObject(Element parentElement) {
        ImportedObject objectValue = new ImportedObject();
        objectValue.setDataFieldName(parentElement.getName());
        readRawValues(parentElement, objectValue);
        return objectValue;
    }

    protected void readRawValues(Element parentElement, RawValuesSource rawValuesSource) {
        parentElement.elements().stream().filter(Element::isTextOnly).forEach(element -> rawValuesSource.addRawValue(element.getName(), element.getTextTrim()));
        Map<String, List<Element>> elementsByTag = groupElementsByTag(parentElement);
        elementsByTag.forEach((tagName, elements) -> {
            if (elements.size() == 1) {
                Element element = elements.get(0);
                if (containsSimpleValues(element)) {
                    ImportedObject importedObject = createImportedObject(elements.get(0));
                    importedObject.setDataFieldName(tagName);
                    rawValuesSource.addRawValue(tagName, importedObject);
                } else {
                    ImportedObjectList importedObjectList = createImportedObjectList(element.elements());
                    importedObjectList.setDataFieldName(tagName);
                    rawValuesSource.addRawValue(tagName, importedObjectList);
                }
            } else if (elements.size() > 1) {
                ImportedObjectList importedObjectList = createImportedObjectList(elements);
                importedObjectList.setDataFieldName(tagName);
                rawValuesSource.addRawValue(tagName, importedObjectList);
            }
        });
    }

    protected boolean containsSimpleValues(Element element) {
        return element.elements().stream().anyMatch(Element::isTextOnly);
    }

    protected ImportedObjectList createImportedObjectList(List<Element> elements) {
        ImportedObjectList listObject = new ImportedObjectList();
        elements.forEach(element -> listObject.addImportedObject(createImportedObject(element)));
        return listObject;
    }

    protected Map<String, List<Element>> groupElementsByTag(Element parentElement) {
        Map<String, List<Element>> elementMap = new HashMap<>();
        parentElement.elements().stream().filter(element -> !element.isTextOnly()).forEach(element -> {
            String name = element.getName();
            if (!elementMap.containsKey(name)) {
                elementMap.put(name, new ArrayList<>());
            }
            elementMap.get(name).add(element);
        });
        return elementMap;
    }
}
