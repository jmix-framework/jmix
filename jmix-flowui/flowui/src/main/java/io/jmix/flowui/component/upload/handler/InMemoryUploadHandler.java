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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@Component("flowui_InMemoryUploadHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InMemoryUploadHandler
        extends TransferProgressAwareHandler<UploadEvent, InMemoryUploadHandler>
        implements UploadHandler, TransferProgressNotifier, SupportUploadSuccessCallback {

    protected UploadSuccessCallback successCallback;

    public InMemoryUploadHandler() {
    }

    @Override
    public void handleUploadRequest(UploadEvent event) throws IOException {
        // CAUTION: copied from com.vaadin.flow.server.streams.InMemoryUploadHandler [last update Vaadin 24.9.0]
        byte[] data;
        try {
            try (InputStream inputStream = event.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                TransferUtil.transfer(inputStream, outputStream,
                        getTransferContext(event), getListeners());
                data = outputStream.toByteArray();
            }
        } catch (IOException e) {
            notifyError(event, e);
            throw e;
        }
        event.getUI().access(() -> {
            try {
                if (successCallback != null) {
                    successCallback.complete(new UploadContext(
                            new UploadMetadata(event.getFileName(), event.getContentType(), event.getFileSize())));
                }

            } catch (IOException e) {
                throw new UncheckedIOException(
                        "Error in memory upload callback", e);
            }
        });
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
    protected TransferContext getTransferContext(UploadEvent transferEvent) {
        // CAUTION: copied from com.vaadin.flow.server.streams.InMemoryUploadHandler [last update Vaadin 24.9.0]
        return new TransferContext(transferEvent.getRequest(),
                transferEvent.getResponse(), transferEvent.getSession(),
                transferEvent.getFileName(), transferEvent.getOwningElement(),
                transferEvent.getFileSize());
    }
}
