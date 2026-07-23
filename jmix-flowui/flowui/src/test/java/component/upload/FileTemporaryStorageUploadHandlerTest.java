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
import com.vaadin.flow.server.streams.UploadEvent;
import io.jmix.flowui.backgroundtask.ThreadLocalVaadinRequestHolder;
import io.jmix.flowui.component.upload.handler.FileTemporaryStorageUploadHandler;
import io.jmix.flowui.upload.TemporaryStorage;
import io.jmix.flowui.upload.TemporaryStorage.FileInfo;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileTemporaryStorageUploadHandlerTest {

    @Test
    void testEachUploadedFileReceivesItsOwnFileInfo() throws IOException {
        TemporaryStorage temporaryStorage = mock(TemporaryStorage.class);

        File fileA = Files.createTempFile("upload-a", ".tmp").toFile();
        File fileB = Files.createTempFile("upload-b", ".tmp").toFile();
        fileA.deleteOnExit();
        fileB.deleteOnExit();

        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        when(temporaryStorage.createFile())
                .thenReturn(new FileInfo(fileA, idA), new FileInfo(fileB, idB));

        FileTemporaryStorageUploadHandler handler = new FileTemporaryStorageUploadHandler(temporaryStorage);

        Map<String, UUID> receivedIdsByFileName = new LinkedHashMap<>();
        handler.setUploadSuccessHandler(context ->
                receivedIdsByFileName.put(context.uploadMetadata().fileName(), context.data().getId()));

        // UI.access() defers the success callback to the UI queue, as it does at runtime.
        List<Command> deferredCommands = new ArrayList<>();
        UI ui = mock(UI.class);
        when(ui.access(any())).thenAnswer(invocation -> {
            deferredCommands.add(invocation.getArgument(0));
            return null;
        });

        handler.handleUploadRequest(uploadEvent("a.txt", "AAA", ui));
        handler.handleUploadRequest(uploadEvent("b.txt", "BBBB", ui));

        // Run the queued UI callbacks after both uploads have been handled.
        deferredCommands.forEach(Command::execute);

        assertEquals(idA, receivedIdsByFileName.get("a.txt"));
        assertEquals(idB, receivedIdsByFileName.get("b.txt"));
    }

    @Test
    void testUploadRequestIsAvailableInSuccessCallbackAndClearedAfter() throws IOException {
        TemporaryStorage temporaryStorage = mock(TemporaryStorage.class);

        File file = Files.createTempFile("upload", ".tmp").toFile();
        file.deleteOnExit();
        when(temporaryStorage.createFile()).thenReturn(new FileInfo(file, UUID.randomUUID()));

        FileTemporaryStorageUploadHandler handler = new FileTemporaryStorageUploadHandler(temporaryStorage);

        VaadinRequest request = mock(VaadinRequest.class);
        AtomicReference<VaadinRequest> requestDuringCallback = new AtomicReference<>();
        handler.setUploadSuccessHandler(context ->
                requestDuringCallback.set(ThreadLocalVaadinRequestHolder.getRequest()));

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
    }

    private UploadEvent uploadEvent(String fileName, String content, UI ui) {
        return uploadEvent(fileName, content, ui, mock(VaadinRequest.class));
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
        return event;
    }
}
