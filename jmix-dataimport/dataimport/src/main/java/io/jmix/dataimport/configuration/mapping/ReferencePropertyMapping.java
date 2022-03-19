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

package io.jmix.dataimport.configuration.mapping;

/**
 * Mapping for reference property by one lookup property that has a raw value in the input data.
 * <br>
 * This mapping allows to set:
 * <ul>
 *     <li>Entity property name: name of the reference property</li>
 *     <li>Lookup property name: name of the property from reference entity</li>
 *     <li>Data field name: name of the data field that contains a raw value of lookup property</li>
 *     <li>Reference import policy. Default value: {@link ReferenceImportPolicy#IGNORE_IF_MISSING}</li>
 * </ul>
 *
 * @see ReferenceImportPolicy
 */
public class ReferencePropertyMapping implements PropertyMapping {
    private String entityPropertyName;
    private String dataFieldName;
    private String lookupPropertyName;
    private ReferenceImportPolicy referenceImportPolicy;

    public ReferencePropertyMapping(String entityPropertyName, String dataFieldName, String lookupPropertyName,
                                    ReferenceImportPolicy importPolicy) {
        this.entityPropertyName = entityPropertyName;
        this.dataFieldName = dataFieldName;
        this.lookupPropertyName = lookupPropertyName;
        this.referenceImportPolicy = importPolicy;
    }

    public String getEntityPropertyName() {
        return entityPropertyName;
    }

    public String getDataFieldName() {
        return dataFieldName;
    }

    public String getLookupPropertyName() {
        return lookupPropertyName;
    }

    public ReferenceImportPolicy getReferenceImportPolicy() {
        return referenceImportPolicy;
    }

}
