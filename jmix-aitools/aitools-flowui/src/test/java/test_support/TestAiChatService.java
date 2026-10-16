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

package test_support;

import io.jmix.aitools.tool.AiToolStatusUpdateEvent;
import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.service.AiChatResponseSpec;
import io.jmix.aitoolsflowui.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * {@link AiChatService} for UI tests: reports the chat as available, so the views render as they do in a
 * configured application, and echoes any message instead of calling a model. Tests can replace the answer and
 * publish tool statuses the way a tool does during a call.
 */
public class TestAiChatService implements AiChatService {

    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    protected volatile Function<AiChatResponseSpec, String> responder = TestAiChatService::echo;

    /**
     * Sets how the next calls answer.
     *
     * @param responder receives the spec of the call, returns the answer
     */
    public void setResponder(Function<AiChatResponseSpec, String> responder) {
        this.responder = responder;
    }

    /**
     * Publishes a tool status for the given call, as a tool would.
     *
     * @param correlationId correlation id of the call the status belongs to
     * @param update        the status
     */
    public void publishStatus(String correlationId, AiToolStatusUpdate update) {
        eventPublisher.publishEvent(new AiToolStatusUpdateEvent(this, correlationId, update));
    }

    public void reset() {
        responder = TestAiChatService::echo;
    }

    /**
     * Blocks like an HTTP call that does not react to the interruption: waits for the latch, keeping the interrupt
     * for the caller to see afterwards.
     *
     * @param release latch that ends the call; the wait gives up after 5 seconds
     */
    public static void awaitIgnoringInterrupt(CountDownLatch release) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    release.await(5, TimeUnit.SECONDS);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public String generateResponse(AiChatResponseSpec spec) {
        return responder.apply(spec);
    }

    @Deprecated(since = "3.2", forRemoval = true)
    @Override
    public String processMessage(AiChatMessage message) {
        return message.getContent();
    }

    @Deprecated(since = "3.2", forRemoval = true)
    @Override
    public String processMessage(AiChatMessage message, Consumer<AiToolStatusUpdate> statusCallback) {
        return message.getContent();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private static String echo(AiChatResponseSpec spec) {
        return spec.getUserMessage().getContent();
    }
}
