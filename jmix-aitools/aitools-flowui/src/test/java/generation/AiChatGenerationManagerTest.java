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
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCancelledEvent;
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
import org.jspecify.annotations.Nullable;
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
import test_support.TestGenerationWorkers;
import test_support.TestUiUserEventCollector;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Background generation of assistant answers: persistence order, the one-per-conversation registry, tool status
 * relay, owner-only reads, failures, cancellation and the execution context of the worker.
 */
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatGenerationManagerTest {

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
        // A worker still running would write into the next test.
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
    void start_conversationWithoutUsername_accessDecidedByConversationService() {
        // A custom AiConversationService may keep the owner elsewhere; returning the conversation grants access.
        AiConversation conversation = conversationService.addConversation("Chat");

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));

        events.await(AiChatGenerationCompletedEvent.class);
        assertEquals(2, messageService.loadMessages(conversation).size());
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
    void retry_unansweredQuestion_answersItWithoutSavingAnotherMessage() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> {
            throw new IllegalStateException("model down");
        });
        AiChatMessage question = TestAuthentication.callAs("alice", () -> manager.start(conversation, "Hi"));
        events.await(AiChatGenerationFailedEvent.class);
        chatService.reset();
        events.clear();

        AiChatMessage retried = TestAuthentication.callAs("alice", () -> manager.retry(conversation));

        UiEventPublisher.UiUserEvent completed = events.await(AiChatGenerationCompletedEvent.class);
        assertNotNull(retried);
        assertEquals(question.getId(), retried.getId());
        List<AiChatMessage> messages = List.copyOf(messageService.loadMessages(conversation));
        assertEquals(List.of(AiChatMessageType.USER, AiChatMessageType.ASSISTANT),
                messages.stream().map(AiChatMessage::getType).toList());
        assertEquals(messages.get(1).getId(),
                ((AiChatGenerationCompletedEvent) completed.getEvent()).getAssistantMessageId());
        assertInstanceOf(AiChatGenerationStartedEvent.class, events.eventsOf(conversation.getId()).get(0));
    }

    @Test
    void retry_whileRunning_rejectedAndPersistsNothing() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> awaitRelease());
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));

        assertThrows(AiChatGenerationRejectedException.class,
                () -> TestAuthentication.runAs("alice", () -> manager.retry(conversation)));
        assertEquals(1, messageService.loadMessages(conversation).size());
    }

    @Test
    void retry_lastQuestionAnswered_returnsNull_startsNothing_andKeepsConversationOpen() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        events.await(AiChatGenerationCompletedEvent.class);
        events.clear();

        AiChatMessage retried = TestAuthentication.callAs("alice", () -> manager.retry(conversation));

        assertNull(retried);
        assertTrue(events.isEmpty());
        assertFalse(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Next"));
        events.await(AiChatGenerationCompletedEvent.class);
        assertEquals(4, messageService.loadMessages(conversation).size());
    }

    @Test
    void retry_emptyConversation_returnsNull_andStartsNothing() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");

        AiChatMessage retried = TestAuthentication.callAs("alice", () -> manager.retry(conversation));

        assertNull(retried);
        assertTrue(events.isEmpty());
        assertTrue(messageService.loadMessages(conversation).isEmpty());
    }

    @Test
    void retry_otherUsersConversation_deniedAndStartsNothing() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");

        assertThrows(AccessDeniedException.class,
                () -> TestAuthentication.runAs("bob", () -> manager.retry(conversation)));

        assertTrue(events.isEmpty());
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

    @Test
    void cancel_running_discardsLateAnswer_andPublishesCancelled() throws Exception {
        assertLateOutcomeDiscarded(() -> "late answer");
    }

    @Test
    void cancel_running_lateModelFailure_publishesNothingMore() throws Exception {
        assertLateOutcomeDiscarded(() -> {
            throw new IllegalStateException("model down");
        });
    }

    @Test
    void cancel_running_lateBlankAnswer_publishesNothingMore() throws Exception {
        assertLateOutcomeDiscarded(() -> "  ");
    }

    @Test
    void start_afterCancel_conversationAcceptsNextQuestion() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> awaitRelease());
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "First"));
        assertTrue(TestAuthentication.callAs("alice", () -> manager.cancel(conversation)));

        chatService.reset();
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Second"));

        events.await(AiChatGenerationCompletedEvent.class);
        assertEquals(List.of(AiChatMessageType.USER, AiChatMessageType.USER, AiChatMessageType.ASSISTANT),
                messageService.loadMessages(conversation).stream().map(AiChatMessage::getType).toList());
    }

    @Test
    void cancel_otherUserWhileRunning_accessDenied_andKeepsResponseRunning() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> awaitRelease());
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));

        assertThrows(AccessDeniedException.class,
                () -> TestAuthentication.callAs("bob", () -> manager.cancel(conversation)));
        assertTrue(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));
    }

    @Test
    void cancel_otherUserNothingRunning_accessDenied() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");

        assertThrows(AccessDeniedException.class,
                () -> TestAuthentication.callAs("bob", () -> manager.cancel(conversation)));
    }

    @Test
    void cancel_nothingRunning_returnsFalse() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");

        assertFalse(TestAuthentication.callAs("alice", () -> manager.cancel(conversation)));
        assertTrue(events.isEmpty());
    }

    @Test
    void cancel_answerBeingSaved_refused_andAnswerCompletes() throws Exception {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        CountDownLatch saving = new CountDownLatch(1);
        CountDownLatch saveReleased = new CountDownLatch(1);
        messageService.setBeforeSave(AiChatMessageType.ASSISTANT, () -> {
            saving.countDown();
            await(saveReleased);
        });
        try {
            TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
            assertTrue(saving.await(5, TimeUnit.SECONDS));

            assertFalse(TestAuthentication.callAs("alice", () -> manager.cancel(conversation)));
        } finally {
            saveReleased.countDown();
        }

        events.await(AiChatGenerationCompletedEvent.class);
        assertEquals(List.of(AiChatGenerationStartedEvent.class, AiChatGenerationCompletedEvent.class),
                eventTypes(conversation));
        assertEquals(2, messageService.loadMessages(conversation).size());
    }

    @Test
    void cancel_beforeUserMessageSaved_startsNothing_andPublishesCancelled() throws Exception {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        CountDownLatch saving = new CountDownLatch(1);
        CountDownLatch saveReleased = new CountDownLatch(1);
        messageService.setBeforeSave(AiChatMessageType.USER, () -> {
            saving.countDown();
            await(saveReleased);
        });
        ExecutorService starter = Executors.newSingleThreadExecutor();
        try {
            Future<?> start = starter.submit(
                    () -> TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi")));
            assertTrue(saving.await(5, TimeUnit.SECONDS));

            assertTrue(TestAuthentication.callAs("alice", () -> manager.cancel(conversation)));
            // The starting thread reports it, once it is past the save.
            assertTrue(events.isEmpty());
            saveReleased.countDown();
            start.get(5, TimeUnit.SECONDS);
        } finally {
            saveReleased.countDown();
            starter.shutdownNow();
        }

        assertEquals(List.of(AiChatGenerationCancelledEvent.class), eventTypes(conversation));
        assertFalse(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));
    }

    @Test
    void cancel_whileStartedIsPublished_singleCancelledFollowsStarted_andModelIsNotCalled() throws Exception {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        AtomicBoolean modelCalled = new AtomicBoolean();
        chatService.setResponder(spec -> {
            modelCalled.set(true);
            return "answer";
        });
        CountDownLatch publishing = new CountDownLatch(1);
        CountDownLatch publishReleased = new CountDownLatch(1);
        events.setOnEvent(event -> {
            if (event.getEvent() instanceof AiChatGenerationStartedEvent) {
                publishing.countDown();
                await(publishReleased);
            }
        });
        ExecutorService starter = Executors.newSingleThreadExecutor();
        try {
            Future<?> start = starter.submit(
                    () -> TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi")));
            assertTrue(publishing.await(5, TimeUnit.SECONDS));

            assertTrue(TestAuthentication.callAs("alice", () -> manager.cancel(conversation)));
            publishReleased.countDown();
            start.get(5, TimeUnit.SECONDS);
        } finally {
            publishReleased.countDown();
            starter.shutdownNow();
        }

        assertEquals(List.of(AiChatGenerationStartedEvent.class, AiChatGenerationCancelledEvent.class),
                eventTypes(conversation));
        assertFalse(modelCalled.get());
    }

    @Test
    void stop_responseFailedMeanwhile_refused_andPublishingWorkerNotInterrupted() throws Exception {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        CountDownLatch calling = new CountDownLatch(1);
        CountDownLatch failNow = new CountDownLatch(1);
        chatService.setResponder(spec -> {
            calling.countDown();
            await(failNow);
            throw new IllegalStateException("model down");
        });
        CountDownLatch publishing = new CountDownLatch(1);
        CountDownLatch publishReleased = new CountDownLatch(1);
        AtomicBoolean interruptedWhilePublishing = new AtomicBoolean();
        events.setOnEvent(event -> {
            if (event.getEvent() instanceof AiChatGenerationFailedEvent) {
                publishing.countDown();
                await(publishReleased);
                interruptedWhilePublishing.set(Thread.currentThread().isInterrupted());
            }
        });
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        assertTrue(calling.await(5, TimeUnit.SECONDS));
        // A cancel that looked the response up just before it failed.
        Object generation = runningGeneration(conversation);
        failNow.countDown();
        try {
            assertTrue(publishing.await(5, TimeUnit.SECONDS));

            assertEquals(Boolean.FALSE, ReflectionTestUtils.invokeMethod(manager, "stop", generation));
        } finally {
            publishReleased.countDown();
        }

        events.await(AiChatGenerationFailedEvent.class);
        assertFalse(interruptedWhilePublishing.get());
    }

    @Test
    void start_running_schedulesTimeout_andCancelDropsIt() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> awaitRelease());
        int queued = timeoutQueue().size();

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        assertEquals(queued + 1, timeoutQueue().size());

        TestAuthentication.runAs("alice", () -> manager.cancel(conversation));
        assertEquals(queued, timeoutQueue().size());
    }

    @Test
    void start_completed_dropsItsTimeout() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        int queued = timeoutQueue().size();
        AtomicInteger queuedAtEvent = new AtomicInteger(-1);
        events.setOnEvent(event -> {
            if (event.getEvent() instanceof AiChatGenerationCompletedEvent) {
                queuedAtEvent.set(timeoutQueue().size());
            }
        });

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        events.await(AiChatGenerationCompletedEvent.class);

        assertEquals(queued, queuedAtEvent.get());
    }

    @Test
    void start_failed_dropsItsTimeout() {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        chatService.setResponder(spec -> {
            throw new IllegalStateException("model down");
        });
        int queued = timeoutQueue().size();
        AtomicInteger queuedAtEvent = new AtomicInteger(-1);
        events.setOnEvent(event -> {
            if (event.getEvent() instanceof AiChatGenerationFailedEvent) {
                queuedAtEvent.set(timeoutQueue().size());
            }
        });

        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        events.await(AiChatGenerationFailedEvent.class);

        assertEquals(queued, queuedAtEvent.get());
    }

    /**
     * Cancels a response whose model call ignores the interruption, lets the call end with the given outcome and
     * checks that nothing of it is saved or published.
     *
     * @param lateOutcome how the model call ends after the cancel: returns an answer or throws
     * @throws Exception if waiting for the worker fails
     */
    void assertLateOutcomeDiscarded(Supplier<String> lateOutcome) throws Exception {
        AiConversation conversation = conversationService.addConversation("Chat", "alice");
        ownWorkers = TestGenerationWorkers.install(manager);
        CountDownLatch calling = new CountDownLatch(1);
        chatService.setResponder(spec -> {
            calling.countDown();
            TestAiChatService.awaitIgnoringInterrupt(release);
            return lateOutcome.get();
        });
        TestAuthentication.runAs("alice", () -> manager.start(conversation, "Hi"));
        assertTrue(calling.await(5, TimeUnit.SECONDS));

        assertTrue(TestAuthentication.callAs("alice", () -> manager.cancel(conversation)));
        release.countDown();
        ownWorkers.awaitIdle();

        assertEquals(List.of(AiChatGenerationStartedEvent.class, AiChatGenerationCancelledEvent.class),
                eventTypes(conversation));
        assertEquals(List.of(AiChatMessageType.USER),
                messageService.loadMessages(conversation).stream().map(AiChatMessage::getType).toList());
        assertFalse(TestAuthentication.callAs("alice", () -> manager.isGenerating(conversation)));
    }

    Object runningGeneration(AiConversation conversation) {
        Map<?, ?> generations = (Map<?, ?>) ReflectionTestUtils.getField(manager, "generationsByConversation");
        return Objects.requireNonNull(Objects.requireNonNull(generations).get(conversation.getId()));
    }

    BlockingQueue<Runnable> timeoutQueue() {
        Object scheduler = ReflectionTestUtils.getField(manager, "timeoutScheduler");
        return ((ScheduledThreadPoolExecutor) Objects.requireNonNull(scheduler)).getQueue();
    }

    List<Class<?>> eventTypes(AiConversation conversation) {
        return events.eventsOf(conversation.getId()).stream()
                .<Class<?>>map(Object::getClass)
                .toList();
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
