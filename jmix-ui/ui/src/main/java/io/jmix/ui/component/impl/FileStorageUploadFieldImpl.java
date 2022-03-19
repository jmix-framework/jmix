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

package io.jmix.ui.component.impl;

import com.vaadin.ui.Button;
import io.jmix.core.DevelopmentException;
import io.jmix.core.FileRef;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.FileStorageUploadField;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.widget.JmixFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.InputStream;

public class FileStorageUploadFieldImpl extends AbstractFileStorageUploadField<FileRef>
        implements FileStorageUploadField {

    private static final Logger log = LoggerFactory.getLogger(FileStorageUploadFieldImpl.class);

    @Override
    protected void onUploadSucceeded(JmixFileUpload.SucceededEvent event) {
        fileName = event.getFileName();
        fileId = tempFileId;

        saveFile(fileName);
        component.setFileNameButtonCaption(fileName);

        super.onUploadSucceeded(event);
    }

    protected void saveFile(String fileName) {
        checkFileStorageInitialized();
        switch (mode) {
            case MANUAL:
                internalValueChangedOnUpload = true;
                setValue(null); //FileRef should be set manually after uploading file into storage
                setValueToPresentation(fileName);
                internalValueChangedOnUpload = false;
                break;
            case IMMEDIATE:
                FileRef fileRef = temporaryStorage.putFileIntoStorage(fileId, fileName, fileStorage);
                setValue(fileRef);
                break;
        }
    }

    @Override
    protected void onFileNameClick(Button.ClickEvent e) {
        FileRef value = getValue();
        if (value == null && fileId == null) {
            return;
        }

        switch (mode) {
            case MANUAL:
                String name = getFileName();
                String fileName = StringUtils.isEmpty(name) ?
                        (value != null ? value.getFileName() : "attachment") : name;
                downloader.download(this::getFileContent, fileName);
                break;
            case IMMEDIATE:
                if (value == null) {
                    return;
                }
                downloader.download(this::getFileContent, value.getFileName());
                break;
        }
    }

    @Override
    protected void valueBindingConnected(ValueSource<FileRef> valueSource) {
        super.valueBindingConnected(valueSource);
        if (valueSource instanceof EntityValueSource) {
            MetaPropertyPath metaPropertyPath = ((EntityValueSource) valueSource).getMetaPropertyPath();
            if (metaPropertyPath.getRange().isDatatype()) {
                Datatype datatype = metaPropertyPath.getRange().asDatatype();
                if (!FileRef.class.isAssignableFrom(datatype.getJavaClass())) {
                    throw new DevelopmentException("FileStorageUploadField doesn't support Datatype with class: " + datatype.getJavaClass());
                }
            } else {
                throw new DevelopmentException("FileStorageUploadField doesn't support properties with association");
            }
        }
    }

    @Override
    public InputStream getFileContent() {
        if (contentProvider != null) {
            return contentProvider.get();
        }

        FileRef fileRef = getValue();
        return getFileContent(fileRef);
    }

    @Nullable
    @Override
    protected String convertToPresentation(@Nullable FileRef modelValue) throws ConversionException {
        return modelValue == null ? null : modelValue.getFileName();
    }

    @Override
    public void setFileStorageName(@Nullable String fileStorageName) {
        this.fileStorageName = fileStorageName;
    }

    @Nullable
    @Override
    public String getFileStorageName() {
        return fileStorageName;
    }

    @Override
    public FileStoragePutMode getMode() {
        return mode;
    }

    @Override
    public void setMode(FileStoragePutMode mode) {
        this.mode = mode;
    }
}
