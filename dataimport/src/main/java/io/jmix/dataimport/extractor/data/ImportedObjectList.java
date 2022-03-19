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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents an object list in JSON or XML with the following info:
 * <ul>
 *     <li>Data field name: for JSON - field name, for XML - tag name</li>
 *     <li>List of {@link ImportedObject}s</li>
 * </ul>
 * <br>
 * JSON example:
 * <br>
 * The "items" JSON array represents {@link ImportedObjectList} with data field name = "items" and two {@link ImportedObject}s.
 * <pre>
 * [
 *   {
 *     "orderNumber": "#001",
 *     "orderDate": "12/07/2021",
 *     "orderAmount": 100,
 *     "items": [
 *       {
 *         "productName": "Outback Power Nano-Carbon Battery 12V",
 *         "quantity": 4
 *       },
 *       {
 *         "productName": "Fullriver Sealed Battery 6V",
 *         "quantity": 5
 *       }
 *     ]
 *   }
 * ]
 * </pre>
 * <p>
 * XML example:
 * <br>
 * The "items" tag represents {@link ImportedObjectList} with data field name = "items" and two {@link ImportedObject}s.
 * <pre>
 * &lt;orders&gt;
 *     &lt;order&gt;
 *         &lt;number&gt;#001&lt;/number&gt;
 *         &lt;amount&gt;50.5&lt;/amount&gt;
 *         &lt;date&gt;12/02/2021 12:00&lt;/date&gt;
 *         &lt;items&gt;
 *             &lt;item&gt;
 *                 &lt;productName&gt;Outback Power Nano-Carbon Battery 12V&lt;/productName&gt;
 *                 &lt;quantity&gt;5&lt;/quantity&gt;
 *             &lt;/item&gt;
 *             &lt;item&gt;
 *                 &lt;productName&gt;Fullriver Sealed Battery 6V&lt;/productName&gt;
 *                 &lt;quantity&gt;4&lt;/quantity&gt;
 *             &lt;/item&gt;
 *         &lt;/items&gt;
 *     &lt;/order&gt;
 * &lt;/orders&gt;
 * </pre>
 */
public class ImportedObjectList {
    protected String dataFieldName;
    protected List<ImportedObject> importedObjects = new ArrayList<>();

    public String getDataFieldName() {
        return dataFieldName;
    }

    public void setDataFieldName(String dataFieldName) {
        this.dataFieldName = dataFieldName;
    }

    public List<ImportedObject> getImportedObjects() {
        return importedObjects;
    }

    public void setImportedObjects(List<ImportedObject> importedObjects) {
        this.importedObjects = importedObjects;
    }

    public ImportedObjectList addImportedObject(ImportedObject importedObject) {
        this.importedObjects.add(importedObject);
        return this;
    }

    @Override
    public String toString() {
        return String.format("Field: %s, objects: [%s]", dataFieldName, importedObjects.stream()
                .filter(Objects::nonNull)
                .map(ImportedObject::toString)
                .collect(Collectors.joining(", ")));
    }
}
