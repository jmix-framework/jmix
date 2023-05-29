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

import org.springframework.lang.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents an object in JSON or XML with the following info:
 * <ul>
 *     <li>Data field name: for JSON - field name, for XML - tag name</li>
 *     <li>Raw values map: for JSON - raw values of JSON object fields by field names,
 *     for XML: values of child tags by tag names</li>
 * </ul>
 * <br>
 * JSON example:
 * <br>
 * The "customer" object represents {@link ImportedObject} with data field name = "customer" and two raw values: "name" and "email".
 * <pre>
 * [
 *   {
 *     "orderNumber": "#001",
 *     "orderDate": "12/07/2021",
 *     "orderAmount": 100,
 *     "customer": {
 *       "name": "Shelby Robinson",
 *       "email": "robinson@mail.com"
 *     }
 *   }
 * ]
 * </pre>
 * <p>
 * XML example:
 * <br>
 * The "customer" tag represents {@link ImportedObject} with data field name = "customer" and two raw values: "name" and "email".
 * <pre>
 * &lt;orders&gt;
 *     &lt;order&gt;
 *         &lt;number&gt;#001&lt;/number&gt;
 *         &lt;amount&gt;50.5&lt;/amount&gt;
 *         &lt;date&gt;12/02/2021 12:00&lt;/date&gt;
 *         &lt;customer&gt;
 *             &lt;name&gt;Parker Leighton&lt;/name&gt;
 *             &lt;email&gt;leighton@mail.com&lt;/email&gt;
 *         &lt;/customer&gt;
 *     &lt;/order&gt;
 * &lt;/orders&gt;
 * </pre>
 */
public class ImportedObject implements RawValuesSource {
    protected String dataFieldName;
    protected Map<String, Object> rawValues = new HashMap<>();

    public Map<String, Object> getRawValues() {
        return rawValues;
    }

    public void setRawValues(Map<String, Object> rawValues) {
        this.rawValues = rawValues;
    }

    public String getDataFieldName() {
        return dataFieldName;
    }

    public void setDataFieldName(String dataFieldName) {
        this.dataFieldName = dataFieldName;
    }

    public RawValuesSource addRawValue(String dataFieldName, @Nullable Object value) {
        this.rawValues.put(dataFieldName, value);
        return this;
    }

    @Nullable
    public Object getRawValue(String dataFieldName) {
        return rawValues.get(dataFieldName);
    }

    @Override
    public String toString() {
        return String.format("Field: %s, Data: {%s}", dataFieldName, rawValues.entrySet().stream()
                .map(rawValueEntry -> String.format("%s: %s", rawValueEntry.getKey(), getValueString(rawValueEntry)))
                .collect(Collectors.joining(", ")));
    }

    @Nullable
    private String getValueString(Map.Entry<String, Object> rawValueEntry) {
        return Optional.ofNullable(rawValueEntry.getValue()).map(Object::toString).orElse(null);
    }
}
