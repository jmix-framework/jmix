/*
 * Copyright (c) 2008-2017 Haulmont.
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

package io.jmix.ui.component.impl;

import com.vaadin.server.StreamResource;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.FileStorageResource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("ui_FileStorageResource")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FileStorageResourceImpl<T> extends AbstractStreamSettingsResource
        implements WebResource, FileStorageResource<T> {

    protected static final String FILE_STORAGE_EXCEPTION_MESSAGE = "Can't create FileStorageResource. " +
            "An error occurred while obtaining a file from the storage";

    protected FileStorageLocator fileStorageLocator;

    protected T fileReference;
    protected FileStorage<T> fileStorage;

    protected String mimeType;

    @Autowired
    public void setFileStorageLocator(FileStorageLocator fileStorageLocator) {
        this.fileStorageLocator = fileStorageLocator;
    }

    @Override
    public FileStorageResource<T> setFileReference(T fileReference) {
        Preconditions.checkNotNullArgument(fileReference);

        this.fileReference = fileReference;
        hasSource = true;

        fireResourceUpdateEvent();

        return this;
    }

    @Override
    public T getFileReference() {
        return fileReference;
    }

    @Override
    protected void createResource() {
        resource = new StreamResource(() -> {
            try {
                return getFileStorage().openStream(fileReference);
            } catch (FileStorageException e) {
                throw new RuntimeException(FILE_STORAGE_EXCEPTION_MESSAGE, e);
            }
        }, getResourceName());

        StreamResource streamResource = (StreamResource) this.resource;

        streamResource.setCacheTime(cacheTime);
        streamResource.setBufferSize(bufferSize);
    }

    @SuppressWarnings("unchecked")
    protected FileStorage<T> getFileStorage() {
        if (fileStorage == null) {
            FileStorage<?> defaultFileStorage = fileStorageLocator.getDefault();
            if (!defaultFileStorage.getReferenceType().isAssignableFrom(fileReference.getClass())) {
                throw new IllegalArgumentException("Reference type is not compatible with the default file storage");
            }
            fileStorage = (FileStorage<T>) defaultFileStorage;
        }
        return fileStorage;
    }

    public void setFileStorage(FileStorage<T> fileStorage) {
        this.fileStorage = fileStorage;
    }

    protected String getResourceName() {
        StringBuilder name = new StringBuilder();

        String fullName = StringUtils.isNotEmpty(fileName)
                ? fileName
                : getFileStorage().getFileName(fileReference);
        String baseName = FilenameUtils.getBaseName(fullName);

        if (StringUtils.isEmpty(baseName)) {
            return UUID.randomUUID().toString();
        }

        String extension = FilenameUtils.getExtension(fullName);

        return name.append(baseName)
                .append('-')
                .append(UUID.randomUUID().toString())
                .append('.')
                .append(extension)
                .toString();
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;

        if (resource != null) {
            ((StreamResource) resource).setMIMEType(mimeType);
        }
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }
}
