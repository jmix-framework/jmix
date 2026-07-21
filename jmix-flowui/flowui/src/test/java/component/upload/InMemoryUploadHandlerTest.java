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
import io.jmix.flowui.component.upload.handler.InMemoryUploadHandler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
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
