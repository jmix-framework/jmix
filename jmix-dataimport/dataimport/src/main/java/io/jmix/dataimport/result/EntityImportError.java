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

import io.jmix.dataimport.extractor.data.ImportedDataItem;

import org.springframework.lang.Nullable;

/**
 * An object that contains the following details if an entity import fails:
 * <ul>
 *     <li>An instance of {@link ImportedDataItem}</li>
 *     <li>Extracted entity</li>
 *     <li>Error message</li>
 *     <li>Error type: {@link EntityImportErrorType}</li>
 * </ul>
 */
public class EntityImportError {
    protected ImportedDataItem importedDataItem;

    protected String errorMessage;
    protected EntityImportErrorType errorType;

    protected Object entity;

    public EntityImportError(Object entity) {
        this.entity = entity;
    }

    public EntityImportError() {
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public EntityImportError setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public ImportedDataItem getImportedDataItem() {
        return importedDataItem;
    }

    public EntityImportError setImportedDataItem(ImportedDataItem importedDataItem) {
        this.importedDataItem = importedDataItem;
        return this;
    }

    public EntityImportErrorType getErrorType() {
        return errorType;
    }

    public EntityImportError setErrorType(EntityImportErrorType errorType) {
        this.errorType = errorType;
        return this;
    }

    @Nullable
    public Object getEntity() {
        return entity;
    }

    public EntityImportError setEntity(Object entity) {
        this.entity = entity;
        return this;
    }
}
