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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.core.app.CubaFileStorage;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.FileStorageUploadField;
import io.jmix.ui.component.UploadField;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.impl.AbstractFileStorageUploadField;
import io.jmix.ui.upload.TemporaryStorage;
import io.jmix.ui.widget.JmixFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.function.Consumer;

@Deprecated
public class WebFileUploadField extends AbstractFileStorageUploadField<FileDescriptor>
        implements FileUploadField {

    private static final Logger log = LoggerFactory.getLogger(WebFileUploadField.class);

    @Autowired
    protected FileUploadingAPI fileUploading;

    @Autowired
    protected CubaFileStorage cubaFileStorage;

    @Autowired
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void setFileStorageLocator(FileStorageLocator fileStorageLocator) {
        //ignore FileStorageLocator and use cuba file storage
        fileStorage = cubaFileStorage.getDelegate();
    }

    @Override
    protected void initComponent() {
        super.initComponent();
        applyPermissions();
    }

    @Override
    protected void onUploadSucceeded(JmixFileUpload.SucceededEvent event) {
        fileName = event.getFileName();
        fileId = tempFileId;

        saveFile(getFileDescriptor());
        component.setFileNameButtonCaption(fileName);

        super.onUploadSucceeded(event);
    }

    @Override
    protected void onFileNameClick(Button.ClickEvent e) {
        FileDescriptor value = getValue();
        if (value == null) {
            return;
        }

        switch (mode) {
            case MANUAL:
                String name = getFileName();
                String fileName = StringUtils.isEmpty(name) ? value.getName() : name;
                downloader.download(this::getFileContent, fileName);
                break;
            case IMMEDIATE:
                downloader.download(this::getFileContent, value.getName());
                break;
        }
    }

    protected void saveFile(FileDescriptor fileDescriptor) {
        switch (mode) {
            case MANUAL:
                internalValueChangedOnUpload = true;
                setValue(fileDescriptor);
                internalValueChangedOnUpload = false;
                break;
            case IMMEDIATE:
                try {
                    fileUploading.putFileIntoStorage(fileId, fileDescriptor);
                    FileDescriptor committedDescriptor = commitFileDescriptor(fileDescriptor);
                    setValue(committedDescriptor);
                } catch (FileStorageException e) {
                    log.error("Error has occurred during file saving", e);
                }
                break;
        }
    }

    @Nullable
    @Override
    protected String convertToPresentation(@Nullable FileDescriptor modelValue) throws ConversionException {
        return modelValue == null ? null : modelValue.getName();
    }

    @Nullable
    @Override
    public InputStream getFileContent() {
        if (contentProvider != null) {
            return contentProvider.get();
        }

        FileRef fileRef = cubaFileStorage.toFileRef(getValue());
        return getFileContent(fileRef);
    }

    protected void applyPermissions() {
        Security security = (Security) applicationContext.getBean(Security.NAME);

        if (!security.isEntityOpPermitted(FileDescriptor.class, EntityOp.UPDATE)) {
            component.setUploadButtonEnabled(false);
            component.setClearButtonEnabled(false);
        }
        if (!security.isEntityOpPermitted(FileDescriptor.class, EntityOp.READ)) {
            component.setFileNameButtonEnabled(false);
        }
    }

    protected FileDescriptor commitFileDescriptor(FileDescriptor fileDescriptor) {
        DataSupplier dataSupplier = getDataSupplier();
        if (dataSupplier != null) {
            return dataSupplier.commit(fileDescriptor);
        }

        DataManager dataManager = (DataManager) applicationContext.getBean(DataManager.NAME);
        return dataManager.commit(fileDescriptor);
    }

    protected DataSupplier getDataSupplier() {
        if (getDatasource() != null) {
            return getDatasource().getDataSupplier();
        }
        Window window = ComponentsHelper.getWindowNN(this);
        if (window.getFrameOwner() instanceof LegacyFrame) {
            DsContext dsContext = ((LegacyFrame) window.getFrameOwner()).getDsContext();
            if (dsContext != null && dsContext.getDataSupplier() != null) {
                return dsContext.getDataSupplier();
            }
        }

        return null;
    }

    @Nullable
    @Override
    public FileDescriptor getFileDescriptor() {
        if (fileId != null) {
            return fileUploading.getFileDescriptor(fileId, fileName);
        } else {
            return null;
        }
    }

    /**
     * Get content bytes for uploaded file
     *
     * @return Bytes for uploaded file
     * @deprecated Please use {@link AbstractFileStorageUploadField#getFileId()} method
     * and {@link TemporaryStorage}
     */
    @Override
    @Deprecated
    public byte[] getBytes() {
        byte[] bytes = null;
        try {
            if (fileId != null) {
                File file = fileUploading.getFile(fileId);
                FileInputStream fileInputStream = new FileInputStream(file);
                ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
                IOUtils.copy(fileInputStream, byteOutput);
                bytes = byteOutput.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to get file content", e);
        }

        return bytes;
    }

    @Override
    public FileStorageUploadField.FileStoragePutMode getMode() {
        return mode;
    }

    @Override
    public void setMode(FileStorageUploadField.FileStoragePutMode mode) {
        this.mode = mode;
    }

    @Override
    public void removeFileUploadSucceedListener(Consumer<FileUploadSucceedEvent> listener) {
        unsubscribe(FileUploadSucceedEvent.class, listener);
    }

    @Override
    public void removeBeforeValueClearListener(Consumer<BeforeValueClearEvent> listener) {
        unsubscribe(BeforeValueClearEvent.class, listener);
    }

    @Override
    public void removeAfterValueClearListener(Consumer<AfterValueClearEvent> listener) {
        unsubscribe(AfterValueClearEvent.class, listener);
    }

    @Override
    public void removeFileUploadStartListener(Consumer<FileUploadStartEvent> listener) {
        unsubscribe(FileUploadStartEvent.class, listener);
    }

    @Override
    public void removeFileUploadFinishListener(Consumer<FileUploadFinishEvent> listener) {
        unsubscribe(FileUploadFinishEvent.class, listener);
    }

    @Override
    public void removeFileUploadErrorListener(Consumer<FileUploadErrorEvent> listener) {
        unsubscribe(FileUploadErrorEvent.class, listener);
    }

    @Override
    public void addValidator(Consumer<? super FileDescriptor> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<FileDescriptor> validator) {
        removeValidator(validator::accept);
    }

    @Override
    public void setDropZone(@Nullable UploadField.DropZone dropZone) {
        this.dropZone = dropZone;

        if (dropZone == null) {
            component.setDropZone(null);
        } else {
            io.jmix.ui.component.Component target = dropZone.getTarget();
            if (target instanceof com.haulmont.cuba.gui.components.Window.Wrapper) {
                target = ((com.haulmont.cuba.gui.components.Window.Wrapper) target).getWrappedWindow();
            }

            Component vComponent = target.unwrapComposition(Component.class);
            component.setDropZone(vComponent);
        }
    }
}
