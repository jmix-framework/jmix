/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.component.upload.handler;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.communication.TransferUtil;
import com.vaadin.flow.server.streams.*;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.backgroundtask.ThreadLocalVaadinRequestHolder;
import io.jmix.flowui.kit.component.streams.TransferProgressNotifier;
import io.jmix.flowui.kit.component.upload.handler.SupportUploadSuccessHandler;
import io.jmix.flowui.upload.TemporaryStorage;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * Upload handler for storing upload stream into a temporary storage.
 */
@Component("flowui_FileTemporaryStorageUploadHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FileTemporaryStorageUploadHandler
        extends AbstractUploadHandler<FileTemporaryStorageUploadHandler>
        implements TransferProgressNotifier, SupportUploadSuccessHandler<TemporaryStorage.FileInfo> {

    private static final Logger log = LoggerFactory.getLogger(FileTemporaryStorageUploadHandler.class);

    protected final TemporaryStorage temporaryStorage;

    protected UploadSuccessHandler<TemporaryStorage.FileInfo> successCallback;
    protected TemporaryStorage.FileInfo fileInfo;

    public FileTemporaryStorageUploadHandler(TemporaryStorage temporaryStorage) {
        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public void handleUploadRequest(UploadEvent event) throws IOException {
        // CAUTION: copied from com.vaadin.flow.server.streams.AbstractFileUploadHandler [last update Vaadin 25.3.0]
        UploadMetadata metadata = new UploadMetadata(event.getFileName(),
                event.getContentType(), event.getFileSize());
        setTransferUI(event.getUI());
        // Upload fields delete the file returned by getFileInfo() when an upload fails,
        // so it must not point to the file of a previous upload.
        fileInfo = null;
        TemporaryStorage.FileInfo uploadedFileInfo = null;
        try {
            runMetadataValidators(event);
            if (!event.isRejected()) {
                try (InputStream raw = event.getInputStream()) {
                    InputStream in = applyHeaderValidators(event, raw);
                    if (!event.isRejected()) {
                        // Create the file only once metadata and header passed,
                        // so a rejection leaves no file behind.
                        uploadedFileInfo = createFile(metadata);
                        try (FileOutputStream outputStream = new FileOutputStream(
                                uploadedFileInfo.getFile())) {
                            TransferUtil.transfer(in, outputStream,
                                    getTransferContext(event), getListeners());
                        }
                    }
                }
            }
        } catch (IOException e) {
            deleteAndNotify(event, uploadedFileInfo, e);
            throw e;
        } catch (RuntimeException e) {
            // A validator may throw an unchecked exception; still clean up.
            deleteQuietly(uploadedFileInfo);
            throw e;
        }
        if (hasValidators() && uploadedFileInfo != null && !event.isRejected()) {
            // Whole-content validation. Its streams are closed before any deletion.
            // This runs after the transfer's onComplete has already fired, so any
            // failure here is reported via onError.
            try (FileUploadContent content = new FileUploadContent(uploadedFileInfo.getFile())) {
                runCompleteValidators(event, content);
            } catch (IOException e) {
                deleteAndNotify(event, uploadedFileInfo, e);
                throw e;
            } catch (RuntimeException e) {
                deleteQuietly(uploadedFileInfo);
                notifyError(event, new IOException(e));
                throw e;
            }
        }
        // A validator may reject the upload during any phase (metadata, header
        // or complete); all of them converge here. The transfer's own onComplete
        // may already have fired, so the rejection is surfaced as a terminal
        // onError, and the written file is discarded rather than delivered.
        if (event.isRejected()) {
            notifyError(event,
                    new UploadRejectedException(event.getRejectionMessage()));
            deleteQuietly(uploadedFileInfo);
            return;
        }
        final TemporaryStorage.FileInfo deliveredFileInfo = uploadedFileInfo;
        // The success callback runs via UI.access() from the upload handler thread, without an active
        // VaadinServletRequest. Provide the upload request through the thread-local holder so that opening
        // a view-based dialog or window from the success handler can perform the view access check, which
        // requires a request. See UiAccessChecker#isViewPermitted.
        VaadinRequest request = event.getRequest();
        event.getUI().access(() -> {
            try {
                if (successCallback != null) {
                    ThreadLocalVaadinRequestHolder.setRequest(request);
                    try {
                        successCallback.complete(new UploadSuccessContext<>(metadata, deliveredFileInfo));
                    } finally {
                        ThreadLocalVaadinRequestHolder.clear();
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Error in file upload callback", e);
            }
        });
    }

    @Override
    public Registration addTransferProgressListener(TransferProgressListener listener) {
        return super.addTransferProgressListener(listener);
    }

    @Override
    public void setUploadSuccessHandler(@Nullable UploadSuccessHandler<TemporaryStorage.FileInfo> handler) {
        this.successCallback = handler;
    }

    public TemporaryStorage.FileInfo getFileInfo() {
        return fileInfo;
    }

    protected TemporaryStorage.FileInfo createFile(UploadMetadata metadata) {
        TemporaryStorage.FileInfo created = temporaryStorage.createFile();
        fileInfo = created;
        return created;
    }

    protected void deleteAndNotify(UploadEvent event, TemporaryStorage.@Nullable FileInfo fileInfo, IOException e) {
        deleteQuietly(fileInfo);
        notifyError(event, e);
    }

    protected void deleteQuietly(TemporaryStorage.@Nullable FileInfo fileInfo) {
        if (fileInfo == null) {
            return;
        }
        try {
            temporaryStorage.deleteFile(fileInfo.getId());
        } catch (RuntimeException e) {
            log.warn("Could not delete the temporary file {} of a rejected or failed upload", fileInfo.getId(), e);
        }
    }
}
