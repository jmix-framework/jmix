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

import com.vaadin.flow.server.streams.*;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.streams.TransferProgressNotifier;
import io.jmix.flowui.kit.component.upload.handler.SupportUploadSuccessHandler;
import io.jmix.flowui.upload.TemporaryStorage;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * Upload handler for storing upload stream into a temporary storage.
 */
@Component("flowui_FileTemporaryStorageUploadHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FileTemporaryStorageUploadHandler
        extends TransferProgressAwareHandler<UploadEvent, FileTemporaryStorageUploadHandler>
        implements UploadHandler, TransferProgressNotifier, SupportUploadSuccessHandler<TemporaryStorage.FileInfo> {

    protected final TemporaryStorage temporaryStorage;

    protected UploadSuccessHandler<TemporaryStorage.FileInfo> successCallback;
    protected TemporaryStorage.FileInfo fileInfo;

    public FileTemporaryStorageUploadHandler(TemporaryStorage temporaryStorage) {
        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public void handleUploadRequest(UploadEvent event) throws IOException {
        // CAUTION: copied from com.vaadin.flow.server.streams.AbstractFileUploadHandler [last update Vaadin 24.9.0]
        UploadMetadata metadata = new UploadMetadata(event.getFileName(),
                event.getContentType(), event.getFileSize());
        File file;
        try {
            file = createFile(metadata);
            try (InputStream inputStream = event.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(
                         file)) {
                TransferUtil.transfer(inputStream, outputStream,
                        getTransferContext(event), getListeners());
            }
        } catch (IOException e) {
            notifyError(event, e);
            throw e;
        }
        event.getUI().access(() -> {
            try {
                if (successCallback != null) {
                    successCallback.complete(new UploadSuccessContext<>(metadata, fileInfo));
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

    protected File createFile(UploadMetadata metadata) {
        fileInfo = temporaryStorage.createFile();
        return fileInfo.getFile();
    }

    @Override
    protected TransferContext getTransferContext(UploadEvent transferEvent) {
        // CAUTION: copied from com.vaadin.flow.server.streams.AbstractFileUploadHandler [last update Vaadin 24.9.0]
        return new TransferContext(transferEvent.getRequest(),
                transferEvent.getResponse(), transferEvent.getSession(),
                transferEvent.getFileName(), transferEvent.getOwningElement(),
                transferEvent.getFileSize());
    }
}
