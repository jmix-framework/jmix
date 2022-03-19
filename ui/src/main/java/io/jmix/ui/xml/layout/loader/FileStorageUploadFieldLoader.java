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

package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.component.FileStorageUploadField;
import io.jmix.ui.component.FileStorageUploadField.FileStoragePutMode;

public class FileStorageUploadFieldLoader extends FileUploadFieldLoader<FileStorageUploadField> {

    @Override
    public void loadComponent() {
        super.loadComponent();
        loadString(element, "fileStorage", resultComponent::setFileStorageName);
        loadEnum(element, FileStoragePutMode.class, "fileStoragePutMode", resultComponent::setMode);
    }

    @Override
    public void createComponent() {
        resultComponent = factory.create(FileStorageUploadField.NAME);
        loadId(resultComponent, element);
    }
}
