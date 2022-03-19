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

package io.jmix.search.searching;

import java.util.List;

/**
 * Represents single entity instance found in index
 */
public class SearchResultEntry {

    private final String docId;
    private final String instanceName;
    private final String entityName;
    private final List<FieldHit> fieldHits;

    public SearchResultEntry(String docId, String instanceName, String entityName, List<FieldHit> fieldHits) {
        this.docId = docId;
        this.instanceName = instanceName;
        this.entityName = entityName;
        this.fieldHits = fieldHits;
    }

    public String getDocId() {
        return docId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getEntityName() {
        return entityName;
    }

    public List<FieldHit> getFieldHits() {
        return fieldHits;
    }
}
