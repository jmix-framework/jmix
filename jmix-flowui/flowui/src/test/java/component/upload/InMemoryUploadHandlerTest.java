/*
 * Copyright 2026 Haulmont.
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

package component.upload;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.streams.TransferContext;
import com.vaadin.flow.server.streams.TransferProgressListener;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadRejectedException;
import io.jmix.flowui.backgroundtask.ThreadLocalVaadinRequestHolder;
import io.jmix.flowui.component.upload.handler.InMemoryUploadHandler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InMemoryUploadHandlerTest {

    @Test
    void testUploadRequestIsAvailableInSuccessCallbackAndClearedAfter() throws IOException {
        InMemoryUploadHandler handler = new InMemoryUploadHandler();

        VaadinRequest request = mock(VaadinRequest.class);
        AtomicReference<VaadinRequest> requestDuringCallback = new AtomicReference<>();
        AtomicReference<byte[]> dataDuringCallback = new AtomicReference<>();
        handler.setUploadSuccessHandler(context -> {
            requestDuringCallback.set(ThreadLocalVaadinRequestHolder.getRequest());
            dataDuringCallback.set(context.data());
        });

        List<Command> deferredCommands = new ArrayList<>();
        UI ui = mock(UI.class);
        when(ui.access(any())).thenAnswer(invocation -> {
            deferredCommands.add(invocation.getArgument(0));
            return null;
        });

        handler.handleUploadRequest(uploadEvent("a.txt", "AAA", ui, request));
        deferredCommands.forEach(Command::execute);

        // The upload request must be available while the success callback runs...
        assertSame(request, requestDuringCallback.get());
        // ...and cleared afterwards, so it does not leak to the pooled thread.
        assertNull(ThreadLocalVaadinRequestHolder.getRequest());
        assertArrayEquals("AAA".getBytes(StandardCharsets.UTF_8), dataDuringCallback.get());
    }

    @Test
    void testMetadataRejectionSkipsSuccessCallback() throws IOException {
        InMemoryUploadHandler handler = new InMemoryUploadHandler();
        handler.validateMetadata(event -> event.reject("Too large"));

        AtomicReference<byte[]> delivered = new AtomicReference<>();
        handler.setUploadSuccessHandler(context -> delivered.set(context.data()));
        List<IOException> errors = new ArrayList<>();
        handler.addTransferProgressListener(errorRecorder(errors));

        handler.handleUploadRequest(uploadEvent("a.txt", "AAA", immediateUi(), mock(VaadinRequest.class)));

        assertNull(delivered.get());
        assertEquals(1, errors.size());
        assertInstanceOf(UploadRejectedException.class, errors.get(0));
        assertEquals("Too large", errors.get(0).getMessage());
    }

    @Test
    void testHeaderValidatorSeesLeadingBytesAndWholeContentIsDelivered() throws IOException {
        InMemoryUploadHandler handler = new InMemoryUploadHandler();
        AtomicReference<String> header = new AtomicReference<>();
        handler.validateHeader(2, (event, buffer) -> {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            header.set(new String(bytes, StandardCharsets.UTF_8));
        });

        AtomicReference<byte[]> delivered = new AtomicReference<>();
        handler.setUploadSuccessHandler(context -> delivered.set(context.data()));

        handler.handleUploadRequest(uploadEvent("a.txt", "ABCD", immediateUi(), mock(VaadinRequest.class)));

        assertEquals("AB", header.get());
        assertArrayEquals("ABCD".getBytes(StandardCharsets.UTF_8), delivered.get());
    }

    @Test
    void testCompleteRejectionSkipsSuccessCallback() throws IOException {
        InMemoryUploadHandler handler = new InMemoryUploadHandler();
        AtomicReference<String> validatedContent = new AtomicReference<>();
        handler.validateComplete((event, content) -> {
            try (InputStream inputStream = content.getInputStream()) {
                validatedContent.set(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            }
            event.reject("Infected");
        });

        AtomicReference<byte[]> delivered = new AtomicReference<>();
        handler.setUploadSuccessHandler(context -> delivered.set(context.data()));

        handler.handleUploadRequest(uploadEvent("a.txt", "AAA", immediateUi(), mock(VaadinRequest.class)));

        assertEquals("AAA", validatedContent.get());
        assertNull(delivered.get());
    }

    @Test
    void testProgressListenersRunInUiOfUploadEvent() throws IOException {
        InMemoryUploadHandler handler = new InMemoryUploadHandler();
        List<String> startedFiles = new ArrayList<>();
        handler.addTransferProgressListener(new TransferProgressListener() {
            @Override
            public void onStart(TransferContext context) {
                startedFiles.add(context.fileName());
            }
        });

        // The owning element of the upload is not attached, so only the event knows the UI
        handler.handleUploadRequest(uploadEvent("a.txt", "AAA", immediateUi(), mock(VaadinRequest.class)));

        assertEquals(List.of("a.txt"), startedFiles);
    }

    private TransferProgressListener errorRecorder(List<IOException> errors) {
        return new TransferProgressListener() {
            @Override
            public void onError(TransferContext context, IOException reason) {
                errors.add(reason);
            }
        };
    }

    private UI immediateUi() {
        UI ui = mock(UI.class);
        when(ui.access(any())).thenAnswer(invocation -> {
            invocation.<Command>getArgument(0).execute();
            return null;
        });
        return ui;
    }

    private UploadEvent uploadEvent(String fileName, String content, UI ui, VaadinRequest request) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        UploadEvent event = mock(UploadEvent.class);
        when(event.getFileName()).thenReturn(fileName);
        when(event.getContentType()).thenReturn("text/plain");
        when(event.getFileSize()).thenReturn((long) bytes.length);
        when(event.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        when(event.getUI()).thenReturn(ui);
        when(event.getRequest()).thenReturn(request);

        AtomicReference<String> rejectionMessage = new AtomicReference<>();
        doAnswer(invocation -> {
            rejectionMessage.set(invocation.getArgument(0));
            return null;
        }).when(event).reject(anyString());
        when(event.isRejected()).thenAnswer(invocation -> rejectionMessage.get() != null);
        when(event.getRejectionMessage()).thenAnswer(invocation -> rejectionMessage.get());
        return event;
    }
}
