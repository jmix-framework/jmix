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
import io.jmix.flowui.component.streams.TransferProgressNotifier;
import io.jmix.flowui.upload.TemporaryStorage;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.function.Consumer;

@Component("flowui_FileTemporaryStorageUploadHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FileTemporaryStorageUploadHandler
        extends TransferProgressAwareHandler<UploadEvent, FileTemporaryStorageUploadHandler>
        implements UploadHandler, TransferProgressNotifier, SupportUploadSuccessCallback {

    protected final TemporaryStorage temporaryStorage;

    protected UploadSuccessCallback successCallback;

    public FileTemporaryStorageUploadHandler(TemporaryStorage temporaryStorage) {
        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public Registration addTransferProgressListener(TransferProgressListener listener) {
        return super.addTransferProgressListener(listener);
    }

    @Override
    public void setUploadSuccessCallback(@Nullable UploadSuccessCallback successCallback) {
        this.successCallback = successCallback;
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
                    successCallback.complete(new UploadContext(metadata));
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Error in file upload callback", e);
            }
        });
    }

    protected File createFile(UploadMetadata metadata) {
        TemporaryStorage.FileInfo fileInfo = temporaryStorage.createFile();
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
