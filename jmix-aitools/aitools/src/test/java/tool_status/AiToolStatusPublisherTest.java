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

package tool_status;

import io.jmix.aitools.tool.AiToolStatusUpdateEvent;
import io.jmix.aitools.tool.AiToolStatusPublisher;
import io.jmix.aitools.tool.AiToolStatusUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class AiToolStatusPublisherTest {

    final List<Object> published = new ArrayList<>();
    AiToolStatusPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new AiToolStatusPublisher();
        ReflectionTestUtils.setField(publisher, "eventPublisher", (ApplicationEventPublisher) published::add);
    }

    @Test
    void update_withCorrelationId_publishesEvent() {
        publisher.update("Searching customers", context(Map.of(AiToolStatusPublisher.CORRELATION_ID, "gen-1")));

        assertEquals(1, published.size());
        AiToolStatusUpdateEvent event = (AiToolStatusUpdateEvent) published.get(0);
        assertEquals("gen-1", event.getCorrelationId());
        assertEquals("Searching customers", event.getUpdate().getMessage());
        assertFalse(event.getUpdate().isCompleted());
    }

    @Test
    void complete_withCorrelationId_publishesCompletedUpdate() {
        publisher.complete("Searching customers", "3 found",
                context(Map.of(AiToolStatusPublisher.CORRELATION_ID, "gen-1")));

        AiToolStatusUpdateEvent event = (AiToolStatusUpdateEvent) published.get(0);
        assertEquals("3 found", event.getUpdate().getResultSnippet());
        assertTrue(event.getUpdate().isCompleted());
    }

    @Test
    void update_withoutCorrelationId_publishesNothing() {
        publisher.update("Searching customers",
                context(Map.of(AiToolStatusPublisher.DEFAULT_TOOL_CONTEXT_MARKER, true)));

        assertTrue(published.isEmpty());
    }

    @Test
    void update_blankMessage_publishesNothing() {
        publisher.update(" ", context(Map.of(AiToolStatusPublisher.CORRELATION_ID, "gen-1")));

        assertTrue(published.isEmpty());
    }

    @Test
    void update_nullContext_publishesNothing() {
        publisher.update("Searching customers", null);

        assertTrue(published.isEmpty());
    }

    @Test
    void update_deprecatedCallback_stillInvoked() {
        List<AiToolStatusUpdate> received = new ArrayList<>();
        Consumer<AiToolStatusUpdate> callback = received::add;

        publisher.update("Searching customers",
                context(Map.of(AiToolStatusPublisher.STATUS_UPDATE_CALLBACK, callback)));

        assertEquals(1, received.size());
        assertTrue(published.isEmpty());
    }

    @Test
    void publish_listenerThrows_toolNotBrokenAndCallbackStillInvoked() {
        ReflectionTestUtils.setField(publisher, "eventPublisher", (ApplicationEventPublisher) event -> {
            throw new IllegalStateException("listener failed");
        });
        List<AiToolStatusUpdate> received = new ArrayList<>();
        Consumer<AiToolStatusUpdate> callback = received::add;

        assertDoesNotThrow(() -> publisher.update("Searching customers", context(Map.of(
                AiToolStatusPublisher.CORRELATION_ID, "gen-1",
                AiToolStatusPublisher.STATUS_UPDATE_CALLBACK, callback))));

        assertEquals(1, received.size());
    }

    @Test
    void publish_callbackThrows_toolNotBroken() {
        Consumer<AiToolStatusUpdate> callback = update -> {
            throw new IllegalStateException("callback failed");
        };

        assertDoesNotThrow(() -> publisher.update("Searching customers", context(Map.of(
                AiToolStatusPublisher.CORRELATION_ID, "gen-1",
                AiToolStatusPublisher.STATUS_UPDATE_CALLBACK, callback))));

        assertEquals(1, published.size());
    }

    private ToolContext context(Map<String, Object> entries) {
        return new ToolContext(entries);
    }
}
