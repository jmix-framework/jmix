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
import io.jmix.core.FileRef;
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
public class FileStorageResourceImpl extends AbstractStreamSettingsResource
        implements WebResource, FileStorageResource {

    protected FileStorageLocator fileStorageLocator;

    protected FileRef fileReference;

    protected String mimeType;

    @Autowired
    public void setFileStorageLocator(FileStorageLocator fileStorageLocator) {
        this.fileStorageLocator = fileStorageLocator;
    }

    @Override
    public FileStorageResource setFileReference(FileRef fileReference) {
        Preconditions.checkNotNullArgument(fileReference);

        this.fileReference = fileReference;
        hasSource = true;

        fireResourceUpdateEvent();

        return this;
    }

    @Override
    public FileRef getFileReference() {
        return fileReference;
    }

    @Override
    protected void createResource() {
        resource = new StreamResource(() ->
                fileStorageLocator.getByName(fileReference.getStorageName()).openStream(fileReference),
                getResourceName());

        StreamResource streamResource = (StreamResource) this.resource;

        streamResource.setCacheTime(cacheTime);
        streamResource.setBufferSize(bufferSize);
    }

    protected String getResourceName() {
        StringBuilder name = new StringBuilder();

        String fullName = StringUtils.isNotEmpty(fileName)
                ? fileName
                : fileReference.getFileName();
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
