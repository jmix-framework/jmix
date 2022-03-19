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

package io.jmix.dataimport.result;

public enum EntityImportErrorType {
    UNIQUE_VIOLATION("UNIQUE_VIOLATION"),
    VALIDATION("VALIDATION"),
    PERSISTENCE("PERSISTENCE"),
    PRE_IMPORT_PREDICATE("PRE_IMPORT_PREDICATE"),
    DATA_BINDING("DATA_BINDING"),
    NOT_IMPORTED_BATCH("NOT_IMPORTED_BATCH");

    private String id;

    EntityImportErrorType(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }
}