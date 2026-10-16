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

import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.generation.AiChatGeneration;
import io.jmix.aitoolsflowui.generation.AiChatGenerationManager;
import io.jmix.aitoolsflowui.generation.AiChatGenerationRejectedException;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCompletedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationFailedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationProgressEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationStartedEvent;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiChatMessageService;
import test_support.TestAiChatService;
import test_support.TestAiConversationService;
import test_support.TestAuthentication;
import test_support.TestUiUserEventCollector;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Background generation of assistant answers: persistence order, the one-per-conversation registry, tool status
 * relay, owner-only reads, failures and the execution context of the worker.
 */
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatGenerationManagerTest {

    @Autowired AiChatGenerationManager manager;
    @Autowired TestAiChatService chatService;
    @Autowired TestAiConversationService conversationService;
    @Autowired TestAiChatMessageService messageService;
    @Autowired TestUiUserEventCollector events;

    final CountDownLatch release = new CountDownLatch(1);

    @BeforeEach
    void setUp() {
        events.clear();
    }

    @AfterEach
    void tearDown() {
        release.countDown();
        // A worker still running would write into the next test.
        events.awaitAllFinished();
        chatService.reset();
        conversationService.clear();
        messageService.clear();
        events.clear();
    }

    @Test
    void start_persistsQuestionThenAnswer_andPublishesStartedCompleted() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");

        AiChatMessage question = TestAuthentication.callAs("alice", () -> manager.start(conversation, "Hi"));

        UiEventPublisher.UiUserEvent completed = events.await(AiChatGenerationCompletedEvent.class);
        List<AiChatMessage> messages = List.copyOf(messageService.loadMessages(conversation));
        assertEquals(List.of(AiChatMessageType.USER, AiChatMessageType.ASSISTANT),
                messages.stream().map(AiChatMessage::getType).toList());
        assertEquals(question.getId(), messages.get(0).getId());
        assertEquals(messages.get(1).getId(),
                ((AiChatGenerationCompletedEvent) completed.getEvent()).getAssistantMessageId());
        assertInstanceOf(AiChatGenerationStartedEvent.class, events.eventsOf(conversation.getId()).get(0));
        assertEquals(List.of("alice"), List.copyOf(completed.getUsernames()));
    }

    @Test
    void start_completed_answerPersistedAndEntryRemovedBeforeEvent() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        AtomicReference<Boolean> generatingAtEvent = new AtomicReference<>();
        AtomicInteger messagesAtEvent = new AtomicInteger();
        events.setOnEvent(event -> {
            if (event.getEvent() instanceof AiChatGenerationCompletedEvent) {
                generatingAtEvent.set(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));
                messagesAtEvent.set(messageService.loadMessages(conversation).size());
            }
        });

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        events.await(AiChatGenerationCompletedEvent.class);

        assertEquals(Boolean.FALSE, generatingAtEvent.get());
        assertEquals(2, messagesAtEvent.get());
    }

    @Test
    void start_whileRunning_rejectedAndPersistsNothing() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> awaitRelease());

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "First"));

        assertThrows(AiChatGenerationRejectedException.class,
                () -> TestAuthentication.runAs("alice", () -> manager.start(conversation, "Second")));
        assertEquals(1, messageService.loadMessages(conversation).size());
    }

    @Test
    void start_otherUsersConversation_deniedAndPersistsNothing() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");

        assertThrows(AccessDeniedException.class,
                () -> TestAuthentication.runAs("bob", () -> manager.start(conversation, "Hi")));

        assertTrue(events.isEmpty());
        assertTrue(messageService.loadMessages(conversation).isEmpty());
        assertFalse(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));
    }

    @Test
    void start_conversationNotFound_deniedAndPersistsNothing() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        conversationService.remove(conversation);

        assertThrows(AccessDeniedException.class,
                () -> TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi")));

        assertTrue(events.isEmpty());
        assertTrue(messageService.loadMessages(conversation).isEmpty());
    }

    @Test
    void onToolStatus_running_foldedIntoSnapshot_andPublishedAsProgress() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> {
            String id = spec.getCorrelationId();
            chatService.publishStatus(id, new AiToolStatusUpdate("Searching"));
            chatService.publishStatus(id, new AiToolStatusUpdate("Searching", "3 found"));
            return awaitRelease();
        });

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        awaitEventCount(conversation, AiChatGenerationProgressEvent.class, 2);

        AiChatGeneration snapshot = TestAuthentication.callAs("alice",
                () -> manager.getGeneration(conversation)).orElseThrow();
        assertEquals(1, snapshot.getStatuses().size());
        assertTrue(snapshot.getStatuses().get(0).isCompleted());
    }

    @Test
    void onToolStatus_numbersEveryUpdate_progressInOrder_andSnapshotCarriesLastNumber() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> {
            String id = spec.getCorrelationId();
            chatService.publishStatus(id, new AiToolStatusUpdate("Searching"));
            // A repeated in-flight update changes nothing and is not published, but still takes a number.
            chatService.publishStatus(id, new AiToolStatusUpdate("Searching"));
            chatService.publishStatus(id, new AiToolStatusUpdate("Searching", "3 found"));
            chatService.publishStatus(id, new AiToolStatusUpdate("Sorting"));
            return awaitRelease();
        });

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        awaitEventCount(conversation, AiChatGenerationProgressEvent.class, 3);

        assertEquals(List.of(1L, 3L, 4L), events.eventsOf(conversation.getId()).stream()
                .filter(AiChatGenerationProgressEvent.class::isInstance)
                .map(event -> ((AiChatGenerationProgressEvent) event).getSequence())
                .toList());
        AiChatGeneration snapshot = TestAuthentication.callAs("alice",
                () -> manager.getGeneration(conversation)).orElseThrow();
        assertEquals(4L, snapshot.getStatusSequence());
        assertEquals(2, snapshot.getStatuses().size());
    }

    @Test
    void onToolStatus_progressPublishBlocked_snapshotStillReadable() throws Exception {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> {
            chatService.publishStatus(spec.getCorrelationId(), new AiToolStatusUpdate("Searching"));
            return awaitRelease();
        });
        CountDownLatch publishing = new CountDownLatch(1);
        CountDownLatch publishReleased = new CountDownLatch(1);
        events.setOnEvent(event -> {
            if (event.getEvent() instanceof AiChatGenerationProgressEvent) {
                publishing.countDown();
                await(publishReleased);
            }
        });
        ExecutorService reader = Executors.newSingleThreadExecutor();
        try {
            TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
            assertTrue(publishing.await(5, TimeUnit.SECONDS));

            Future<Optional<AiChatGeneration>> snapshot = reader.submit(
                    () -> TestAuthentication.callAs("alice", () -> manager.getGeneration(conversation)));

            assertEquals(1L, snapshot.get(2, TimeUnit.SECONDS).orElseThrow().getStatusSequence());
        } finally {
            publishReleased.countDown();
            reader.shutdownNow();
        }
    }

    @Test
    void start_userMessageNotSavedYet_hiddenFromReads_butRejectsSecondStart() throws Exception {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> awaitRelease());
        CountDownLatch saving = new CountDownLatch(1);
        CountDownLatch saveReleased = new CountDownLatch(1);
        messageService.setBeforeSave(AiChatMessageType.USER, () -> {
            saving.countDown();
            await(saveReleased);
        });
        ExecutorService starter = Executors.newSingleThreadExecutor();
        try {
            Future<?> firstStart = starter.submit(
                    () -> TestAuthentication.runAs("alice", () -> manager.start(conversation, "First")));
            assertTrue(saving.await(5, TimeUnit.SECONDS));

            assertTrue(TestAuthentication.callAs("alice", () -> manager.getGeneration(conversation)).isEmpty());
            assertFalse(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));
            assertThrows(AiChatGenerationRejectedException.class,
                    () -> TestAuthentication.runAs("alice", () -> manager.start(conversation, "Second")));

            saveReleased.countDown();
            firstStart.get(5, TimeUnit.SECONDS);

            assertTrue(TestAuthentication.callAs("alice", () -> manager.getGeneration(conversation)).isPresent());
        } finally {
            saveReleased.countDown();
            starter.shutdownNow();
        }
    }

    @Test
    void start_eventsSnapshotAndCorrelationId_carrySameGenerationId() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        AtomicReference<String> correlationId = new AtomicReference<>();
        chatService.setResponder(spec -> {
            String id = spec.getCorrelationId();
            correlationId.set(id);
            chatService.publishStatus(id, new AiToolStatusUpdate("Searching"));
            return awaitRelease();
        });

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        awaitEventCount(conversation, AiChatGenerationProgressEvent.class, 1);
        UUID generationId = TestAuthentication.callAs("alice",
                () -> manager.getGeneration(conversation)).orElseThrow().getGenerationId();
        release.countDown();
        events.await(AiChatGenerationCompletedEvent.class);

        assertEquals(List.of(AiChatGenerationStartedEvent.class, AiChatGenerationProgressEvent.class,
                        AiChatGenerationCompletedEvent.class),
                events.eventsOf(conversation.getId()).stream().map(Object::getClass).toList());
        assertEquals(List.of(generationId, generationId, generationId), generationIds(conversation));
        assertEquals(generationId.toString(), correlationId.get());
    }

    @Test
    void start_nextResponseInConversation_getsNewGenerationId() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "First"));
        awaitEventCount(conversation, AiChatGenerationCompletedEvent.class, 1);
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Second"));
        awaitEventCount(conversation, AiChatGenerationCompletedEvent.class, 2);

        List<UUID> ids = generationIds(conversation);
        assertEquals(4, ids.size());
        assertEquals(ids.get(0), ids.get(1));
        assertEquals(ids.get(2), ids.get(3));
        assertNotEquals(ids.get(0), ids.get(2));
    }

    @Test
    void onToolStatus_unknownCorrelationId_ignored() {
        chatService.publishStatus("unknown", new AiToolStatusUpdate("Searching"));

        assertTrue(events.isEmpty());
    }

    @Test
    void getGeneration_otherUser_seesNothing() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> awaitRelease());
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));

        assertTrue(TestAuthentication.callAs("bob", () -> manager.getGeneration(conversation)).isEmpty());
        assertFalse(TestAuthentication.callAs("bob", () -> manager.isGenerating(conversation)));
    }

    @Test
    void start_underSubstitution_ownedBySubstitutedUser_andSentToBoth() {
        AiConversation conversation = conversationService.addConversation("Chat", "boris");
        chatService.setResponder(spec -> awaitRelease());

        TestAuthentication.callAsSubstituting("anna", "boris", () -> manager.start(conversation, "Hi"));

        UiEventPublisher.UiUserEvent started = events.await(AiChatGenerationStartedEvent.class);
        assertEquals(List.of("boris", "anna"), List.copyOf(started.getUsernames()));
        assertTrue(TestAuthentication.callAs("boris", () -> manager.isGenerating(conversation)));
        assertFalse(TestAuthentication.callAs("anna", () -> manager.isGenerating(conversation)));
    }

    @Test
    void start_generationFails_publishesFailed_andPersistsNoAnswer() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> {
            throw new IllegalStateException("model down");
        });

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));

        events.await(AiChatGenerationFailedEvent.class);
        assertEquals(1, messageService.loadMessages(conversation).size());
        assertFalse(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));
    }

    @Test
    void start_blankAnswer_publishesFailed_andPersistsNoAnswer() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> "  ");

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));

        events.await(AiChatGenerationFailedEvent.class);
        assertEquals(1, messageService.loadMessages(conversation).size());
    }

    @Test
    void start_userMessageSaveFails_rethrowsAndReleasesConversation() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        IllegalStateException saveError = new IllegalStateException("database down");
        messageService.setFailure(AiChatMessageType.USER, saveError);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi")));

        assertEquals(saveError, thrown);
        assertTrue(events.isEmpty());
        assertTrue(messageService.loadMessages(conversation).isEmpty());
        assertFalse(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));

        messageService.setFailure(AiChatMessageType.USER, null);
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Again"));
        events.await(AiChatGenerationCompletedEvent.class);
        assertEquals(2, messageService.loadMessages(conversation).size());
    }

    @Test
    void start_answerSaveFails_publishesFailed_andReleasesConversation() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        messageService.setFailure(AiChatMessageType.ASSISTANT, new IllegalStateException("database down"));

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));

        events.await(AiChatGenerationFailedEvent.class);
        assertEquals(List.of(AiChatMessageType.USER),
                messageService.loadMessages(conversation).stream().map(AiChatMessage::getType).toList());
        assertFalse(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));
    }

    @Test
    void start_executorRejects_rethrowsPublishesFailed_andReleasesConversation() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        Object poolExecutor = ReflectionTestUtils.getField(manager, "executor");
        ExecutorService stopped = Executors.newSingleThreadExecutor();
        stopped.shutdown();
        ReflectionTestUtils.setField(manager, "executor", stopped);
        try {
            assertThrows(RejectedExecutionException.class,
                    () -> TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi")));
        } finally {
            ReflectionTestUtils.setField(manager, "executor", poolExecutor);
        }

        assertEquals(List.of(AiChatGenerationStartedEvent.class, AiChatGenerationFailedEvent.class),
                events.eventsOf(conversation.getId()).stream().map(Object::getClass).toList());
        assertFalse(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Again"));
        events.await(AiChatGenerationCompletedEvent.class);
    }

    @Test
    void start_submitFailsUnexpectedly_rethrowsPublishesFailed_andReleasesConversation() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        Object poolExecutor = ReflectionTestUtils.getField(manager, "executor");
        ReflectionTestUtils.setField(manager, "executor", null);
        try {
            assertThrows(NullPointerException.class,
                    () -> TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi")));
        } finally {
            ReflectionTestUtils.setField(manager, "executor", poolExecutor);
        }

        assertEquals(List.of(AiChatGenerationStartedEvent.class, AiChatGenerationFailedEvent.class),
                events.eventsOf(conversation.getId()).stream().map(Object::getClass).toList());
        assertFalse(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Again"));
        events.await(AiChatGenerationCompletedEvent.class);
    }

    @Test
    void start_worker_runsAsStartingUser() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        AtomicReference<String> workerUser = new AtomicReference<>();
        chatService.setResponder(spec -> {
            workerUser.set(SecurityContextHolder.getContext().getAuthentication().getName());
            return "ok";
        });

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        events.await(AiChatGenerationCompletedEvent.class);

        assertEquals("alice", workerUser.get());
    }

    String awaitRelease() {
        await(release);
        return "late answer";
    }

    void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    void awaitEventCount(AiConversation conversation, Class<? extends ApplicationEvent> type, int count) {
        long deadline = System.currentTimeMillis() + 5000;
        while (events.eventsOf(conversation.getId()).stream().filter(type::isInstance).count() < count) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("Expected " + count + " " + type.getSimpleName() + " events");
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
    }

    List<UUID> generationIds(AiConversation conversation) {
        return events.eventsOf(conversation.getId()).stream()
                .map(event -> ((AiChatGenerationEvent) event).getGenerationId())
                .toList();
    }
}
