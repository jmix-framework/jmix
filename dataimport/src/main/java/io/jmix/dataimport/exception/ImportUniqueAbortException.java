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

package io.jmix.dataimport.exception;

import io.jmix.dataimport.extractor.data.ImportedDataItem;
import io.jmix.dataimport.extractor.entity.EntityExtractionResult;

public class ImportUniqueAbortException extends RuntimeException {
    private Object existingEntity;
    private Object createdEntity;
    private ImportedDataItem importedDataItem;

    public ImportUniqueAbortException(Object existingEntity, EntityExtractionResult entityExtractionResult) {
        this.existingEntity = existingEntity;
        this.createdEntity = entityExtractionResult.getEntity();
        this.importedDataItem = entityExtractionResult.getImportedDataItem();
    }

    public Object getExistingEntity() {
        return existingEntity;
    }

    public Object getCreatedEntity() {
        return createdEntity;
    }

    public ImportedDataItem getImportedDataItem() {
        return importedDataItem;
    }
}
