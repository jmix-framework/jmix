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

package generation;

import io.jmix.aitoolsflowui.generation.AiChatGenerationManager;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationFailedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationStartedEvent;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiChatMessageService;
import test_support.TestAiChatService;
import test_support.TestAiConversationService;
import test_support.TestAuthentication;
import test_support.TestGenerationWorkers;
import test_support.TestUiUserEventCollector;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The response timeout stops a response the same way a cancel does, but reports it as failed. A separate class,
 * since the timeout is read once per application context.
 */
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class},
        properties = "jmix.aitools.ui.assistant-response-timeout=100ms")
class AiChatGenerationTimeoutTest {

    @Autowired AiChatGenerationManager manager;
    @Autowired TestAiChatService chatService;
    @Autowired TestAiConversationService conversationService;
    @Autowired TestAiChatMessageService messageService;
    @Autowired TestUiUserEventCollector events;

    final CountDownLatch release = new CountDownLatch(1);

    @Nullable TestGenerationWorkers ownWorkers;

    @BeforeEach
    void setUp() {
        events.clear();
    }

    @AfterEach
    void tearDown() {
        release.countDown();
        events.awaitAllFinished();
        if (ownWorkers != null) {
            ownWorkers.restore();
        }
        chatService.reset();
        conversationService.clear();
        messageService.clear();
        events.clear();
    }

    @Test
    void timeout_running_publishesFailed_andDiscardsLateAnswer() throws Exception {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        ownWorkers = TestGenerationWorkers.install(manager);
        chatService.setResponder(spec -> {
            TestAiChatService.awaitIgnoringInterrupt(release);
            return "late answer";
        });

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        events.await(AiChatGenerationFailedEvent.class);
        release.countDown();
        ownWorkers.awaitIdle();

        assertEquals(List.of(AiChatGenerationStartedEvent.class, AiChatGenerationFailedEvent.class),
                events.eventsOf(conversation.getId()).stream().map(Object::getClass).toList());
        assertEquals(List.of(AiChatMessageType.USER),
                messageService.loadMessages(conversation).stream().map(AiChatMessage::getType).toList());
        assertFalse(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));
    }

    @Test
    void timeout_whileQueued_publishesFailed_andModelIsNotCalled() throws Exception {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        ownWorkers = TestGenerationWorkers.install(manager);
        // Another response occupies the only worker.
        ownWorkers.execute(() -> TestAiChatService.awaitIgnoringInterrupt(release));
        AtomicBoolean modelCalled = new AtomicBoolean();
        chatService.setResponder(spec -> {
            modelCalled.set(true);
            return "answer";
        });

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        events.await(AiChatGenerationFailedEvent.class);
        release.countDown();
        ownWorkers.awaitIdle();

        assertFalse(modelCalled.get());
        assertEquals(1, messageService.loadMessages(conversation).size());
    }
}
