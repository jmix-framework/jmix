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

package chat_data_block;

import com.vaadin.flow.component.Component;
import io.jmix.aitoolsflowui.fragment.chat.AiChatFragment;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanel;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanelProvider;
import io.jmix.aitoolsflowui.fragment.chat.AiConversationCreatedEvent;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputPanel;
import io.jmix.aitoolsflowui.generation.AiChatGenerationManager;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCancelledEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationStartedEvent;
import io.jmix.aitoolsflowui.model.AiChatDataBlock;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.prompt.AiSuggestedPromptContext;
import io.jmix.aitoolsflowui.service.AiConversationAutoTitleService;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.fragment.FragmentOwner;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiChatInputPanel;
import test_support.TestAiChatMessageService;
import test_support.TestAiChatPanel;
import test_support.TestAiChatService;
import test_support.TestAiConversationService;
import test_support.TestDataBlockAiChatPanel;
import test_support.TestSuggestedPromptProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The chat fragment shows a pending data block through the panel, sends it with the user's next question as one
 * message kept in the assistant memory, and creates the conversation on the first message when none is bound.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class,
        AiChatFragmentDataBlockTest.TestDataBlockConfiguration.class})
class AiChatFragmentDataBlockTest {

    static final String CONTENT = "**Orders**\n\n| Number |\n|---|\n| 1 |";
    static final List<String> PROMPTS = List.of("Summarize", "Compare");

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    RouteSupport routeSupport;
    @Autowired
    TestAiConversationService conversationService;
    @Autowired
    TestAiChatMessageService messageService;
    @Autowired
    TestAiChatService chatService;
    @Autowired
    Messages messages;
    @Autowired
    Metadata metadata;
    @Autowired
    @Qualifier("firstPromptProvider")
    TestSuggestedPromptProvider firstPromptProvider;
    @Autowired
    @Qualifier("secondPromptProvider")
    TestSuggestedPromptProvider secondPromptProvider;

    @MockitoBean
    AiChatGenerationManager generationManager;
    @MockitoBean
    AiConversationAutoTitleService autoTitleService;
    // Deep stubs keep the builders of notifications from failing on a null result.
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    Notifications notifications;

    AiConversation conversation;

    @BeforeEach
    void setUp() {
        conversation = conversationService.addConversation("New chat");
        when(generationManager.start(any(), any())).thenAnswer(inv ->
                messageService.createMessage(inv.getArgument(0), AiChatMessageType.USER, inv.getArgument(1)));
        when(generationManager.start(any(), any(), anyBoolean())).thenAnswer(inv ->
                messageService.createMessage(inv.getArgument(0), AiChatMessageType.USER, inv.getArgument(1),
                        (boolean) inv.getArgument(2)));
    }

    @AfterEach
    void tearDown() {
        conversationService.clear();
        messageService.clear();
        chatService.reset();
        firstPromptProvider.reset();
        secondPromptProvider.reset();
        TestPanelProvider.lastChatPanel = null;
        TestPanelProvider.dataBlockSupported = true;
    }

    @Test
    void dataBlockSet_showsCaptionAndPrompts_withoutWarning() {
        secondPromptProvider.prompts = PROMPTS;
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();

        fragment.setDataBlock(block());

        assertTrue(panel.dataBlockShown);
        assertEquals("Orders · 1 row", panel.dataBlockCaption);
        assertNull(panel.dataBlockWarning);
        assertEquals(PROMPTS, panel.suggestedPrompts);
        assertNotNull(panel.dataBlockRemoveHandler);
        assertNotNull(panel.suggestedPromptHandler);
    }

    @Test
    void dataBlockSent_withQuestion_composesOneKeptMessage_titlesByQuestion_andHidesBlock() {
        AiChatView view = navigate();
        AiChatFragment fragment = chatFragment(view);
        TestDataBlockAiChatPanel panel = lastChatPanel();
        fragment.setDataBlock(block());

        panel.submit("Which order is the largest?");

        List<AiChatMessage> saved = List.copyOf(messageService.loadMessages(conversation));
        assertEquals(1, saved.size());
        assertEquals("Which order is the largest?\n\n" + CONTENT, saved.get(0).getContent());
        assertTrue(saved.get(0).getKeepInContext());
        assertEquals("Which order is the largest?",
                Objects.requireNonNull(conversationService.loadConversation(conversation.getId())).getTitle());
        assertFalse(panel.dataBlockShown);
        assertNull(panel.dataBlockRemoveHandler);
        assertNull(fragment.getDataBlock());
    }

    @Test
    void messageSent_afterDataBlockSent_goesWithoutData() {
        AiChatFragment fragment = chatFragment(navigate());
        fragment.setDataBlock(block());
        fragment.sendMessage("First");
        // The response ends before the next question can be sent.
        fragment.onGenerationCancelled(new AiChatGenerationCancelledEvent(this, conversation.getId(),
                UUID.randomUUID()));

        fragment.sendMessage("Second");

        verify(generationManager).start(any(), eq("Second"));
    }

    @Test
    void dataBlockSet_memoryFull_showsWarning() {
        messageService.setKeepInContextLimit(1);
        keptMessage("Kept");
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();

        fragment.setDataBlock(block());

        assertEquals(messages.getMessage("askAiAboutData.memoryFull"), panel.dataBlockWarning);
    }

    @Test
    void dataBlockSent_memoryFull_isSavedUnkept() {
        messageService.setKeepInContextLimit(1);
        keptMessage("Kept");
        AiChatFragment fragment = chatFragment(navigate());
        fragment.setDataBlock(block());

        fragment.sendMessage("Question");

        verify(generationManager).start(any(), eq("Question\n\n" + CONTENT), eq(true));
        AiChatMessage sent = List.copyOf(messageService.loadMessages(conversation)).get(1);
        assertFalse(Boolean.TRUE.equals(sent.getKeepInContext()));
    }

    @Test
    void messageReleased_memoryNoLongerFull_warningGoesAway() {
        messageService.setKeepInContextLimit(1);
        AiChatMessage kept = keptMessage("Kept");
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();
        fragment.setDataBlock(block());
        assertNotNull(panel.dataBlockWarning);

        panel.requestKeepInContext(kept, false);

        assertTrue(panel.dataBlockShown);
        assertNull(panel.dataBlockWarning);
    }

    @Test
    void messageKept_memoryBecomesFull_warningAppears() {
        messageService.setKeepInContextLimit(1);
        AiChatMessage message = messageService.createMessage(conversation, AiChatMessageType.USER, "Remember me");
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();
        fragment.setDataBlock(block());
        assertNull(panel.dataBlockWarning);

        panel.requestKeepInContext(message, true);

        assertEquals(messages.getMessage("askAiAboutData.memoryFull"), panel.dataBlockWarning);
    }

    @Test
    void promptPicked_sendsAtOnceWithBlock() {
        secondPromptProvider.prompts = PROMPTS;
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();
        fragment.setDataBlock(block());

        panel.pickPrompt("Compare");

        verify(generationManager).start(any(), eq("Compare\n\n" + CONTENT), eq(true));
        assertTrue(fragment.isAwaitingResponse());
        assertFalse(panel.dataBlockShown);
    }

    @Test
    void dataBlockRemoved_hidesBlockAndPrompts_andNextMessageGoesWithoutData() {
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();
        fragment.setDataBlock(block());

        panel.removeDataBlock();
        panel.submit("Hi");

        assertFalse(panel.dataBlockShown);
        assertTrue(panel.suggestedPrompts.isEmpty());
        assertNull(fragment.getDataBlock());
        verify(generationManager).start(any(), eq("Hi"));
        verify(generationManager, never()).start(any(), any(), anyBoolean());
    }

    @Test
    void blankQuestion_sendsNothing_andKeepsBlock() {
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();
        fragment.setDataBlock(block());

        fragment.sendMessage("   ");

        verify(generationManager, never()).start(any(), any(), anyBoolean());
        assertTrue(panel.dataBlockShown);
        assertNotNull(fragment.getDataBlock());
    }

    @Test
    void promptPicked_responseRunning_sendsNothing_andKeepsBlock() {
        secondPromptProvider.prompts = PROMPTS;
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();
        fragment.setDataBlock(block());
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), UUID.randomUUID()));

        panel.pickPrompt("Compare");

        verify(generationManager, never()).start(any(), any(), anyBoolean());
        assertTrue(panel.dataBlockShown);
        assertNotNull(fragment.getDataBlock());
    }

    @Test
    void sendFailed_keepsBlock() {
        doThrow(new IllegalStateException("db down")).when(generationManager).start(any(), any(), anyBoolean());
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();
        fragment.setDataBlock(block());

        AiChatPanel.SubmitResult result = panel.submit("Question");

        assertFalse(result.isAccepted());
        assertTrue(panel.dataBlockShown);
        assertNotNull(fragment.getDataBlock());
    }

    @Test
    void readOnly_hidesBlock_andShowsItAgainWhenEditable() {
        secondPromptProvider.prompts = PROMPTS;
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();
        fragment.setDataBlock(block());

        fragment.setReadOnly(true);

        assertFalse(panel.dataBlockShown);
        assertTrue(panel.suggestedPrompts.isEmpty());
        assertNull(panel.suggestedPromptHandler);
        assertNotNull(fragment.getDataBlock());

        fragment.setReadOnly(false);

        assertTrue(panel.dataBlockShown);
        assertEquals(PROMPTS, panel.suggestedPrompts);
    }

    @Test
    void prompts_explicitOnBlock_win() {
        firstPromptProvider.prompts = List.of("From provider");
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();

        fragment.setDataBlock(new AiChatDataBlock("Orders · 1 row", CONTENT, null, List.of("Explicit")));

        assertEquals(List.of("Explicit"), panel.suggestedPrompts);
        assertTrue(firstPromptProvider.askedContexts.isEmpty());
    }

    @Test
    void prompts_fromProviders_askedWithConversationAndBlockEntity() {
        secondPromptProvider.prompts = List.of("Second");
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();
        MetaClass metaClass = metadata.getClass(AiChatMessage.class);

        fragment.setDataBlock(new AiChatDataBlock("Messages · 1 row", CONTENT, metaClass));

        assertEquals(List.of("Second"), panel.suggestedPrompts);
        assertEquals(1, firstPromptProvider.askedContexts.size());
        AiSuggestedPromptContext context = firstPromptProvider.askedContexts.get(0);
        assertSame(metaClass, context.getDataMetaClass());
        assertEquals(conversation.getId(), Objects.requireNonNull(context.getConversation()).getId());
    }

    @Test
    void prompts_resolvedOncePerBlock_notOnRefresh() {
        secondPromptProvider.prompts = PROMPTS;
        AiChatMessage message = messageService.createMessage(conversation, AiChatMessageType.USER, "Remember me");
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();
        fragment.setDataBlock(block());

        // A resync, a memory change and an input visibility change all redraw the block.
        fragment.setConversationId(conversation.getId());
        panel.requestKeepInContext(message, true);
        fragment.setReadOnly(true);
        fragment.setReadOnly(false);

        assertEquals(1, firstPromptProvider.askedContexts.size());
        assertEquals(1, secondPromptProvider.askedContexts.size());
        assertEquals(PROMPTS, panel.suggestedPrompts);

        fragment.setDataBlock(block());

        assertEquals(2, firstPromptProvider.askedContexts.size());
        assertEquals(2, secondPromptProvider.askedContexts.size());
    }

    @Test
    void prompts_noProviderAnswers_offersNone() {
        AiChatFragment fragment = chatFragment(navigate());
        TestDataBlockAiChatPanel panel = lastChatPanel();

        fragment.setDataBlock(block());

        assertTrue(panel.dataBlockShown);
        assertTrue(panel.suggestedPrompts.isEmpty());
        assertNull(panel.suggestedPromptHandler);
        assertEquals(1, secondPromptProvider.askedContexts.size());
    }

    @Test
    void panelWithoutDataBlock_ignoresBlock_andSendsPlainMessage() {
        TestPanelProvider.dataBlockSupported = false;
        AiChatFragment fragment = chatFragment(navigate());

        fragment.setDataBlock(block());
        fragment.sendMessage("Hi");

        assertNull(fragment.getDataBlock());
        verify(generationManager).start(any(), eq("Hi"));
        verify(generationManager, never()).start(any(), any(), anyBoolean());
    }

    @Test
    void noConversation_withBlock_showsInputAndBlock_andStoresNothing() {
        secondPromptProvider.prompts = PROMPTS;
        AiChatView view = navigateWithoutConversation();
        TestDataBlockAiChatPanel panel = lastChatPanel();

        view.setDataBlock(block());

        assertTrue(chatFragment(view).isVisible());
        assertFalse(emptyLayout(view).isVisible());
        assertTrue(panel.inputVisible);
        assertTrue(panel.dataBlockShown);
        assertEquals(PROMPTS, panel.suggestedPrompts);
        // Only the conversation of the set-up.
        assertEquals(1, conversationService.loadConversations().size());
    }

    @Test
    void noConversation_firstSend_createsConversation_firesEvent_andSends() {
        AiChatView view = navigateWithoutConversation();
        AiChatFragment fragment = chatFragment(view);
        TestDataBlockAiChatPanel panel = lastChatPanel();
        List<AiConversationCreatedEvent> createdEvents = new ArrayList<>();
        fragment.addConversationCreatedListener(createdEvents::add);
        view.setDataBlock(block());

        AiChatPanel.SubmitResult result = panel.submit("Which order is the largest?");

        assertTrue(result.isAccepted());
        assertEquals(1, createdEvents.size());
        AiConversation created = createdEvents.get(0).getConversation();
        List<AiChatMessage> saved = List.copyOf(messageService.loadMessages(created));
        assertEquals(1, saved.size());
        assertEquals("Which order is the largest?\n\n" + CONTENT, saved.get(0).getContent());
        assertTrue(saved.get(0).getKeepInContext());
        assertEquals("Which order is the largest?",
                Objects.requireNonNull(conversationService.loadConversation(created.getId())).getTitle());
        assertEquals("Which order is the largest?", view.getPageTitle());
        assertFalse(panel.dataBlockShown);
        assertTrue(panel.inputVisible);
    }

    @Test
    void viewNavigatedToAnotherConversation_dropsPendingBlock() {
        AiChatView view = navigate();
        view.setDataBlock(block());
        AiConversation other = conversationService.addConversation("Other chat");

        navigationSupport.navigate(AiChatView.class,
                routeSupport.createRouteParameters(AiChatView.ROUTE_PARAM_ID, other.getId()));

        AiChatView shown = UiTestUtils.getCurrentView();
        assertSame(view, shown);
        assertNull(chatFragment(shown).getDataBlock());
        assertFalse(lastChatPanel().dataBlockShown);
    }

    @Test
    void viewNavigatedToUnknownId_fromNewConversation_showsNotFound() {
        AiChatView view = navigateWithoutConversation();
        view.setDataBlock(block());

        navigationSupport.navigate(AiChatView.class,
                routeSupport.createRouteParameters(AiChatView.ROUTE_PARAM_ID, UUID.randomUUID()));

        AiChatView shown = UiTestUtils.getCurrentView();
        assertSame(view, shown);
        AiChatFragment fragment = chatFragment(shown);
        assertFalse(fragment.isNewConversation());
        assertNull(fragment.getDataBlock());
        assertFalse(fragment.isVisible());
        assertTrue(UiTestUtils.<Component>getComponent(shown, "notFoundLayout").isVisible());
    }

    @Test
    void viewNavigatedToSameConversation_keepsPendingBlock() {
        AiChatView view = navigate();
        view.setDataBlock(block());

        navigate();

        AiChatView shown = UiTestUtils.getCurrentView();
        assertSame(view, shown);
        assertNotNull(chatFragment(shown).getDataBlock());
    }

    @Test
    void newConversation_blockRemoved_keepsInput_andViewKeepsShowingChat() {
        AiChatView view = navigateWithoutConversation();
        AiChatFragment fragment = chatFragment(view);
        TestDataBlockAiChatPanel panel = lastChatPanel();
        view.setDataBlock(block());

        panel.removeDataBlock();

        assertTrue(fragment.isNewConversation());
        assertNull(fragment.getDataBlock());
        assertTrue(panel.inputVisible);
        assertFalse(panel.dataBlockShown);
        assertTrue(fragment.isVisible());
        assertFalse(emptyLayout(view).isVisible());
        assertEquals(1, conversationService.loadConversations().size());
    }

    @Test
    void newConversation_blockRemoved_firstMessage_createsConversation_andSendsWithoutData() {
        AiChatView view = navigateWithoutConversation();
        AiChatFragment fragment = chatFragment(view);
        TestDataBlockAiChatPanel panel = lastChatPanel();
        List<AiConversationCreatedEvent> createdEvents = new ArrayList<>();
        fragment.addConversationCreatedListener(createdEvents::add);
        view.setDataBlock(block());
        panel.removeDataBlock();

        AiChatPanel.SubmitResult result = panel.submit("Hi");

        assertTrue(result.isAccepted());
        assertEquals(1, createdEvents.size());
        List<AiChatMessage> saved = List.copyOf(messageService.loadMessages(createdEvents.get(0).getConversation()));
        assertEquals(1, saved.size());
        assertEquals("Hi", saved.get(0).getContent());
        assertFalse(Boolean.TRUE.equals(saved.get(0).getKeepInContext()));
        verify(generationManager, never()).start(any(), any(), anyBoolean());
        assertFalse(fragment.isNewConversation());
    }

    @Test
    void newConversation_hostBindsConversation_endsIt_andBlockGoesThere() {
        AiChatView view = navigateWithoutConversation();
        AiChatFragment fragment = chatFragment(view);
        TestDataBlockAiChatPanel panel = lastChatPanel();
        view.setDataBlock(block());

        view.setConversation(conversation);

        assertFalse(fragment.isNewConversation());
        assertTrue(panel.dataBlockShown);

        panel.submit("Question");

        assertEquals(1, conversationService.loadConversations().size());
        List<AiChatMessage> saved = List.copyOf(messageService.loadMessages(conversation));
        assertEquals("Question\n\n" + CONTENT, saved.get(0).getContent());
    }

    @Test
    void newConversation_fragmentBoundToConversation_endsIt() {
        AiChatView view = navigateWithoutConversation();
        AiChatFragment fragment = chatFragment(view);
        view.setDataBlock(block());

        fragment.setConversation(conversation);

        assertFalse(fragment.isNewConversation());
    }

    @Test
    void newConversation_hostClearsConversation_endsIt_andShowsEmptyState() {
        AiChatView view = navigateWithoutConversation();
        AiChatFragment fragment = chatFragment(view);
        TestDataBlockAiChatPanel panel = lastChatPanel();
        view.setDataBlock(block());
        panel.removeDataBlock();

        view.setConversation(null);

        assertFalse(fragment.isNewConversation());
        assertFalse(panel.inputVisible);
        assertFalse(fragment.isVisible());
        assertTrue(emptyLayout(view).isVisible());
    }

    @Test
    void noConversation_creationFails_rejectsMessage_andKeepsBlock() {
        AiChatView view = navigateWithoutConversation();
        AiChatFragment fragment = chatFragment(view);
        TestDataBlockAiChatPanel panel = lastChatPanel();
        view.setDataBlock(block());
        conversationService.setCreateFailure(new IllegalStateException("db down"));

        AiChatPanel.SubmitResult result = panel.submit("Question");

        assertFalse(result.isAccepted());
        verify(notifications).create(messages.getMessage(AiChatFragment.class,
                "aiChatFragment.errorProcessingMessage"));
        verify(generationManager, never()).start(any(), any(), anyBoolean());
        assertTrue(panel.dataBlockShown);
        assertNotNull(fragment.getDataBlock());
    }

    @Test
    void noConversation_noBlock_hidesInput_andShowsEmptyState() {
        AiChatView view = navigateWithoutConversation();
        TestDataBlockAiChatPanel panel = lastChatPanel();

        assertFalse(panel.inputVisible);
        assertFalse(panel.dataBlockShown);
        assertFalse(chatFragment(view).isVisible());
        assertTrue(emptyLayout(view).isVisible());
    }

    @Test
    void noConversation_aiUnavailable_hidesInputAndBlock() {
        chatService.setAvailable(false);
        AiChatView view = navigateWithoutConversation();
        TestDataBlockAiChatPanel panel = lastChatPanel();

        view.setDataBlock(block());

        assertFalse(panel.inputVisible);
        assertFalse(panel.dataBlockShown);
        assertNotNull(chatFragment(view).getDataBlock());
    }

    AiChatDataBlock block() {
        return new AiChatDataBlock("Orders · 1 row", CONTENT, null);
    }

    AiChatMessage keptMessage(String text) {
        return messageService.createMessage(conversation, AiChatMessageType.USER, text, true);
    }

    AiChatView navigate() {
        navigationSupport.navigate(AiChatView.class,
                routeSupport.createRouteParameters(AiChatView.ROUTE_PARAM_ID, conversation.getId()));
        return UiTestUtils.getCurrentView();
    }

    AiChatView navigateWithoutConversation() {
        navigationSupport.navigate(AiChatView.class);
        return UiTestUtils.getCurrentView();
    }

    AiChatFragment chatFragment(AiChatView view) {
        return UiTestUtils.getComponent(view, "chatFragment");
    }

    Component emptyLayout(AiChatView view) {
        return UiTestUtils.getComponent(view, "emptyLayout");
    }

    TestDataBlockAiChatPanel lastChatPanel() {
        return (TestDataBlockAiChatPanel) Objects.requireNonNull(TestPanelProvider.lastChatPanel);
    }

    @TestConfiguration
    static class TestDataBlockConfiguration {

        @Bean
        TestPanelProvider testDataBlockPanelProvider() {
            return new TestPanelProvider();
        }

        // Declared before the first one, so that only the order puts it second.
        @Bean
        TestSuggestedPromptProvider secondPromptProvider() {
            return new TestSuggestedPromptProvider(2);
        }

        @Bean
        TestSuggestedPromptProvider firstPromptProvider() {
            return new TestSuggestedPromptProvider(1);
        }
    }

    static class TestPanelProvider implements AiChatPanelProvider {

        @Nullable
        static TestAiChatPanel lastChatPanel;
        static boolean dataBlockSupported = true;

        @Override
        public AiChatPanel createChatPanel(FragmentOwner owner) {
            lastChatPanel = dataBlockSupported ? new TestDataBlockAiChatPanel() : new TestAiChatPanel();
            return lastChatPanel;
        }

        @Override
        public AiChatInputPanel createInputPanel(FragmentOwner owner) {
            return new TestAiChatInputPanel();
        }

        @Override
        public int getOrder() {
            return JmixOrder.HIGHEST_PRECEDENCE;
        }
    }
}
