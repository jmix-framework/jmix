/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@ManagedResource(description = "Manages TemporaryStorage", objectName = "jmix.ui:type=TemporaryStorage")
@Component("ui_TemporaryStorageManagementFacade")
public class TemporaryStorageManagementFacade {
    @Autowired
    protected TemporaryStorageImpl temporaryStorage;

    @ManagedOperation(description = "Show temp files")
    public String showTempFiles() {
        return temporaryStorage.showTempFiles();
    }

    @ManagedOperation(description = "Clear temp directory")
    public String clearTempDirectory() {
        temporaryStorage.clearTempDirectory();
        return "Done";
    }
}
