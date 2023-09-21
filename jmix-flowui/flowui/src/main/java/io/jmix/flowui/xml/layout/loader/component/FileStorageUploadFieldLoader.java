/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import io.jmix.flowui.component.upload.FileStorageUploadField;
import io.jmix.flowui.component.upload.UploadFieldI18NSupport;
import io.jmix.flowui.kit.component.upload.FileStoragePutMode;
import io.jmix.flowui.kit.component.upload.JmixUploadI18N;

public class FileStorageUploadFieldLoader extends AbstractUploadFieldLoader<FileStorageUploadField> {

    @Override
    protected FileStorageUploadField createComponent() {
        return factory.create(FileStorageUploadField.class);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        componentLoader().loadRequired(resultComponent, element, context);
        componentLoader().loadValidationAttributes(resultComponent, element, context);

        getLoaderSupport().loadString(element, "fileStorageName", resultComponent::setFileStorageName);
        getLoaderSupport().loadEnum(element, FileStoragePutMode.class, "fileStoragePutMode",
                resultComponent::setFileStoragePutMode);
    }

    @Override
    protected JmixUploadI18N createI18N() {
        return applicationContext.getBean(UploadFieldI18NSupport.class).getI18nFileStorageUploadField();
    }
}
