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

import java.util.ArrayList;
import java.util.List;

/**
 * An object that contains the following information about the import execution result:
 * <ul>
 *     <li>Success: whether entities import is executed successfully or not</li>
 *     <li>List of entity ids that are successfully imported</li>
 *     <li>List of {@link EntityImportError}s created for not imported entities</li>
 *     <li>Error message</li>
 * </ul>
 */
public class ImportResult {
    protected boolean success = true;

    protected List<Object> importedEntityIds = new ArrayList<>();
    protected List<EntityImportError> failedEntities = new ArrayList<>();

    protected String errorMessage;

    public boolean isSuccess() {
        return success;
    }

    public ImportResult setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public List<Object> getImportedEntityIds() {
        return importedEntityIds;
    }

    public void setImportedEntityIds(List<Object> importedEntityIds) {
        this.importedEntityIds = importedEntityIds;
    }

    public List<EntityImportError> getFailedEntities() {
        return failedEntities;
    }

    public void addImportedEntityId(Object id) {
        this.importedEntityIds.add(id);
    }

    public void addFailedEntity(EntityImportError result) {
        this.failedEntities.add(result);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ImportResult setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }
}
