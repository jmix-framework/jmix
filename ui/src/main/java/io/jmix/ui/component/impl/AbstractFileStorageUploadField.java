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

import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.FileStorageUploadField.FileStoragePutMode;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.upload.TemporaryStorage;
import io.jmix.ui.widget.JmixFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.*;
import java.util.UUID;

public abstract class AbstractFileStorageUploadField<T> extends AbstractSingleFileUploadField<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractFileStorageUploadField.class);

    protected TemporaryStorage temporaryStorage;
    protected FileStorageLocator fileStorageLocator;
    protected FileStorage fileStorage;
    protected String fileStorageName;

    protected FileStoragePutMode mode = FileStoragePutMode.IMMEDIATE;

    protected UUID fileId;
    protected UUID tempFileId;

    /*
     * This flag is used only for MANUAL mode to register that file was uploaded with the upload button rather then
     * setValue calling or changed property in the datasource.
     */
    protected boolean internalValueChangedOnUpload = false;

    @Override
    protected void valueBindingConnected(ValueSource<T> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            MetaPropertyPath metaPropertyPath = ((EntityValueSource) valueSource).getMetaPropertyPath();
            if (metaPropertyPath.getRange().isDatatype()) {
                Datatype datatype = metaPropertyPath.getRange().asDatatype();
                if (!FileRef.class.isAssignableFrom(datatype.getJavaClass())) {
                    throw new IllegalArgumentException("FileStorageUploadField doesn't support Datatype with class: " + datatype.getJavaClass());
                }
            } else {
                throw new IllegalArgumentException("FileStorageUploadField doesn't support properties with association");
            }
        }

        setShowFileName(true);
    }

    @Autowired
    public void setTemporaryStorage(TemporaryStorage temporaryStorage) {
        this.temporaryStorage = temporaryStorage;
    }

    @Autowired
    public void setFileStorageLocator(FileStorageLocator fileStorageLocator) {
        this.fileStorageLocator = fileStorageLocator;
    }

    @Override
    protected OutputStream receiveUpload(String fileName, String MIMEType) {
        try {
            TemporaryStorage.FileInfo fileInfo = temporaryStorage.createFile();
            tempFileId = fileInfo.getId();
            File tmpFile = fileInfo.getFile();

            return new FileOutputStream(tmpFile);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to receive file '%s' of MIME type: %s", fileName, MIMEType), e);
        }
    }

    protected void internalValueChanged(@Nullable Object newValue) {
        fileName = newValue == null ? null : newValue.toString();

        if (!internalValueChangedOnUpload) {
            fileId = null;
            tempFileId = null;
        }
    }

    @Override
    protected void onUploadFailed(JmixFileUpload.FailedEvent event) {
        try {
            temporaryStorage.deleteFile(tempFileId);
            tempFileId = null;
        } catch (Exception e) {
            if (e instanceof FileStorageException) {
                FileStorageException fse = (FileStorageException) e;
                if (fse.getType() != FileStorageException.Type.FILE_NOT_FOUND) {
                    log.warn(String.format("Could not remove temp file %s after broken uploading", tempFileId));
                }
            }
            log.warn(String.format("Error while delete temp file %s", tempFileId));
        }
        super.onUploadFailed(event);
    }

    protected void checkFileStorageInitialized() {
        if (fileStorage == null) {
            if (StringUtils.isNotEmpty(fileStorageName)) {
                fileStorage = fileStorageLocator.getByName(fileStorageName);
            } else {
                fileStorage = fileStorageLocator.getDefault();
            }
        }
    }

    @Nullable
    public InputStream getFileContent(@Nullable FileRef fileRef) {
        checkFileStorageInitialized();
        switch (mode) {
            case MANUAL:
                if (fileId == null) {
                    return fileRef != null ? fileStorage.openStream(fileRef) : null;
                }

                File file = temporaryStorage.getFile(fileId);
                if (file != null) {
                    try {
                        return new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        log.error("Unable to get content of {}", file, e);
                    }
                    return null;
                }

                try {
                    if (fileRef != null && fileStorage.fileExists(fileRef)) {
                        return fileStorage.openStream(fileRef);
                    }
                } catch (io.jmix.core.FileStorageException e) {
                    log.error("Unable to get content of {}", fileRef, e);
                    return null;
                }
                break;
            case IMMEDIATE:
                if (fileRef != null) {
                    return fileStorage.openStream(fileRef);
                }
        }
        return null;
    }

    @Nullable
    @Override
    public String getFileName() {
        return super.getFileName();
    }

    /**
     * @return File id for uploaded file in {@link TemporaryStorage}
     */
    @Nullable
    public UUID getFileId() {
        return fileId;
    }
}
