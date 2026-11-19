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

package chat_panel;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.function.SerializableSupplier;
import io.jmix.aitoolsflowui.fragment.chat.AiChatFragment;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanel;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanelProvider;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputFragment;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputPanel;
import io.jmix.aitoolsflowui.generation.AiChatGenerationManager;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationFailedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationStartedEvent;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiConversationAutoTitleService;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.aitoolsflowui.view.chathub.AiChatHubView;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.UiComponentUtils;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiChatInputPanel;
import test_support.TestAiChatMessageService;
import test_support.TestAiChatService;
import test_support.TestAiChatPanel;
import test_support.TestAiConversationService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A provider ordered before the add-on's own one draws the chat and the hub input with its panels, while the chat
 * fragment keeps sending, following the response and supplying the texts.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class,
        AiChatPanelOverrideTest.TestPanelConfiguration.class})
class AiChatPanelOverrideTest {

    static final UUID GENERATION_ID = UUID.randomUUID();

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

    @MockitoBean
    AiChatGenerationManager generationManager;
    @MockitoBean
    AiConversationAutoTitleService autoTitleService;
    // Deep stubs keep notifications a test does not look at from failing on a null builder.
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    Notifications notifications;

    @BeforeEach
    void setUp() {
        when(generationManager.start(any(), any())).thenAnswer(inv ->
                messageService.createMessage(inv.getArgument(0), AiChatMessageType.USER, inv.getArgument(1)));
    }

    @AfterEach
    void tearDown() {
        conversationService.clear();
        messageService.clear();
        chatService.reset();
        TestPanelProvider.lastChatPanel = null;
        TestPanelProvider.lastInputPanel = null;
    }

    @Test
    void aiChatView_higherPriorityProvider_drawsWithItsPanel() {
        AiConversation conversation = conversationService.addConversation("Revenue by region");
        AiChatMessage message = messageService.createMessage(conversation, AiChatMessageType.USER, "Hi");

        AiChatView view = navigate(conversation);

        TestAiChatPanel panel = assertInstanceOf(TestAiChatPanel.class, panelOf(chatFragment(view)));
        assertEquals(List.of(message.getId()), panel.shownMessages.stream().map(AiChatMessage::getId).toList());
    }

    @Test
    void titleRow_isDrawnByTheFragment_withAnyPanel() {
        AiConversation conversation = conversationService.addConversation("Revenue by region");

        AiChatFragment fragment = chatFragment(navigate(conversation));

        assertInstanceOf(TestAiChatPanel.class, panelOf(fragment));
        H3 title = assertInstanceOf(H3.class, UiComponentUtils.getComponent(fragment, "conversationTitle"));
        assertEquals("Revenue by region", title.getText());
        assertTrue(UiComponentUtils.getComponent(fragment, "renameConversationBtn").isVisible());
    }

    @Test
    void panelSubmit_sendsThroughTheFragment() {
        AiConversation conversation = conversationService.addConversation("Chat");
        navigate(conversation);
        TestAiChatPanel panel = lastChatPanel();

        panel.submit("Hi");

        verify(generationManager).start(any(), eq("Hi"));
        assertTrue(panel.responseRunning);
    }

    @Test
    void failure_textComesFromTheFragment() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation));
        fragment.onGenerationStarted(new AiChatGenerationStartedEvent(this, conversation.getId(), GENERATION_ID));

        fragment.onGenerationFailed(new AiChatGenerationFailedEvent(this, conversation.getId(), GENERATION_ID));

        assertEquals(messages.getMessage("io.jmix.aitoolsflowui.fragment.chat",
                "aiChatFragment.errorProcessingMessage"), lastChatPanel().failure);
    }

    @Test
    void panelStop_cancelsTheBoundConversation() {
        AiConversation conversation = conversationService.addConversation("Chat");
        navigate(conversation);

        runStop(lastChatPanel());

        verify(generationManager).cancel(argThat(cancelled -> conversation.getId().equals(cancelled.getId())));
    }

    @Test
    void panelStop_conversationNotAccessible_resyncsThePanel() {
        AiConversation conversation = conversationService.addConversation("Chat");
        navigate(conversation);
        TestAiChatPanel panel = lastChatPanel();
        int shownBefore = panel.showMessagesCalls;
        when(generationManager.cancel(any())).thenThrow(new AccessDeniedException("entity", "AiConversation"));

        runStop(panel);

        assertEquals(shownBefore + 1, panel.showMessagesCalls);
    }

    @Test
    void panelRetry_retriesTheBoundConversation() {
        AiConversation conversation = conversationService.addConversation("Chat");
        navigate(conversation);
        TestAiChatPanel panel = lastChatPanel();

        Objects.requireNonNull(panel.retryHandler).run();

        verify(generationManager).retry(argThat(retried -> conversation.getId().equals(retried.getId())));
    }

    @Test
    void input_panelLockedWhileResponding_isDisabledWhileResponseRuns() {
        AiConversation conversation = conversationService.addConversation("Chat");
        navigate(conversation);
        TestAiChatPanel panel = lastChatPanel();

        panel.submit("Hi");

        assertTrue(panel.responseRunning);
        assertFalse(panel.inputEnabled);
    }

    @Test
    void input_panelNotLockedWhileResponding_staysEnabledUnlessHostDisablesIt() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation));
        TestAiChatPanel panel = lastChatPanel();
        panel.lockedWhileResponding = false;

        panel.submit("Hi");

        assertTrue(panel.responseRunning);
        assertTrue(panel.inputEnabled);

        fragment.setMessageInputEnabled(false);

        assertFalse(panel.inputEnabled);
    }

    @Test
    void submit_whileResponseRuns_rejected() {
        AiConversation conversation = conversationService.addConversation("Chat");
        navigate(conversation);
        TestAiChatPanel panel = lastChatPanel();
        panel.lockedWhileResponding = false;
        assertTrue(panel.submit("Hi").isAccepted());
        assertTrue(panel.responseRunning);

        assertFalse(panel.submit("Again").isAccepted());
        verify(generationManager).start(any(), eq("Hi"));
        verify(generationManager, never()).start(any(), eq("Again"));
    }

    @Test
    void submit_aiNotAvailable_rejected() {
        AiConversation conversation = conversationService.addConversation("Chat");
        navigate(conversation);
        TestAiChatPanel panel = lastChatPanel();
        chatService.setAvailable(false);

        assertFalse(panel.submit("Hi").isAccepted());
        verify(generationManager, never()).start(any(), any());
    }

    @Test
    void submit_noConversation_rejected() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation));
        TestAiChatPanel panel = lastChatPanel();
        fragment.setConversation(null);

        assertFalse(panel.submit("Hi").isAccepted());
        verify(generationManager, never()).start(any(), any());
    }

    @Test
    void avatar_reachesPanelWithTheCapability() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AiChatFragment fragment = chatFragment(navigate(conversation));
        SerializableSupplier<Component> avatarSupplier = Span::new;

        fragment.setAiAvatarIconSupplier(avatarSupplier);

        assertSame(avatarSupplier, lastChatPanel().avatarIconSupplier);
    }

    @Test
    void hub_higherPriorityProvider_showsItsInput_andStartsAConversation() {
        navigationSupport.navigate(AiChatHubView.class);
        AiChatHubView hub = UiTestUtils.getCurrentView();
        AiChatInputFragment input = UiTestUtils.getComponent(hub, "hubFragment.chatInput");
        TestAiChatInputPanel inputPanel = assertInstanceOf(TestAiChatInputPanel.class,
                UiComponentUtils.getComponent(input, "inputPanelContainer").getChildren().findFirst().orElseThrow());

        inputPanel.submit("Revenue by region");

        assertInstanceOf(AiChatView.class, UiTestUtils.getCurrentView());
    }

    AiChatView navigate(AiConversation conversation) {
        navigationSupport.navigate(AiChatView.class,
                routeSupport.createRouteParameters(AiChatView.ROUTE_PARAM_ID, conversation.getId()));
        return UiTestUtils.getCurrentView();
    }

    AiChatFragment chatFragment(AiChatView view) {
        return UiTestUtils.getComponent(view, "chatFragment");
    }

    Component panelOf(AiChatFragment fragment) {
        return UiComponentUtils.getComponent(fragment, "chatPanelContainer")
                .getChildren().findFirst().orElseThrow();
    }

    void runStop(TestAiChatPanel panel) {
        Objects.requireNonNull(panel.stopHandler).run();
    }

    TestAiChatPanel lastChatPanel() {
        return Objects.requireNonNull(TestPanelProvider.lastChatPanel);
    }

    @TestConfiguration
    static class TestPanelConfiguration {

        @Bean
        TestPanelProvider testPanelProvider() {
            return new TestPanelProvider();
        }
    }

    static class TestPanelProvider implements AiChatPanelProvider {

        @Nullable
        static TestAiChatPanel lastChatPanel;
        @Nullable
        static TestAiChatInputPanel lastInputPanel;

        @Override
        public AiChatPanel createChatPanel(FragmentOwner owner) {
            lastChatPanel = new TestAiChatPanel();
            return lastChatPanel;
        }

        @Override
        public AiChatInputPanel createInputPanel(FragmentOwner owner) {
            lastInputPanel = new TestAiChatInputPanel();
            return lastInputPanel;
        }

        @Override
        public int getOrder() {
            return JmixOrder.HIGHEST_PRECEDENCE;
        }
    }
}
