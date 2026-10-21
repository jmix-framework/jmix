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

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.dom.Element;
import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.fragment.chat.AiChatFragment;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineItem;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineItemStatus;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineItemType;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputFragment;
import io.jmix.aitoolsflowui.generation.AiChatGeneration;
import io.jmix.aitoolsflowui.generation.AiChatGenerationManager;
import io.jmix.aitoolsflowui.generation.AiChatGenerationRejectedException;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCancelledEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCompletedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationFailedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationProgressEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationStartedEvent;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiConversationAutoTitleService;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.virtuallist.JmixVirtualList;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.model.CollectionChangeType;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiChatMessageService;
import test_support.TestAiConversationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The chat fragment sends through {@link AiChatGenerationManager} and renders its events: the thinking
 * indicator, folded tool statuses, the answer, the error and a cancel. The manager is mocked and events are delivered
 * by calling the fragment's listeners.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatFragmentGenerationTest {

    static final UUID GENERATION_ID = UUID.randomUUID();
    static final String CHAT_MESSAGES_GROUP = AiChatFragment.class.getPackageName();

    @Autowired ViewNavigationSupport navigationSupport;
    @Autowired RouteSupport routeSupport;
    @Autowired TestAiConversationService conversationService;
    @Autowired TestAiChatMessageService messageService;
    @Autowired Messages messages;
    @Autowired Metadata metadata;

    @MockitoBean AiChatGenerationManager generationManager;
    @MockitoBean AiConversationAutoTitleService autoTitleService;
    // Deep stubs keep notifications a test does not look at from failing on a null builder.
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS) Notifications notifications;

    @AfterEach
    void tearDown() {
        conversationService.clear();
        messageService.clear();
    }

    @Test
    void sendMessage_idle_startsThroughManager_showsThinking_andDisablesComposer() {
        AiConversation conversation = conversationService.addConversation("Chat");
        when(generationManager.start(any(), any())).thenAnswer(inv ->
                messageService.createMessage(inv.getArgument(0), AiChatMessageType.USER, inv.getArgument(1)));
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));

        fragment.sendMessage("Hi");

        verify(generationManager).start(any(), eq("Hi"));
        assertTrue(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
    }

    @Test
    void onGenerationStarted_boundConversation_showsThinking_andDisablesComposer() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));

        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));

        assertTrue(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
    }

    @Test
    void onGenerationProgress_running_foldsStatusIntoThinking() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));

        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));
        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), GENERATION_ID,
                new AiToolStatusUpdate("Searching"), 1));
        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), GENERATION_ID,
                new AiToolStatusUpdate("Searching", "3 found"), 2));

        TimelineItem thinking = timelineItems(fragment).get(0);
        assertEquals(TimelineItemType.ASSISTANT_THINKING, thinking.getType());
        assertEquals(1, thinking.getStatusUpdates().size());
        assertEquals("3 found", thinking.getStatusUpdates().get(0).getResultSnippet());
    }

    @Test
    void onGenerationProgress_withoutThinking_isIgnored() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));

        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), GENERATION_ID,
                new AiToolStatusUpdate("Searching"), 1));

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(timelineItems(fragment).isEmpty());
    }

    @Test
    void onGenerationStarted_otherConversation_isIgnored() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));

        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, UUID.randomUUID(), GENERATION_ID));

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertTrue(timelineItems(fragment).isEmpty());
    }

    @Test
    void onGenerationCompleted_running_reloadsMessages_marksAnswerFresh_andUnlocks() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        AiChatMessage answer = messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello!");

        fragment.onGenerationCompleted(new AiChatGenerationCompletedEvent(this, conversation.getId(), GENERATION_ID,
                answer.getId()));

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT), timelineTypes(fragment));
        TimelineItem answerItem = timelineItems(fragment).get(1);
        assertEquals(answer.getId(), answerItem.getMessage().getId());
        // TRUE until the renderer draws the row, FALSE after it consumed the marker; null means never marked.
        assertNotNull(answerItem.getFresh());
    }

    @Test
    void onGenerationFailed_running_showsErrorBubble_andUnlocks() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));

        fragment.onGenerationFailed(new AiChatGenerationFailedEvent(this, conversation.getId(), GENERATION_ID));

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.ASSISTANT), timelineTypes(fragment));
    }

    @Test
    void onGenerationCancelled_running_dropsThinking_andUnlocks_withoutErrorBubble() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));

        fragment.onGenerationCancelled(new AiChatGenerationCancelledEvent(this, conversation.getId(), GENERATION_ID));

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertTrue(timelineItems(fragment).isEmpty());
    }

    @Test
    void onGenerationCancelled_afterSendBeforeStarted_dropsThinking_andUnlocks() {
        AiConversation conversation = conversationService.addConversation("Chat");
        when(generationManager.start(any(), any())).thenAnswer(inv ->
                messageService.createMessage(inv.getArgument(0), AiChatMessageType.USER, inv.getArgument(1)));
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.sendMessage("Hi");

        fragment.onGenerationCancelled(new AiChatGenerationCancelledEvent(this, conversation.getId(), GENERATION_ID));

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER), timelineTypes(fragment));
    }

    @Test
    void onGenerationCancelled_nothingShown_leavesComposerAlone() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        // Disabled by the host, not by a response.
        fragment.setMessageInputEnabled(false);

        fragment.onGenerationCancelled(new AiChatGenerationCancelledEvent(this, conversation.getId(), GENERATION_ID));

        assertFalse(composer(fragment).isInputEnabled());
        assertTrue(timelineItems(fragment).isEmpty());
    }

    @Test
    void onGenerationCancelled_otherGenerationShown_ignored() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));

        fragment.onGenerationCancelled(new AiChatGenerationCancelledEvent(this, conversation.getId(),
                UUID.randomUUID()));

        assertTrue(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
    }

    @Test
    void onGenerationFailed_withoutThinking_showsErrorBubble() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));

        fragment.onGenerationFailed(new AiChatGenerationFailedEvent(this, conversation.getId(), GENERATION_ID));

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.ASSISTANT), timelineTypes(fragment));
    }

    @Test
    void onGenerationStarted_afterSendMessage_adoptsGeneration_andAppliesItsProgress() {
        AiConversation conversation = conversationService.addConversation("Chat");
        when(generationManager.start(any(), any())).thenAnswer(inv ->
                messageService.createMessage(inv.getArgument(0), AiChatMessageType.USER, inv.getArgument(1)));
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.sendMessage("Hi");

        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));
        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), GENERATION_ID,
                new AiToolStatusUpdate("Searching"), 1));

        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
        assertEquals(1, timelineItems(fragment).get(1).getStatusUpdates().size());
    }

    @Test
    void onGenerationEvents_lateEventsOfReplacedGeneration_ignored() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        UUID previous = UUID.randomUUID();
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), previous));
        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), previous,
                new AiToolStatusUpdate("Old step"), 1));

        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));
        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), previous,
                new AiToolStatusUpdate("Late step"), 2));
        AiChatMessage oldAnswer = messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Old");
        fragment.onGenerationCompleted(new AiChatGenerationCompletedEvent(this, conversation.getId(), previous,
                oldAnswer.getId()));
        fragment.onGenerationFailed(new AiChatGenerationFailedEvent(this, conversation.getId(), previous));

        assertTrue(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
        assertTrue(timelineItems(fragment).get(0).getStatusUpdates().isEmpty());

        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), GENERATION_ID,
                new AiToolStatusUpdate("New step"), 1));
        assertEquals(List.of("New step"), timelineItems(fragment).get(0).getStatusUpdates().stream()
                .map(TimelineItemStatus::getMessage).toList());
    }

    @Test
    void onGenerationCompleted_withoutThinking_reloadsMessages() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        AiChatMessage answer = messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello!");

        fragment.onGenerationCompleted(new AiChatGenerationCompletedEvent(this, conversation.getId(), GENERATION_ID,
                answer.getId()));

        assertFalse(fragment.isAwaitingResponse());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT), timelineTypes(fragment));
    }

    @Test
    void setConversation_rebindWhileRunning_clearsThinking_andEnablesComposer() {
        AiConversation first = conversationService.addConversation("First");
        AiConversation second = conversationService.addConversation("Second");
        AiChatFragment fragment = chatFragment(navigate(first.getId()));
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, first.getId(), GENERATION_ID));

        fragment.setConversation(second);

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertTrue(timelineItems(fragment).isEmpty());
    }

    @Test
    void setConversation_hostDisabledComposer_staysDisabled() {
        AiConversation first = conversationService.addConversation("First");
        AiConversation second = conversationService.addConversation("Second");
        AiChatFragment fragment = chatFragment(navigate(first.getId()));
        fragment.setMessageInputEnabled(false);

        fragment.setConversation(second);

        assertFalse(composer(fragment).isInputEnabled());
    }

    @Test
    void onGenerationCompleted_hostDisabledComposer_staysDisabled() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));
        fragment.setMessageInputEnabled(false);
        AiChatMessage answer = messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello!");

        fragment.onGenerationCompleted(new AiChatGenerationCompletedEvent(this, conversation.getId(), GENERATION_ID,
                answer.getId()));

        assertFalse(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
    }

    @Test
    void onGenerationFailed_hostDisabledComposer_staysDisabled() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.setMessageInputEnabled(false);
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));

        fragment.onGenerationFailed(new AiChatGenerationFailedEvent(this, conversation.getId(), GENERATION_ID));

        assertFalse(composer(fragment).isInputEnabled());
    }

    @Test
    void setMessageInputEnabled_whileRunning_composerStaysLockedUntilResponseEnds() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));

        fragment.setMessageInputEnabled(true);
        assertFalse(composer(fragment).isInputEnabled());

        fragment.onGenerationCancelled(new AiChatGenerationCancelledEvent(this, conversation.getId(), GENERATION_ID));
        assertTrue(composer(fragment).isInputEnabled());
    }

    @Test
    void open_whileGenerating_showsThinkingWithSnapshotStatuses_andDisablesComposer() {
        AiConversation conversation = conversationService.addConversation("Chat");
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        when(generationManager.getGeneration(any())).thenReturn(Optional.of(new AiChatGeneration(
                conversation.getId(), GENERATION_ID, List.of(new AiToolStatusUpdate("Searching", "3 found")), 2)));

        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));

        assertTrue(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
        List<TimelineItemStatus> statuses = timelineItems(fragment).get(1).getStatusUpdates();
        assertEquals(1, statuses.size());
        assertEquals("3 found", statuses.get(0).getResultSnippet());
    }

    @Test
    void open_whileGenerating_appliesLaterEventsOfSnapshotGeneration() {
        AiConversation conversation = conversationService.addConversation("Chat");
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        when(generationManager.getGeneration(any())).thenReturn(Optional.of(new AiChatGeneration(
                conversation.getId(), GENERATION_ID, List.of(new AiToolStatusUpdate("Searching")), 1)));
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        when(generationManager.getGeneration(any())).thenReturn(Optional.empty());

        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), GENERATION_ID,
                new AiToolStatusUpdate("Searching", "3 found"), 2));

        List<TimelineItemStatus> statuses = timelineItems(fragment).get(1).getStatusUpdates();
        assertEquals(1, statuses.size());
        assertEquals("3 found", statuses.get(0).getResultSnippet());

        AiChatMessage answer = messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello!");
        fragment.onGenerationCompleted(new AiChatGenerationCompletedEvent(this, conversation.getId(), GENERATION_ID,
                answer.getId()));

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT), timelineTypes(fragment));
    }

    @Test
    void open_notGenerating_noThinking() {
        AiConversation conversation = conversationService.addConversation("Chat");
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        when(generationManager.getGeneration(any())).thenReturn(Optional.empty());

        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER), timelineTypes(fragment));
    }

    @Test
    void setConversation_rebindToGenerating_showsThinking() {
        AiConversation first = conversationService.addConversation("First");
        AiConversation second = conversationService.addConversation("Second");
        AiChatFragment fragment = chatFragment(navigate(first.getId()));
        when(generationManager.getGeneration(any())).thenReturn(
                Optional.of(new AiChatGeneration(second.getId(), GENERATION_ID, List.of(), 0)));

        fragment.setConversation(second);

        assertTrue(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
    }

    @Test
    void reattach_generationStartedWhileDetached_showsThinking() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        Element parent = fragment.getElement().getParent();
        int index = parent.indexOfChild(fragment.getElement());
        parent.removeChild(fragment.getElement());
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        when(generationManager.getGeneration(any())).thenReturn(
                Optional.of(new AiChatGeneration(conversation.getId(), GENERATION_ID, List.of(), 0)));

        parent.insertChild(index, fragment.getElement());

        assertTrue(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
    }

    @Test
    void reattach_generationCompletedWhileDetached_showsAnswer_andEnablesComposer() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));
        Element parent = fragment.getElement().getParent();
        int index = parent.indexOfChild(fragment.getElement());
        parent.removeChild(fragment.getElement());
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello!");

        parent.insertChild(index, fragment.getElement());

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT), timelineTypes(fragment));
    }

    @Test
    void onGenerationStarted_nothingShown_reloadsMessagesBeforeThinking() {
        AiConversation conversation = conversationService.addConversation("Chat");
        messageService.createMessage(conversation, AiChatMessageType.USER, "Earlier");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        // Another tab of the same user sent the message.
        messageService.createMessage(conversation, AiChatMessageType.USER, "From the other tab");

        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));

        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.USER, TimelineItemType.ASSISTANT_THINKING),
                timelineTypes(fragment));
        assertEquals("From the other tab", timelineItems(fragment).get(1).getMessage().getContent());
    }

    @Test
    void sendMessage_rejected_resyncsAndKeepsText() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatView view = navigate(conversation.getId());
        AiChatFragment fragment = chatFragment(view);
        // Another tab started a response meanwhile.
        messageService.createMessage(conversation, AiChatMessageType.USER, "From the other tab");
        when(generationManager.getGeneration(any())).thenReturn(
                Optional.of(new AiChatGeneration(conversation.getId(), GENERATION_ID, List.of(), 0)));
        when(generationManager.start(any(), any())).thenThrow(
                new AiChatGenerationRejectedException(conversation.getId()));
        messageField(fragment).setValue("Mine");

        fragment.sendMessage("Mine");

        assertTrue(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
        assertEquals("From the other tab", timelineItems(fragment).get(0).getMessage().getContent());
        assertEquals("Mine", composer(fragment).getValue());
    }

    @Test
    void sendMessage_startFailsAfterSavingMessage_resyncsAndKeepsText() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        when(generationManager.start(any(), any())).thenAnswer(inv -> {
            messageService.createMessage(inv.getArgument(0), AiChatMessageType.USER, inv.getArgument(1));
            throw new RejectedExecutionException("Shutting down");
        });
        messageField(fragment).setValue("Mine");

        fragment.sendMessage("Mine");

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER), timelineTypes(fragment));
        assertEquals("Mine", composer(fragment).getValue());
    }

    @Test
    void onGenerationProgress_alreadyInSnapshot_ignored_andNextApplied() {
        AiConversation conversation = conversationService.addConversation("Chat");
        when(generationManager.getGeneration(any())).thenReturn(Optional.of(new AiChatGeneration(
                conversation.getId(), GENERATION_ID, List.of(new AiToolStatusUpdate("Searching", "3 found")), 2)));
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));

        // Queued while the snapshot was read: already part of it.
        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), GENERATION_ID,
                new AiToolStatusUpdate("Searching", "3 found"), 2));
        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), GENERATION_ID,
                new AiToolStatusUpdate("Sorting"), 3));

        assertEquals(List.of("Searching", "Sorting"), timelineItems(fragment).get(0).getStatusUpdates().stream()
                .map(TimelineItemStatus::getMessage).toList());
    }

    @Test
    void onGenerationStarted_sameGenerationAfterSnapshot_keepsSnapshotSequence() {
        AiConversation conversation = conversationService.addConversation("Chat");
        when(generationManager.getGeneration(any())).thenReturn(Optional.of(new AiChatGeneration(
                conversation.getId(), GENERATION_ID, List.of(new AiToolStatusUpdate("Searching", "3 found")), 2)));
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));

        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));
        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), GENERATION_ID,
                new AiToolStatusUpdate("Searching", "3 found"), 2));

        assertEquals(1, timelineItems(fragment).get(0).getStatusUpdates().size());
    }

    @Test
    void onGenerationCompleted_previousGenerationWhileNextRuns_showsNext_andKeepsComposerLocked() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        AiChatMessage answer = messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello!");
        // Another tab already asked the next question.
        messageService.createMessage(conversation, AiChatMessageType.USER, "Next");
        when(generationManager.getGeneration(any())).thenReturn(
                Optional.of(new AiChatGeneration(conversation.getId(), UUID.randomUUID(), List.of(), 0)));

        fragment.onGenerationCompleted(new AiChatGenerationCompletedEvent(this, conversation.getId(), GENERATION_ID,
                answer.getId()));

        assertTrue(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT, TimelineItemType.USER,
                TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
    }

    @Test
    void retryResponse_afterFailure_retriesThroughManager_replacesErrorWithThinking_andDisablesComposer() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatMessage question = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));
        fragment.onGenerationFailed(new AiChatGenerationFailedEvent(this, conversation.getId(), GENERATION_ID));
        when(generationManager.retry(any())).thenReturn(question);

        fragment.retryResponse();

        verify(generationManager).retry(any());
        assertTrue(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
    }

    @Test
    void retryResponse_questionAnsweredMeanwhile_showsTheAnswer() {
        AiConversation conversation = conversationService.addConversation("Chat");
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        // Answered in another tab meanwhile.
        messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello!");
        when(generationManager.retry(any())).thenReturn(null);

        fragment.retryResponse();

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT), timelineTypes(fragment));
    }

    @Test
    void retryResponse_rejected_showsTheRunningResponse_withoutNotification() {
        AiConversation conversation = conversationService.addConversation("Chat");
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        // Another tab retried first.
        when(generationManager.getGeneration(any())).thenReturn(
                Optional.of(new AiChatGeneration(conversation.getId(), GENERATION_ID, List.of(), 0)));
        when(generationManager.retry(any())).thenThrow(new AiChatGenerationRejectedException(conversation.getId()));

        fragment.retryResponse();

        assertTrue(fragment.isAwaitingResponse());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
        verify(notifications, never()).create(anyString());
    }

    @Test
    void retryResponse_fails_showsErrorNotification() {
        AiConversation conversation = conversationService.addConversation("Chat");
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        when(generationManager.retry(any())).thenThrow(new RejectedExecutionException("Shutting down"));
        Notifications.NotificationBuilder notification = notificationBuilder();

        fragment.retryResponse();

        assertFalse(fragment.isAwaitingResponse());
        verify(notifications).create(
                messages.getMessage(CHAT_MESSAGES_GROUP, "aiChatFragment.errorProcessingMessage"));
        verify(notification).withType(Notifications.Type.ERROR);
    }

    @Test
    void retryResponse_whileResponseShown_ignored() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));

        fragment.retryResponse();

        verify(generationManager, never()).retry(any());
        assertTrue(fragment.isAwaitingResponse());
    }

    @Test
    void sendMessage_rejected_showsWarningNotification() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        when(generationManager.start(any(), any())).thenThrow(
                new AiChatGenerationRejectedException(conversation.getId()));
        Notifications.NotificationBuilder notification = notificationBuilder();

        fragment.sendMessage("Mine");

        verify(notifications).create(messages.getMessage(CHAT_MESSAGES_GROUP, "aiChatFragment.alreadyResponding"));
        verify(notification).withType(Notifications.Type.WARNING);
        verify(notification).show();
    }

    @Test
    void sendMessage_startFails_showsErrorNotification() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        when(generationManager.start(any(), any())).thenThrow(new IllegalStateException("database down"));
        Notifications.NotificationBuilder notification = notificationBuilder();

        fragment.sendMessage("Mine");

        verify(notifications).create(
                messages.getMessage(CHAT_MESSAGES_GROUP, "aiChatFragment.errorProcessingMessage"));
        verify(notification).withType(Notifications.Type.ERROR);
        verify(notification).show();
    }

    @Test
    void sendMessage_rejectedButGenerationAlreadyEnded_keepsTextAndComposerEnabled() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        // The response of the other tab ended before the resync read the snapshot.
        messageService.createMessage(conversation, AiChatMessageType.USER, "From the other tab");
        messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Answer");
        when(generationManager.getGeneration(any())).thenReturn(Optional.empty());
        when(generationManager.start(any(), any())).thenThrow(
                new AiChatGenerationRejectedException(conversation.getId()));
        messageField(fragment).setValue("Mine");

        fragment.sendMessage("Mine");

        assertFalse(fragment.isAwaitingResponse());
        assertTrue(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT), timelineTypes(fragment));
        assertEquals("Mine", composer(fragment).getValue());
    }

    @Test
    void onGenerationStarted_otherGenerationShown_reloadsTimeline_andDropsItsStatuses() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        UUID previous = UUID.randomUUID();
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), previous));
        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), previous,
                new AiToolStatusUpdate("Old step"), 1));
        // The outcome of the previous response was missed; the next question came from another tab.
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello!");
        messageService.createMessage(conversation, AiChatMessageType.USER, "Next");

        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));

        assertTrue(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT, TimelineItemType.USER,
                TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
        assertTrue(timelineItems(fragment).get(3).getStatusUpdates().isEmpty());

        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), GENERATION_ID,
                new AiToolStatusUpdate("New step"), 1));
        assertEquals(1, timelineItems(fragment).get(3).getStatusUpdates().size());
    }

    @Test
    void onGenerationStarted_ownSend_doesNotReloadTimeline() {
        AiConversation conversation = conversationService.addConversation("Chat");
        when(generationManager.start(any(), any())).thenAnswer(inv ->
                messageService.createMessage(inv.getArgument(0), AiChatMessageType.USER, inv.getArgument(1)));
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        fragment.sendMessage("Hi");
        // A reload would now show an empty conversation.
        messageService.clear();

        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));

        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
    }

    @Test
    void onGenerationStarted_nothingShown_reloadsConversationTitle() {
        AiConversation conversation = conversationService.addConversation("New chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        // The sending tab titled the conversation by its first message.
        AiConversation titled = metadata.create(AiConversation.class);
        titled.setId(conversation.getId());
        titled.setTitle("Hi there");
        conversationService.save(titled);
        messageService.createMessage(titled, AiChatMessageType.USER, "Hi there");

        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));

        assertEquals("Hi there", ((H3) UiComponentUtils.getComponent(fragment, "conversationTitle")).getText());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
    }

    @Test
    void reattach_responseEndsWhenSnapshotIsRead_showsAnswer() {
        AiConversation conversation = conversationService.addConversation("Chat");
        messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        // The answer is saved and the response ends just as the snapshot is read.
        when(generationManager.getGeneration(any())).thenAnswer(inv -> {
            messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello!");
            return Optional.empty();
        });

        reattach(fragment);

        assertFalse(fragment.isAwaitingResponse());
        assertEquals(List.of(TimelineItemType.USER, TimelineItemType.ASSISTANT), timelineTypes(fragment));
    }

    @Test
    void reattach_snapshotWithSeveralStatuses_rendersThinkingOnce() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation.getId()));
        when(generationManager.getGeneration(any())).thenReturn(Optional.of(new AiChatGeneration(
                conversation.getId(), GENERATION_ID, List.of(new AiToolStatusUpdate("Searching", "3 found"),
                new AiToolStatusUpdate("Sorting", "done"), new AiToolStatusUpdate("Counting")), 3)));
        CollectionContainer<TimelineItem> timelineItemsDc =
                FragmentUtils.getFragmentData(fragment).getContainer("timelineItemsDc");
        AtomicInteger rowRenders = new AtomicInteger();
        timelineItemsDc.addCollectionChangeListener(event -> {
            if (event.getChangeType() == CollectionChangeType.SET_ITEM) {
                rowRenders.incrementAndGet();
            }
        });

        reattach(fragment);

        assertEquals(List.of("Searching", "Sorting", "Counting"), timelineItems(fragment).get(0).getStatusUpdates()
                .stream()
                .map(TimelineItemStatus::getMessage)
                .toList());
        assertEquals(1, rowRenders.get());
    }

    AiChatView navigate(UUID id) {
        navigationSupport.navigate(AiChatView.class,
                routeSupport.createRouteParameters(AiChatView.ROUTE_PARAM_ID, id));
        return UiTestUtils.getCurrentView();
    }

    AiChatFragment chatFragment(AiChatView view) {
        return UiTestUtils.getComponent(view, "chatFragment");
    }

    AiChatInputFragment composer(AiChatFragment fragment) {
        return (AiChatInputFragment) UiComponentUtils.getComponent(fragment, "composerFragment");
    }

    Notifications.NotificationBuilder notificationBuilder() {
        Notifications.NotificationBuilder builder = mock(Notifications.NotificationBuilder.class, RETURNS_SELF);
        when(notifications.create(anyString())).thenReturn(builder);
        return builder;
    }

    void reattach(AiChatFragment fragment) {
        Element parent = fragment.getElement().getParent();
        int index = parent.indexOfChild(fragment.getElement());
        parent.removeChild(fragment.getElement());
        parent.insertChild(index, fragment.getElement());
    }

    JmixTextArea messageField(AiChatFragment fragment) {
        return (JmixTextArea) UiComponentUtils.getComponent(composer(fragment), "messageField");
    }

    List<TimelineItem> timelineItems(AiChatFragment fragment) {
        @SuppressWarnings("unchecked")
        JmixVirtualList<TimelineItem> list = (JmixVirtualList<TimelineItem>) UiComponentUtils.getComponent(
                fragment, "timelineList");
        @SuppressWarnings("unchecked")
        DataProvider<TimelineItem, Object> dataProvider =
                (DataProvider<TimelineItem, Object>) (DataProvider<?, ?>) list.getDataProvider();
        return dataProvider.fetch(new Query<>()).toList();
    }

    List<TimelineItemType> timelineTypes(AiChatFragment fragment) {
        return timelineItems(fragment).stream().map(TimelineItem::getType).toList();
    }
}
