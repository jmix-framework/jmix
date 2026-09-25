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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Upload handler for storing the upload in-memory. Data is returned as a
 * {@code byte[]} to the given successHandler.
 */
@Component("flowui_InMemoryUploadHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InMemoryUploadHandler
        extends AbstractUploadHandler<InMemoryUploadHandler>
        implements TransferProgressNotifier, SupportUploadSuccessHandler<byte[]> {

    protected UploadSuccessHandler<byte[]> successHandler;

    public InMemoryUploadHandler() {
    }

    @Override
    public void handleUploadRequest(UploadEvent event) throws IOException {
        // CAUTION: copied from com.vaadin.flow.server.streams.InMemoryUploadHandler [last update Vaadin 25.3.0]
        setTransferUI(event.getUI());
        byte[] data = null;
        try {
            data = readContent(event);
        } catch (IOException e) {
            notifyError(event, e);
            throw e;
        }
        if (hasValidators() && data != null && !event.isRejected()) {
            // Complete phase runs after the transfer's onComplete has already
            // fired, so any failure here is reported via onError.
            try {
                runCompleteValidators(event, new ByteArrayUploadContent(data));
            } catch (IOException e) {
                notifyError(event, e);
                throw e;
            } catch (RuntimeException e) {
                notifyError(event, new IOException(e));
                throw e;
            }
        }
        // A validator may reject the upload during any phase (metadata, header
        // or complete); all of them converge here. The transfer's own onComplete
        // may already have fired, so the rejection is surfaced as a terminal
        // onError, and the accumulated data is never delivered.
        if (event.isRejected()) {
            notifyError(event,
                    new UploadRejectedException(event.getRejectionMessage()));
            return;
        }
        final byte[] delivered = data;
        // The success callback runs via UI.access() from the upload handler thread, without an active
        // VaadinServletRequest. Provide the upload request through the thread-local holder so that opening
        // a view-based dialog or window from the success handler can perform the view access check, which
        // requires a request. See UiAccessChecker#isViewPermitted.
        VaadinRequest request = event.getRequest();
        event.getUI().access(() -> {
            try {
                if (successHandler != null) {
                    ThreadLocalVaadinRequestHolder.setRequest(request);
                    try {
                        successHandler.complete(new UploadSuccessContext<>(
                                new UploadMetadata(event.getFileName(), event.getContentType(), event.getFileSize()),
                                delivered));
                    } finally {
                        ThreadLocalVaadinRequestHolder.clear();
                    }
                }

            } catch (IOException e) {
                throw new UncheckedIOException(
                        "Error in memory upload callback", e);
            }
        });
    }

    /**
     * Runs the metadata and header validators and reads the whole upload into memory.
     *
     * @param event the upload being handled
     * @return the uploaded data, or {@code null} if a validator rejected the upload before it was read
     * @throws IOException if reading the upload or a validator fails
     */
    protected byte @Nullable [] readContent(UploadEvent event) throws IOException {
        // CAUTION: copied from com.vaadin.flow.server.streams.InMemoryUploadHandler [last update Vaadin 25.3.0]
        runMetadataValidators(event);
        if (event.isRejected()) {
            return null;
        }
        try (InputStream raw = event.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            InputStream in = applyHeaderValidators(event, raw);
            if (event.isRejected()) {
                return null;
            }
            TransferUtil.transfer(in, outputStream, getTransferContext(event),
                    getListeners());
            return outputStream.toByteArray();
        }
    }

    @Override
    public Registration addTransferProgressListener(TransferProgressListener listener) {
        return super.addTransferProgressListener(listener);
    }

    @Override
    public void setUploadSuccessHandler(@Nullable UploadSuccessHandler<byte[]> handler) {
        this.successHandler = handler;
    }
}
