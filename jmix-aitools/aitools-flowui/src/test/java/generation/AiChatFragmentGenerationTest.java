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

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.generation.AiChatGenerationManager;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCompletedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationFailedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationProgressEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationStartedEvent;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiConversationAutoTitleService;
import io.jmix.aitoolsflowui.view.chat.AiChatFragment;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.aitoolsflowui.view.chat.timeline.TimelineItem;
import io.jmix.aitoolsflowui.view.chat.timeline.TimelineItemStatus;
import io.jmix.aitoolsflowui.view.chat.timeline.TimelineItemType;
import io.jmix.aitoolsflowui.view.input.AiChatInputFragment;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.virtuallist.JmixVirtualList;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiChatMessageService;
import test_support.TestAiConversationService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The chat fragment sends through {@link AiChatGenerationManager} and renders its events: the thinking
 * indicator, folded tool statuses, the answer and the error. The manager is mocked and events are delivered
 * by calling the fragment's listeners.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatFragmentGenerationTest {

    static final UUID GENERATION_ID = UUID.randomUUID();

    @Autowired ViewNavigationSupport navigationSupport;
    @Autowired RouteSupport routeSupport;
    @Autowired TestAiConversationService conversationService;
    @Autowired TestAiChatMessageService messageService;

    @MockitoBean AiChatGenerationManager generationManager;
    @MockitoBean AiConversationAutoTitleService autoTitleService;

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
                new AiToolStatusUpdate("Searching")));
        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), GENERATION_ID,
                new AiToolStatusUpdate("Searching", "3 found")));

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
                new AiToolStatusUpdate("Searching")));

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
        // The renderer consumes the marker when it draws the row, so FALSE (not null) shows the row was marked.
        assertEquals(Boolean.FALSE, answerItem.getFresh());
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
                new AiToolStatusUpdate("Searching")));

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
                new AiToolStatusUpdate("Old step")));

        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));
        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), previous,
                new AiToolStatusUpdate("Late step")));
        AiChatMessage oldAnswer = messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Old");
        fragment.onGenerationCompleted(new AiChatGenerationCompletedEvent(this, conversation.getId(), previous,
                oldAnswer.getId()));
        fragment.onGenerationFailed(new AiChatGenerationFailedEvent(this, conversation.getId(), previous));

        assertTrue(fragment.isAwaitingResponse());
        assertFalse(composer(fragment).isInputEnabled());
        assertEquals(List.of(TimelineItemType.ASSISTANT_THINKING), timelineTypes(fragment));
        assertTrue(timelineItems(fragment).get(0).getStatusUpdates().isEmpty());

        fragment.onGenerationProgress(new AiChatGenerationProgressEvent(this, conversation.getId(), GENERATION_ID,
                new AiToolStatusUpdate("New step")));
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
