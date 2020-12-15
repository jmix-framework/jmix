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
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.ui.component.FileStorageUploadField;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.upload.TemporaryStorage;
import io.jmix.ui.widget.JmixFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Objects;
import java.util.UUID;

public class FileStorageUploadFieldImpl<T> extends AbstractSingleFileUploadField<T>
        implements FileStorageUploadField<T> {

    private static final Logger log = LoggerFactory.getLogger(FileUploadFieldImpl.class);

    protected TemporaryStorage temporaryStorage;
    protected FileStorage<T> fileStorage;
    protected String fileStorageName;

    protected FileStoragePutMode mode = FileStoragePutMode.MANUAL;

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

        setShowFileName(true);
    }

    @Autowired
    public void setTemporaryStorage(TemporaryStorage temporaryStorage) {
        this.temporaryStorage = temporaryStorage;
    }

    @Autowired
    public void setFileStorageLocator(FileStorageLocator fileStorageLocator) {
        String fileStorageName = getFileStorageName();
        if (StringUtils.isNotEmpty(fileStorageName)) {
            fileStorage = fileStorageLocator.get(fileStorageName);
        } else {
            fileStorage = fileStorageLocator.getDefault();
        }
        ValueSource<T> valueSource = getValueSource();
        if (valueSource != null && !fileStorage.getReferenceType().isAssignableFrom(valueSource.getType())) {
            throw new IllegalArgumentException(String.format(
                    "File storage %s is not applicable for the property of type %s",
                    fileStorage.getClass().getName(), valueSource.getType().getName()));
        }
    }

    @Override
    protected void onFileNameClick(Button.ClickEvent e) {
        T value = getValue();
        if (value == null) {
            return;
        }

        switch (mode) {
            case MANUAL:
                String name = getFileName();
                String fileName = StringUtils.isEmpty(name) ? getFileNameByValue(value) : name;
                downloader.download(this::getFileContent, fileName);
                break;
            case IMMEDIATE:
                downloader.download(this::getFileContent, getFileNameByValue(value));
                break;
        }
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

    protected void saveFile(T reference) {
        switch (mode) {
            case MANUAL:
                internalValueChangedOnUpload = true;
                setValue(reference);
                internalValueChangedOnUpload = false;
                break;
            case IMMEDIATE:
                temporaryStorage.putFileIntoStorage(fileId, reference, fileStorage);
                setValue(reference);
                break;
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
    protected void onUploadSucceeded(JmixFileUpload.SucceededEvent event) {
        fileName = event.getFileName();
        fileId = tempFileId;

        saveFile(getReference());
        component.setFileNameButtonCaption(fileName);

        super.onUploadSucceeded(event);
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

    @Nullable
    @Override
    protected String convertToPresentation(@Nullable T modelValue) throws ConversionException {
        return modelValue == null ? null : getFileNameByValue(modelValue);
    }

    protected String getFileNameByValue(T value) {
        return fileStorage.getFileName(value);
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
    @Override
    public UUID getFileId() {
        return fileId;
    }

    @Nullable
    @Override
    public T getReference() {
        if (fileId != null) {
            return fileStorage.createReference(fileName);
        } else {
            return null;
        }
    }

    @Override
    public InputStream getFileContent() {
        if (contentProvider != null) {
            return contentProvider.get();
        }

        T reference = getValue();
        switch (mode) {
            case MANUAL:
                if (fileId == null) {
                    return fileStorage.openStream(reference);
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
                    if (fileStorage.fileExists(reference)) {
                        return fileStorage.openStream(reference);
                    }
                } catch (FileStorageException e) {
                    log.error("Unable to get content of {}", reference, e);
                    return null;
                }
                break;
            case IMMEDIATE:
                if (reference != null) {
                    return fileStorage.openStream(reference);
                }
        }
        return null;
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
